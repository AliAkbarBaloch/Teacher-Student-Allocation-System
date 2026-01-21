package de.unipassau.allocationsystem.dto.report.allocation.health;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for allocation health report.
 * Provides comprehensive metrics on budget compliance, teacher assignments, and fulfillment rates.
 */
@Data
@Builder
public class AllocationHealthReportDto {
    private String planName;
    private String academicYear;
    private String status;

    // Budget Metrics (in Reduction Hours)
    private BudgetMetric totalBudget;
    private BudgetMetric elementaryBudget;
    private BudgetMetric middleSchoolBudget;

    // Fulfillment Metrics
    private int totalStudentCount;
    private int totalRequiredTeachers;
    private int totalAssignedTeachers;
    private double fulfillmentPercentage;

    private boolean isBudgetCompliant;
    private String complianceWarning;
}
