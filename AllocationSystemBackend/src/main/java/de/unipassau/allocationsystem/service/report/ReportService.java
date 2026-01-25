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
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.AllocationPlanRepository;
import de.unipassau.allocationsystem.repository.CreditHourTrackingRepository;
import de.unipassau.allocationsystem.repository.InternshipDemandRepository;
import de.unipassau.allocationsystem.repository.TeacherAssignmentRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for generating various allocation system reports.
 * Provides health reports, bottleneck analysis, and utilization reports.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final AllocationPlanRepository planRepository;
    private final TeacherAssignmentRepository assignmentRepository;
    private final InternshipDemandRepository demandRepository;
    private final TeacherRepository teacherRepository;
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
     * @throws ResourceNotFoundException if the plan does not exist
     */
    @Transactional(readOnly = true)
    public AllocationHealthReportDto generateHealthReport(Long planId) {
        AllocationPlan plan = getPlanOrThrow(planId);
        AcademicYear year = plan.getAcademicYear();

        UsageBySchoolType usage = calculateUsedHoursBySchoolType(planId);
        DemandTotals demandTotals = calculateDemandTotals(year);

        boolean compliant = isBudgetCompliant(year, usage.elementaryUsedHours, usage.middleUsedHours);
        String warning = compliant ? "None" : "Budget limits exceeded!";

        double fulfillmentPercentage = calculateFulfillmentPercentage(demandTotals.totalRequiredTeachers, usage.totalAssignedTeachers);

        return AllocationHealthReportDto.builder()
                .planName(plan.getPlanName())
                .academicYear(year.getYearName())
                .status(plan.getStatus().name())
                .totalBudget(buildMetric((double) year.getTotalCreditHours(), usage.elementaryUsedHours + usage.middleUsedHours))
                .elementaryBudget(buildMetric((double) year.getElementarySchoolHours(), usage.elementaryUsedHours))
                .middleSchoolBudget(buildMetric((double) year.getMiddleSchoolHours(), usage.middleUsedHours))
                .totalStudentCount(demandTotals.totalStudents)
                .totalRequiredTeachers(demandTotals.totalRequiredTeachers)
                .totalAssignedTeachers(usage.totalAssignedTeachers)
                .fulfillmentPercentage(fulfillmentPercentage)
                .isBudgetCompliant(compliant)
                .complianceWarning(warning)
                .build();
    }

    private AllocationPlan getPlanOrThrow(Long planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));
    }

    private UsageBySchoolType calculateUsedHoursBySchoolType(Long planId) {
        List<Object[]> counts = assignmentRepository.countAssignmentsBySchoolType(planId);

        double elementaryUsed = 0.0;
        double middleUsed = 0.0;
        int totalAssigned = 0;

        for (Object[] result : counts) {
            School.SchoolType type = (School.SchoolType) result[0];
            Long count = (Long) result[1];

            totalAssigned += count.intValue();

            double usedHours = count.doubleValue() * 0.5; // 2 assignments = 1 hour
            if (type == School.SchoolType.PRIMARY) {
                elementaryUsed += usedHours;
            } else {
                middleUsed += usedHours;
            }
        }

        return new UsageBySchoolType(elementaryUsed, middleUsed, totalAssigned);
    }

    private DemandTotals calculateDemandTotals(AcademicYear year) {
        List<InternshipDemand> demands = demandRepository.findByAcademicYearId(year.getId());

        int totalRequired = demands.stream()
                .mapToInt(InternshipDemand::getRequiredTeachers)
                .sum();

        int totalStudents = 0;
        for (InternshipDemand d : demands) {
            if (d.getStudentCount() != null) {
                totalStudents += d.getStudentCount();
            }
        }

        return new DemandTotals(totalRequired, totalStudents);
    }

    private boolean isBudgetCompliant(AcademicYear year, double elementaryUsed, double middleUsed) {
        double flexibility = 5.0;

        boolean elementaryOk = elementaryUsed <= year.getElementarySchoolHours() + flexibility;
        boolean middleOk = middleUsed <= year.getMiddleSchoolHours() + flexibility;
        boolean totalOk = (elementaryUsed + middleUsed) <= year.getTotalCreditHours();

        return elementaryOk && middleOk && totalOk;
    }

    private double calculateFulfillmentPercentage(int totalRequired, int totalAssigned) {
        if (totalRequired <= 0) {
            return 0.0;
        }
        return ((double) totalAssigned / (double) totalRequired) * 100.0;
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

    /**
     * Generates a bottleneck report for subject-specific internship demands (e.g., ZSP, SFP).
     * The report compares required teacher counts to available and actually assigned teachers per subject.
     *
     * @param planId the allocation plan ID
     * @return list of bottleneck DTOs sorted by the most critical gap first
     * @throws ResourceNotFoundException if the plan does not exist
     */
    @Transactional(readOnly = true)
    public List<SubjectBottleneckDto> generateBottleneckReport(Long planId) {
        AllocationPlan plan = getPlanOrThrow(planId);

        List<InternshipDemand> demands = demandRepository.findByAcademicYearId(plan.getAcademicYear().getId());
        List<Teacher> allTeachers = teacherRepository.findAll();
        List<TeacherAssignment> planAssignments = assignmentRepository.findByAllocationPlanId(planId);

        List<SubjectBottleneckDto> report = buildBottleneckDtos(plan, demands, allTeachers, planAssignments);

        report.sort(Comparator.comparingInt(SubjectBottleneckDto::getGap));
        return report;
    }

    private List<SubjectBottleneckDto> buildBottleneckDtos(AllocationPlan plan,
                                                           List<InternshipDemand> demands,
                                                           List<Teacher> allTeachers,
                                                           List<TeacherAssignment> planAssignments) {
        List<SubjectBottleneckDto> report = new ArrayList<>();

        for (InternshipDemand demand : demands) {
            if (!isSubjectSpecific(demand)) {
                continue;
            }

            long availableCount = countAvailableTeachersForDemand(plan, demand, allTeachers);
            long assignedCount = countAssignedTeachersForDemand(demand, planAssignments);

            int gap = (int) availableCount - demand.getRequiredTeachers();
            String status = determineBottleneckStatus(gap);

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

        return report;
    }

    private boolean isSubjectSpecific(InternshipDemand demand) {
        return demand.getInternshipType() != null && Boolean.TRUE.equals(demand.getInternshipType().getIsSubjectSpecific());
    }

    private long countAvailableTeachersForDemand(AllocationPlan plan, InternshipDemand demand, List<Teacher> allTeachers) {
        Long yearId = plan.getAcademicYear().getId();
        Long subjectId = demand.getSubject().getId();

        return allTeachers.stream()
                .filter(t -> t.getEmploymentStatus() == Teacher.EmploymentStatus.ACTIVE)
                .filter(t -> isQualifiedForSubject(t, subjectId))
                .filter(t -> isAvailableInYear(t, yearId))
                .count();
    }

    private boolean isQualifiedForSubject(Teacher teacher, Long subjectId) {
        if (teacher.getQualifications() == null) {
            return false;
        }
        return teacher.getQualifications().stream()
                .anyMatch(q -> q.getSubject() != null && subjectId.equals(q.getSubject().getId()));
    }

    private boolean isAvailableInYear(Teacher teacher, Long academicYearId) {
        if (teacher.getAvailabilities() == null) {
            return true;
        }
        return teacher.getAvailabilities().stream()
                .noneMatch(a -> a.getAcademicYear() != null
                        && academicYearId.equals(a.getAcademicYear().getId())
                        && a.getStatus() == TeacherAvailability.AvailabilityStatus.NOT_AVAILABLE);
    }

    private long countAssignedTeachersForDemand(InternshipDemand demand, List<TeacherAssignment> planAssignments) {
        Long subjectId = demand.getSubject().getId();

        return planAssignments.stream()
                .filter(ta -> ta.getSubject() != null && subjectId.equals(ta.getSubject().getId()))
                .count();
    }

    private String determineBottleneckStatus(int gap) {
        if (gap < 0) {
            return "CRITICAL_SHORTAGE";
        }
        if (gap < 5) {
            return "SHORTAGE";
        }
        if (gap > 10) {
            return "SURPLUS";
        }
        return "BALANCED";
    }

    // ==========================================
    // 3. Teacher Utilization Report
    // ==========================================

    /**
     * Generates a teacher utilization report showing assignment counts and credit balance for the plan's academic year.
     *
     * @param planId the allocation plan ID
     * @return list of teacher utilization report DTOs
     * @throws ResourceNotFoundException if the plan does not exist
     */
    @Transactional(readOnly = true)
    public List<TeacherUtilizationReportDto> generateUtilizationReport(Long planId) {
        AllocationPlan plan = getPlanOrThrow(planId);

        List<Teacher> allTeachers = teacherRepository.findAll();
        List<TeacherAssignment> assignments = assignmentRepository.findByAllocationPlanId(planId);

        Map<Long, Long> assignmentCounts = groupAssignmentCounts(assignments);
        Map<Long, Double> balanceMap = buildCreditBalanceMap(plan.getAcademicYear().getId());

        List<TeacherUtilizationReportDto> report = buildUtilizationDtos(allTeachers, assignmentCounts, balanceMap);

        report.sort(Comparator.comparing(TeacherUtilizationReportDto::getUtilizationStatus));
        return report;
    }

    private Map<Long, Long> groupAssignmentCounts(List<TeacherAssignment> assignments) {
        return assignments.stream()
                .filter(ta -> ta.getTeacher() != null && ta.getTeacher().getId() != null)
                .collect(Collectors.groupingBy(ta -> ta.getTeacher().getId(), Collectors.counting()));
    }

    private Map<Long, Double> buildCreditBalanceMap(Long academicYearId) {
        List<CreditHourTracking> creditTrackings = creditTrackingRepository.findByAcademicYearId(academicYearId);
        return creditTrackings.stream()
                .filter(ct -> ct.getTeacher() != null && ct.getTeacher().getId() != null)
                .collect(Collectors.toMap(ct -> ct.getTeacher().getId(), CreditHourTracking::getCreditBalance));
    }

    private List<TeacherUtilizationReportDto> buildUtilizationDtos(List<Teacher> allTeachers,
                                                                   Map<Long, Long> assignmentCounts,
                                                                   Map<Long, Double> balanceMap) {
        List<TeacherUtilizationReportDto> report = new ArrayList<>();

        for (Teacher teacher : allTeachers) {
            if (teacher.getEmploymentStatus() == Teacher.EmploymentStatus.ARCHIVED) {
                continue;
            }

            int count = assignmentCounts.getOrDefault(teacher.getId(), 0L).intValue();
            double balance = balanceMap.getOrDefault(teacher.getId(), 0.0);

            String status = determineUtilizationStatus(count);

            boolean unused = false;
            if (count == 0) {
                unused = true;
            }

            String teacherName = buildTeacherName(teacher);
            String schoolName = buildSchoolName(teacher);

            report.add(TeacherUtilizationReportDto.builder()
                    .teacherId(teacher.getId())
                    .teacherName(teacherName)
                    .schoolName(schoolName)
                    .assignmentsInCurrentPlan(count)
                    .currentCreditBalance(balance)
                    .utilizationStatus(status)
                    .isUnused(unused)
                    .build());
        }

        return report;
    }

    private String determineUtilizationStatus(int assignmentCount) {
        if (assignmentCount == 0) {
            return "UNUSED";
        }
        if (assignmentCount == 1) {
            return "UNDER_UTILIZED";
        }
        if (assignmentCount > 2) {
            return "OVER_UTILIZED";
        }
        return "OPTIMAL";
    }

    private String buildTeacherName(Teacher teacher) {
        String last = teacher.getLastName() != null ? teacher.getLastName() : "";
        String first = teacher.getFirstName() != null ? teacher.getFirstName() : "";
        return last + ", " + first;
    }

    private String buildSchoolName(Teacher teacher) {
        if (teacher.getSchool() == null) {
            return "Unknown";
        }
        if (teacher.getSchool().getSchoolName() == null) {
            return "Unknown";
        }
        return teacher.getSchool().getSchoolName();
    }

    // ==========================================
    // Internal helper records (simple holders)
    // ==========================================

    private static final class UsageBySchoolType {
        private final double elementaryUsedHours;
        private final double middleUsedHours;
        private final int totalAssignedTeachers;

        private UsageBySchoolType(double elementaryUsedHours, double middleUsedHours, int totalAssignedTeachers) {
            this.elementaryUsedHours = elementaryUsedHours;
            this.middleUsedHours = middleUsedHours;
            this.totalAssignedTeachers = totalAssignedTeachers;
        }
    }

    private static final class DemandTotals {
        private final int totalRequiredTeachers;
        private final int totalStudents;

        private DemandTotals(int totalRequiredTeachers, int totalStudents) {
            this.totalRequiredTeachers = totalRequiredTeachers;
            this.totalStudents = totalStudents;
        }
    }
}
