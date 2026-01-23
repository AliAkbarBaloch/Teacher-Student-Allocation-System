package de.unipassau.allocationsystem.dto.report.allocation;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * DTO for comprehensive allocation report.
 * Aggregates header, budget summary, assignments, and utilization analysis.
 */
@Data
@Builder
public class AllocationReportDto {
    private ReportHeaderDto header;
    private BudgetSummaryDto budgetSummary;
    private List<TeacherAssignmentDetailDto> assignments;
    private UtilizationAnalysisDto utilizationAnalysis;
}
