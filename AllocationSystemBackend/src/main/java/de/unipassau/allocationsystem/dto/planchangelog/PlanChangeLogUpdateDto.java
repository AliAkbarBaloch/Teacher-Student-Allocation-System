package de.unipassau.allocationsystem.dto.planchangelog;

import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO for updating an existing plan change log entry.
 * All fields are optional for partial updates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanChangeLogUpdateDto {
    @Size(min = 1, max = 50, message = "Change type must be between 1 and 50 characters")
    private String changeType;

    @Size(min = 1, max = 100, message = "Entity type must be between 1 and 100 characters")
    private String entityType;

    private Long entityId;

    private String oldValue;
    private String newValue;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}