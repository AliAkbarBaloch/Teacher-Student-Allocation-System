package de.unipassau.allocationsystem.dto.allocationplan;

import de.unipassau.allocationsystem.entity.AllocationPlan.PlanStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing allocation plan.
 * All fields are optional to support partial updates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllocationPlanUpdateDto {

    @Size(max = 255, message = "Plan name must not exceed 255 characters")
    private String planName;

    private PlanStatus status;

    private Boolean isCurrent;

    @Size(max = 5000, message = "Notes must not exceed 5000 characters")
    private String notes;
}
