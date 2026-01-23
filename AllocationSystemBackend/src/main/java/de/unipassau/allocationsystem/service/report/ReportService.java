package de.unipassau.allocationsystem.service.report;

import de.unipassau.allocationsystem.dto.report.allocation.health.AllocationHealthReportDto;
import de.unipassau.allocationsystem.dto.report.allocation.health.BudgetMetric;
import de.unipassau.allocationsystem.dto.report.subject.SubjectBottleneckDto;
import de.unipassau.allocationsystem.dto.report.teacher.TeacherUtilizationReportDto;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.AllocationPlan;
import de.unipassau.allocationsystem.entity.CreditHourTracking;
import de.unipassau.allocationsystem.entity.InternshipDemand;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.TeacherAssignment;
import de.unipassau.allocationsystem.entity.TeacherAvailability;
import de.unipassau.allocationsystem.repository.AllocationPlanRepository;
import de.unipassau.allocationsystem.repository.CreditHourTrackingRepository;
import de.unipassau.allocationsystem.repository.InternshipDemandRepository;
import de.unipassau.allocationsystem.repository.TeacherAssignmentRepository;
import de.unipassau.allocationsystem.repository.TeacherAvailabilityRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
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
/**
 * Service for generating various allocation system reports.
 * Provides health reports, bottleneck analysis, and utilization reports.
 */
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
    /**
     * Generates a health report for an allocation plan.
     * Includes budget metrics and allocation status.
     * 
     * @param planId the allocation plan ID
     * @return health report DTO
     */
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

    // ==========================================
    // 2. Subject Bottleneck Report
    // ==========================================
    @Transactional(readOnly = true)
    public List<SubjectBottleneckDto> generateBottleneckReport(Long planId) {
        AllocationPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        List<InternshipDemand> demands = demandRepository.findByAcademicYearId(plan.getAcademicYear().getId());

        // Fetch all teachers active in this year
        List<Teacher> allTeachers = teacherRepository.findAll();
        // Note: In a real large DB, optimize this fetching. Here we fetch all to filter in memory for complex logic.

        List<SubjectBottleneckDto> report = new ArrayList<>();

        for (InternshipDemand demand : demands) {
                        // Only subject-specific internships matter for bottlenecks (ZSP, SFP)
                        // Block internships (PDP) usually don't have strict subject requirements in allocation
                        if (!demand.getInternshipType().getIsSubjectSpecific()) {
                                continue;
                        }

            // 1. Calculate Available Teachers for this subject
            long availableCount = allTeachers.stream()
                    .filter(t -> t.getEmploymentStatus() == Teacher.EmploymentStatus.ACTIVE)
                    // Check if qualified
                    .filter(t -> t.getQualifications().stream()
                            .anyMatch(q -> q.getSubject().getId().equals(demand.getSubject().getId())))
                    // Check if available this year (not marked "nicht")
                    .filter(t -> t.getAvailabilities().stream()
                            .noneMatch(a -> a.getAcademicYear().getId().equals(plan.getAcademicYear().getId())
                                    && a.getStatus() == TeacherAvailability.AvailabilityStatus.NOT_AVAILABLE))
                    .count();

            // 2. Calculate Actual Assignments in this plan
            // (This requires a repository method to count assignments by subject and plan)
            // Simulating count for brevity:
            long assignedCount = assignmentRepository.findByAllocationPlanId(planId).stream()
                    .filter(ta -> ta.getSubject().getId().equals(demand.getSubject().getId()))
                    .count();

            int gap = (int) availableCount - demand.getRequiredTeachers();

                String status = "BALANCED";
                if (gap < 0) {
                        status = "CRITICAL_SHORTAGE";
                } else if (gap < 5) {
                        status = "SHORTAGE"; // Low buffer
                } else if (gap > 10) {
                        status = "SURPLUS";
                }

            report.add(SubjectBottleneckDto.builder()
                    .subjectName(demand.getSubject().getSubjectTitle())
                    .schoolType(demand.getSchoolType().name())
                    .requiredTeacherCount(demand.getRequiredTeachers())
                    .availableTeacherCount((int) availableCount)
                    .actuallyAssignedCount((int) assignedCount)
                    .gap(gap)
                    .status(status)
                    .build());
        }

        // Sort by most critical first
        report.sort(Comparator.comparingInt(SubjectBottleneckDto::getGap));
        return report;
    }

    // ==========================================
    // 3. Teacher Utilization Report
    // ==========================================
    /**
     * Generates a teacher utilization report showing assignment counts and available hours.
     * 
     * @param planId the allocation plan ID
     * @return list of teacher utilization report DTOs
     */
    @Transactional(readOnly = true)
    public List<TeacherUtilizationReportDto> generateUtilizationReport(Long planId) {
        AllocationPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        List<Teacher> allTeachers = teacherRepository.findAll();
        List<TeacherAssignment> assignments = assignmentRepository.findByAllocationPlanId(planId);

        // Group assignments by Teacher
        Map<Long, Long> assignmentCounts = assignments.stream()
                .collect(Collectors.groupingBy(ta -> ta.getTeacher().getId(), Collectors.counting()));

        // Fetch Credit Tracking for this year
        List<CreditHourTracking> creditTrackings = creditTrackingRepository.findByAcademicYearId(plan.getAcademicYear().getId());
        Map<Long, Double> balanceMap = creditTrackings.stream()
                .collect(Collectors.toMap(ct -> ct.getTeacher().getId(), CreditHourTracking::getCreditBalance));

        List<TeacherUtilizationReportDto> report = new ArrayList<>();

        for (Teacher teacher : allTeachers) {

                        // Skip archived teachers
                        if (teacher.getEmploymentStatus() == Teacher.EmploymentStatus.ARCHIVED) {
                                continue;
                        }

            int count = assignmentCounts.getOrDefault(teacher.getId(), 0L).intValue();
            double balance = balanceMap.getOrDefault(teacher.getId(), 0.0);

            String status = "OPTIMAL";
                        if (count == 0) {
                                status = "UNUSED";
                        } else if (count == 1) {
                                status = "UNDER_UTILIZED"; // Need 2 for 1 hour
                        } else if (count > 2) {
                                status = "OVER_UTILIZED";
                        }

            report.add(TeacherUtilizationReportDto.builder()
                    .teacherId(teacher.getId())
                    .teacherName(teacher.getLastName() + ", " + teacher.getFirstName())
                    .schoolName(teacher.getSchool().getSchoolName())
                    .assignmentsInCurrentPlan(count)
                    .currentCreditBalance(balance)
                    .utilizationStatus(status)
                    .isUnused(count == 0)
                    .build());
        }

        // Sort: Unused first, then Over-utilized
        report.sort(Comparator.comparing(TeacherUtilizationReportDto::getUtilizationStatus));
        return report;
    }
}
