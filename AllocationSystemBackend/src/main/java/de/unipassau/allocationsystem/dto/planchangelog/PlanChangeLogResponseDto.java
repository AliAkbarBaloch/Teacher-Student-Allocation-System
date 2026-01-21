package de.unipassau.allocationsystem.dto.planchangelog;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO for plan change log response data.
 * Returns detailed information about changes made to allocation plans.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanChangeLogResponseDto {
    private Long id;
    private Long planId;
    private String changeType;
    private String entityType;
    private Long entityId;
    private String oldValue;
    private String newValue;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}