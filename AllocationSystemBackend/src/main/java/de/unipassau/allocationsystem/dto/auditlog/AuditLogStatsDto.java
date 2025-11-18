package de.unipassau.allocationsystem.dto.auditlog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for audit log statistics and reports.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogStatsDto {

    private Map<String, Long> actionStatistics;
    private Map<String, Long> entityStatistics;
    private Map<String, Long> userActivityStatistics;
    private Long totalLogs;
}
