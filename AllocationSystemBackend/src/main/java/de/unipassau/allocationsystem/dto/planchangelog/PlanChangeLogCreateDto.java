package de.unipassau.allocationsystem.dto.planchangelog;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for creating a new plan change log entry.
 * Records changes made to allocation plans for audit purposes.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanChangeLogCreateDto {
    @NotNull(message = "Plan ID is required")
    private Long planId;

    @NotNull(message = "Change type is required")
    @Size(min = 1, max = 50, message = "Change type must be between 1 and 50 characters")
    private String changeType;

    @NotNull(message = "Entity type is required")
    @Size(min = 1, max = 100, message = "Entity type must be between 1 and 100 characters")
    private String entityType;

    @NotNull(message = "Entity ID is required")
    private Long entityId;

    private String oldValue;
    private String newValue;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}