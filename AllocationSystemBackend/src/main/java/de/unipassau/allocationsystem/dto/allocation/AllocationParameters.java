package de.unipassau.allocationsystem.dto.allocation;

import lombok.Builder;
import lombok.Data;

/**
 * Configuration parameters for the teacher allocation algorithm.
 * Defines hard constraints, group sizing limits, optimization weights, and allocation strategies.
 */
@Data
@Builder
public class AllocationParameters {
    // Hard constraints
    @Builder.Default private int standardAssignmentsPerTeacher = 2;
    @Builder.Default private int maxAssignmentsPerTeacher = 3; // For emergencies/debt coverage

    // Group Sizing
    @Builder.Default private int maxGroupSizeWednesday = 4;
    @Builder.Default private int maxGroupSizeBlock = 2;
    @Builder.Default private boolean allowGroupSizeExpansion = true;

    // Optimization Weights (for scoring candidates)
    @Builder.Default private int weightMainSubject = 10;
    @Builder.Default private int weightZonePreference = 5;
    @Builder.Default private int weightPreferenceMatch = 3;

    // Strategy
    // If true, we prioritize filling subjects that have very few qualified teachers
    @Builder.Default private boolean prioritizeScarcity = true;

    // If true, we force assignments to unused teachers at the end (usually to PDP)
    @Builder.Default private boolean forceUtilizationOfSurplus = true;
}
