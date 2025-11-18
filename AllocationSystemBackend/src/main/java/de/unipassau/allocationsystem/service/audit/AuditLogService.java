package de.unipassau.allocationsystem.service.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.allocationsystem.entity.AuditLog;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.repository.AuditLogRepository;
import de.unipassau.allocationsystem.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * Service for managing audit logs.
 * Provides methods to create, query, and filter audit log entries.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    // ==================== Public API Methods ====================

    /**
     * Synchronously log an audit event with an explicit user.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog log(User user, AuditAction action, String targetEntity, String targetRecordId, Object previousValue, Object newValue, String description) {
        AuditLogContext context = captureContext(user);
        return persistAuditLog(context, action, targetEntity, targetRecordId,
                previousValue, newValue, description);
    }

    /**
     * Asynchronously log an audit event with explicit user.
     */
    public void logAsync(User user, AuditAction action, String targetEntity,
                        String targetRecordId, Object previousValue, Object newValue,
                        String description) {
        AuditLogContext context = captureContext(user);
        logAsyncInternal(context, action, targetEntity, targetRecordId,
                previousValue, newValue, description);
    }

    /**
     * Asynchronously log an audit event with current authenticated user.
     */
    public void logWithCurrentUser(AuditAction action, String targetEntity,
                                   String targetRecordId, Object previousValue,
                                   Object newValue, String description) {
        AuditLogContext context = captureContext(getCurrentUser());
        logAsyncInternal(context, action, targetEntity, targetRecordId,
                previousValue, newValue, description);
    }

    // ==================== Convenience Methods ====================
    public void logCreate(String entityName, String recordId, Object newValue) {
        logWithCurrentUser(AuditAction.CREATE, entityName, recordId, null, newValue,
                String.format("Created %s with ID: %s", entityName, recordId));
    }

    public void logUpdate(String entityName, String recordId, Object previousValue, Object newValue) {
        logWithCurrentUser(AuditAction.UPDATE, entityName, recordId, previousValue, newValue,
                String.format("Updated %s with ID: %s", entityName, recordId));
    }

    public void logDelete(String entityName, String recordId, Object previousValue) {
        logWithCurrentUser(AuditAction.DELETE, entityName, recordId, previousValue, null,
                String.format("Deleted %s with ID: %s", entityName, recordId));
    }

    public void logView(String entityName, String recordId) {
        logWithCurrentUser(AuditAction.VIEW, entityName, recordId, null, null,
                String.format("Viewed %s with ID: %s", entityName, recordId));
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(AuditAction action, String targetEntity, String description) {
        logWithCurrentUser(action, targetEntity, null, null, null, description);
    }

    // ==================== Internal Methods ====================
    @Async("auditExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void logAsyncInternal(AuditLogContext context, AuditAction action,
                                  String targetEntity, String targetRecordId,
                                  Object previousValue, Object newValue, String description) {
        try {
            persistAuditLog(context, action, targetEntity, targetRecordId,
                    previousValue, newValue, description);
        } catch (Exception e) {
            log.error("Failed to create audit log asynchronously", e);
        }
    }

    private AuditLog persistAuditLog(AuditLogContext context, AuditAction action,
                                     String targetEntity, String targetRecordId,
                                     Object previousValue, Object newValue, String description) {
        AuditLog auditLog = AuditLog.builder()
                .user(context.getUser())
                .userIdentifier(context.getUserIdentifier())
                .action(action)
                .targetEntity(targetEntity)
                .targetRecordId(targetRecordId)
                .previousValue(serializeValue(previousValue))
                .newValue(serializeValue(newValue))
                .description(description)
                .eventTimestamp(LocalDateTime.now())
                .ipAddress(context.getIpAddress())
                .userAgent(context.getUserAgent())
                .build();

        return auditLogRepository.save(auditLog);
    }

    private AuditLogContext captureContext(User user) {
        String userIdentifier = user != null ? user.getEmail() : "SYSTEM";
        String ipAddress = null;
        String userAgent = null;

        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                ipAddress = extractClientIpAddress(request);
                userAgent = request.getHeader("User-Agent");
            }
        } catch (Exception e) {
            log.debug("Could not capture request context", e);
        }

        return new AuditLogContext(user, userIdentifier, ipAddress, userAgent);
    }

    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() != null) {
                Object principal = authentication.getPrincipal();

                if (principal instanceof User) {
                    return (User) principal;
                }

                if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
                    return userRepository.findByEmail(userDetails.getUsername()).orElse(null);
                }
            }
        } catch (Exception e) {
            log.debug("Could not retrieve current user from security context", e);
        }
        return null;
    }

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

    private String extractClientIpAddress(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP",
                "WL-Proxy-Client-IP", "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    // ==================== Inner Class ====================

    @lombok.Value
    private static class AuditLogContext {
        User user;
        String userIdentifier;
        String ipAddress;
        String userAgent;
    }
}
