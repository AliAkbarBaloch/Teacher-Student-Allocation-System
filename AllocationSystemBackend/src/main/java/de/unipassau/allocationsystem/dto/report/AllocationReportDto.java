package de.unipassau.allocationsystem.dto.report;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AllocationReportDto {
    private ReportHeaderDto header;
    private BudgetSummaryDto budgetSummary;
    private List<TeacherAssignmentDetailDto> assignments;
    private UtilizationAnalysisDto utilizationAnalysis;
}
