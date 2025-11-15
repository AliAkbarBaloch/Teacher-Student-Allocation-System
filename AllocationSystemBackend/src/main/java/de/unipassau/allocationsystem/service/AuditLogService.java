package de.unipassau.allocationsystem.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.allocationsystem.entity.AuditLog;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for managing audit logs.
 * Provides methods to create, query, and filter audit log entries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * Log an audit event asynchronously.
     * Uses a separate transaction to ensure audit logs are persisted even if the main transaction fails.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAsync(
        User user,
        AuditAction action,
        String targetEntity,
        String targetRecordId,
        Object previousValue,
        Object newValue,
        String description
    ) {
        try {
            log(user, action, targetEntity, targetRecordId, previousValue, newValue, description);
        } catch (Exception e) {
            log.error("Failed to create audit log asynchronously", e);
        }
    }

    /**
     * Log an audit event synchronously.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog log(
        User user,
        AuditAction action,
        String targetEntity,
        String targetRecordId,
        Object previousValue,
        Object newValue,
        String description
    ) {
        try {
            AuditLog auditLog = AuditLog.builder()
                .user(user)
                .userIdentifier(user != null ? user.getEmail() : "SYSTEM")
                .action(action)
                .targetEntity(targetEntity)
                .targetRecordId(targetRecordId)
                .previousValue(serializeValue(previousValue))
                .newValue(serializeValue(newValue))
                .description(description)
                .eventTimestamp(LocalDateTime.now())
                .build();

            // Capture request context if available
            enrichWithRequestContext(auditLog);

            return auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
            throw new RuntimeException("Failed to create audit log", e);
        }
    }

    /**
     * Log an audit event with automatic user detection from security context.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logWithCurrentUser(
        AuditAction action,
        String targetEntity,
        String targetRecordId,
        Object previousValue,
        Object newValue,
        String description
    ) {
        User currentUser = getCurrentUser();
        logAsync(currentUser, action, targetEntity, targetRecordId, 
                previousValue, newValue, description);
    }

    /**
     * Log a simple action without previous/new values.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(AuditAction action, String targetEntity, String description) {
        logWithCurrentUser(action, targetEntity, null, null, null, description);
    }

    /**
     * Log an entity creation.
     */
    public void logCreate(String entityName, String recordId, Object newValue) {
        logWithCurrentUser(AuditAction.CREATE, entityName, recordId, null, newValue,
            "Created " + entityName + " with ID: " + recordId);
    }

    /**
     * Log an entity update.
     */
    public void logUpdate(String entityName, String recordId, Object previousValue, Object newValue) {
        logWithCurrentUser(AuditAction.UPDATE, entityName, recordId, previousValue, newValue,
            "Updated " + entityName + " with ID: " + recordId);
    }

    /**
     * Log an entity deletion.
     */
    public void logDelete(String entityName, String recordId, Object previousValue) {
        logWithCurrentUser(AuditAction.DELETE, entityName, recordId, previousValue, null,
            "Deleted " + entityName + " with ID: " + recordId);
    }

    /**
     * Log a view/read operation.
     */
    public void logView(String entityName, String recordId) {
        logWithCurrentUser(AuditAction.VIEW, entityName, recordId, null, null,
            "Viewed " + entityName + " with ID: " + recordId);
    }

    /**
     * Get all audit logs with pagination.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    /**
     * Get audit logs with filters.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(
        Long userId,
        AuditAction action,
        String targetEntity,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    ) {
        return auditLogRepository.findByFilters(userId, action, targetEntity, startDate, endDate, pageable);
    }

    /**
     * Get audit logs for a specific entity and record.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsForEntity(String entityName, String recordId, Pageable pageable) {
        return auditLogRepository.findByTargetEntityAndTargetRecordId(entityName, recordId, pageable);
    }

    /**
     * Get audit logs for a specific user.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogsForUser(String userIdentifier, Pageable pageable) {
        return auditLogRepository.findByUserIdentifier(userIdentifier, pageable);
    }

    /**
     * Get action statistics for a date range.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getActionStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = auditLogRepository.getActionStatistics(startDate, endDate);
        Map<String, Long> statistics = new HashMap<>();
        for (Object[] result : results) {
            statistics.put(((AuditAction) result[0]).name(), (Long) result[1]);
        }
        return statistics;
    }

    /**
     * Get entity statistics for a date range.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getEntityStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = auditLogRepository.getEntityStatistics(startDate, endDate);
        Map<String, Long> statistics = new HashMap<>();
        for (Object[] result : results) {
            statistics.put((String) result[0], (Long) result[1]);
        }
        return statistics;
    }

    /**
     * Get user activity statistics for a date range.
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getUserActivityStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = auditLogRepository.getUserActivityStatistics(startDate, endDate);
        Map<String, Long> statistics = new HashMap<>();
        for (Object[] result : results) {
            statistics.put((String) result[0], (Long) result[1]);
        }
        return statistics;
    }

    /**
     * Get recent audit logs for monitoring.
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getRecentAuditLogs() {
        return auditLogRepository.findTop100ByOrderByEventTimestampDesc();
    }

    // Helper methods

    /**
     * Serialize object to JSON string.
     */
    private String serializeValue(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize value to JSON: {}", value, e);
            return value.toString();
        }
    }

    /**
     * Get current authenticated user from security context.
     */
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                return (User) authentication.getPrincipal();
            }
        } catch (Exception e) {
            log.debug("Could not retrieve current user from security context", e);
        }
        return null;
    }

    /**
     * Enrich audit log with request context information (IP address, user agent).
     */
    private void enrichWithRequestContext(AuditLog auditLog) {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
            }
        } catch (Exception e) {
            log.debug("Could not enrich audit log with request context", e);
        }
    }

    /**
     * Extract client IP address from request, handling proxies, and load balancers.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Get first IP if multiple is present
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }
}
