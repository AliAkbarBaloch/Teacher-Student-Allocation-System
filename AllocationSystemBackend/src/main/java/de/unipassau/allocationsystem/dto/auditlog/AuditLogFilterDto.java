package de.unipassau.allocationsystem.dto.auditlog;

import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for filtering audit logs.
 * All fields are optional; null values are ignored in the query.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogFilterDto {

    private Long userId;
    private String userIdentifier;
    private AuditAction action;
    private String targetEntity;
    private String targetRecordId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    // Pagination parameters
    private Integer page;
    private Integer size;
    private String sortBy;
    private String sortDirection;
}
