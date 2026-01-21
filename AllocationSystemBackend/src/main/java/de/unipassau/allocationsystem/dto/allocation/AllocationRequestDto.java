package de.unipassau.allocationsystem.dto.allocation;

import lombok.Data;

/**
 * DTO for requesting a teacher allocation operation.
 * Contains metadata, algorithm settings, constraints, and optimization weights.
 */
@Data
public class AllocationRequestDto {
    // Metadata
    private Boolean isCurrent = false;
    private String planVersion;

    // Algorithm Settings
    private Boolean prioritizeScarcity = true;
    private Boolean forceUtilizationOfSurplus = true;
    private Boolean allowGroupSizeExpansion = true;

    // Constraints
    private Integer standardAssignmentsPerTeacher = 2;
    private Integer maxAssignmentsPerTeacher = 3;
    private Integer maxGroupSizeWednesday = 4;
    private Integer maxGroupSizeBlock = 2;

    // Optimization Weights
    private Integer weightMainSubject = 10;
    private Integer weightZonePreference = 5;
}