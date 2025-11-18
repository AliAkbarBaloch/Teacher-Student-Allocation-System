package de.unipassau.allocationsystem.dto;

import de.unipassau.allocationsystem.entity.AllocationPlan.PlanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for allocation plan responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllocationPlanResponseDto {

    private Long id;
    private Long yearId;
    private String yearName;
    private String planName;
    private String planVersion;
    private PlanStatus status;
    private String statusDisplayName;
    private Long createdByUserId;
    private String createdByUserName;
    private String createdByUserEmail;
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;
    private Boolean isCurrent;
    private String notes;
}
