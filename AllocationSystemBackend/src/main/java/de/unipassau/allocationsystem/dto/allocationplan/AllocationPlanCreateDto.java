package de.unipassau.allocationsystem.dto.allocationplan;

import de.unipassau.allocationsystem.entity.AllocationPlan.PlanStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new allocation plan.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AllocationPlanCreateDto {

    @NotNull(message = "Academic year ID is required")
    private Long yearId;

    @NotBlank(message = "Plan name is required")
    @Size(max = 255, message = "Plan name must not exceed 255 characters")
    private String planName;

    @NotBlank(message = "Plan version is required")
    @Size(max = 100, message = "Plan version must not exceed 100 characters")
    private String planVersion;

    @NotNull(message = "Status is required")
    private PlanStatus status;

    private Boolean isCurrent = false;

    @Size(max = 5000, message = "Notes must not exceed 5000 characters")
    private String notes;
}
