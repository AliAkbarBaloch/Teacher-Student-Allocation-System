package de.unipassau.allocationsystem.service.audit;

import de.unipassau.allocationsystem.entity.AuditLog;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
/**
 * Service for querying and analyzing audit log data.
 * Provides methods for retrieving audit logs with various filters and statistics.
 */
public class AuditLogQueryService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Get all audit logs with pagination.
     */
    public Page<AuditLog> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    /**
     * Get audit logs with multiple filters.
     */
    public Page<AuditLog> getAuditLogs(Long userId, AuditAction action,
                                       String targetEntity, LocalDateTime startDate,
                                       LocalDateTime endDate, Pageable pageable) {
        return auditLogRepository.findByFilters(userId, action, targetEntity,
                startDate, endDate, pageable);
    }

    /**
     * Get audit logs for a specific entity and record.
     */
    public Page<AuditLog> getAuditLogsForEntity(String entityName, String recordId,
                                                Pageable pageable) {
        return auditLogRepository.findByTargetEntityAndTargetRecordId(
                entityName, recordId, pageable);
    }

    /**
     * Get audit logs for a specific user.
     */
    public Page<AuditLog> getAuditLogsForUser(String userIdentifier, Pageable pageable) {
        return auditLogRepository.findByUserIdentifier(userIdentifier, pageable);
    }

    /**
     * Get recent audit logs for monitoring.
     */
    public List<AuditLog> getRecentAuditLogs() {
        return auditLogRepository.findTop100ByOrderByEventTimestampDesc();
    }

    /**
     * Get action statistics for a date range.
     */
    public Map<String, Long> getActionStatistics(LocalDateTime startDate,
                                                 LocalDateTime endDate) {
        return convertToMap(
                auditLogRepository.getActionStatistics(startDate, endDate),
                result -> ((AuditAction) result[0]).name()
        );
    }

    /**
     * Get entity statistics for a date range.
     */
    public Map<String, Long> getEntityStatistics(LocalDateTime startDate,
                                                 LocalDateTime endDate) {
        return convertToMap(
                auditLogRepository.getEntityStatistics(startDate, endDate),
                result -> (String) result[0]
        );
    }

    /**
     * Get user activity statistics for a date range.
     */
    public Map<String, Long> getUserActivityStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        return convertToMap(
                auditLogRepository.getUserActivityStatistics(startDate, endDate),
                result -> (String) result[0]
        );
    }

    // Helper method to convert query results to map
    private Map<String, Long> convertToMap(List<Object[]> results, KeyExtractor keyExtractor) {
        Map<String, Long> map = new HashMap<>();
        for (Object[] result : results) {
            map.put(keyExtractor.extractKey(result), (Long) result[1]);
        }
        return map;
    }

    @FunctionalInterface
    private interface KeyExtractor {
        /**
         * Extracts a string key from a query result array.
         * 
         * @param result the result array from a query
         * @return the extracted key as a string
         */
        String extractKey(Object[] result);
    }

}
