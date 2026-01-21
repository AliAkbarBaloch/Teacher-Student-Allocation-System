package de.unipassau.allocationsystem.dto.report.allocation.health;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for budget metrics in reduction hours.
 * Tracks allocated budget from ministry, actual usage from assignments, and remaining balance.
 */
@Data
@Builder
public class BudgetMetric {
    private double allocated; // From Ministry
    private double used;      // Calculated from assignments (2 assignments = 1 hour)
    private double remaining;
}
