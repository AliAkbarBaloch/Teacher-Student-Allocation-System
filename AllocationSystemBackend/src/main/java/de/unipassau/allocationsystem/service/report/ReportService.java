package de.unipassau.allocationsystem.service.report;

import de.unipassau.allocationsystem.dto.report.allocation.health.AllocationHealthReportDto;
import de.unipassau.allocationsystem.dto.report.allocation.health.BudgetMetric;
import de.unipassau.allocationsystem.dto.report.subject.SubjectBottleneckDto;
import de.unipassau.allocationsystem.dto.report.teacher.TeacherUtilizationReportDto;
import de.unipassau.allocationsystem.entity.*;
import de.unipassau.allocationsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final AllocationPlanRepository planRepository;
    private final TeacherAssignmentRepository assignmentRepository;
    private final InternshipDemandRepository demandRepository;
    private final TeacherRepository teacherRepository;
    private final TeacherAvailabilityRepository availabilityRepository;
    private final CreditHourTrackingRepository creditTrackingRepository;

    // ==========================================
    // 1. Health & Budget Report
    // ==========================================
    @Transactional(readOnly = true)
    public AllocationHealthReportDto generateHealthReport(Long planId) {
        AllocationPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));
        AcademicYear year = plan.getAcademicYear();

        // Fetch raw counts
        List<Object[]> counts = assignmentRepository.countAssignmentsBySchoolType(planId);

        // Logic: 2 Assignments = 1 Reduction Hour (0.5 hours per assignment)
        double elementaryUsed = 0.0;
        double middleUsed = 0.0;

        int totalAssigned = 0;

        for (Object[] result : counts) {
            School.SchoolType type = (School.SchoolType) result[0];
            Long count = (Long) result[1];
            totalAssigned += count;

            if (type == School.SchoolType.PRIMARY) {
                elementaryUsed += (count * 0.5);
            } else {
                middleUsed += (count * 0.5); // Summarize Middle, Secondary, etc. as Middle budget
            }
        }

        // Fetch Demands to calculate fulfillment
        List<InternshipDemand> demands = demandRepository.findByAcademicYearId(year.getId());
        int totalRequired = demands.stream().mapToInt(InternshipDemand::getRequiredTeachers).sum();
        int totalStudents = demands.stream().mapToInt(d -> d.getStudentCount() != null ? d.getStudentCount() : 0).sum();

        // Compliance Logic
        boolean isCompliant = (elementaryUsed <= year.getElementarySchoolHours() + 5) && // +5 flexibility
                (middleUsed <= year.getMiddleSchoolHours() + 5) &&
                ((elementaryUsed + middleUsed) <= year.getTotalCreditHours());

        String warning = isCompliant ? "None" : "Budget limits exceeded!";

        return AllocationHealthReportDto.builder()
                .planName(plan.getPlanName())
                .academicYear(year.getYearName())
                .status(plan.getStatus().name())
                .totalBudget(buildMetric((double) year.getTotalCreditHours(), elementaryUsed + middleUsed))
                .elementaryBudget(buildMetric((double) year.getElementarySchoolHours(), elementaryUsed))
                .middleSchoolBudget(buildMetric((double) year.getMiddleSchoolHours(), middleUsed))
                .totalStudentCount(totalStudents)
                .totalRequiredTeachers(totalRequired)
                .totalAssignedTeachers(totalAssigned)
                .fulfillmentPercentage(totalRequired > 0 ? ((double) totalAssigned / totalRequired) * 100 : 0)
                .isBudgetCompliant(isCompliant)
                .complianceWarning(warning)
                .build();
    }

    private BudgetMetric buildMetric(double allocated, double used) {
        return BudgetMetric.builder()
                .allocated(allocated)
                .used(used)
                .remaining(allocated - used)
                .build();
    }
}
