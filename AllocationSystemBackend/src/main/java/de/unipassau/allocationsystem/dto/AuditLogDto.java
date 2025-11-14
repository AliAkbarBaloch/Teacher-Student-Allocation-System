package de.unipassau.allocationsystem.dto;

import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for AuditLog entity.
 * Used for transferring audit log data to clients.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDto {

    private Long id;
    private String userIdentifier;
    private LocalDateTime eventTimestamp;
    private AuditAction action;
    private String targetEntity;
    private String targetRecordId;
    private String previousValue;
    private String newValue;
    private String description;
    private String ipAddress;
    private LocalDateTime createdAt;
}
