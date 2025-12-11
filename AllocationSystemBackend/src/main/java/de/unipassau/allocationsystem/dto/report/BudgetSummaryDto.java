package de.unipassau.allocationsystem.dto.report;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BudgetSummaryDto {
    private Double totalBudgetHours; // e.g., 210
    private Double usedHours;        // Calculated based on assignments (2 assignments = 1 hour)
    private Double remainingHours;
    private Double elementaryHoursUsed;
    private Double middleSchoolHoursUsed;
    private boolean isOverBudget;
}
