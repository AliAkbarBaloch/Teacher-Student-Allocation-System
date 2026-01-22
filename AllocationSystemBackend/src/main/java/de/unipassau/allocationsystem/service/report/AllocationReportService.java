package de.unipassau.allocationsystem.service.report;

import de.unipassau.allocationsystem.dto.report.allocation.AllocationReportDto;
import de.unipassau.allocationsystem.dto.report.allocation.BudgetSummaryDto;
import de.unipassau.allocationsystem.dto.report.allocation.ReportHeaderDto;
import de.unipassau.allocationsystem.dto.report.allocation.TeacherAssignmentDetailDto;
import de.unipassau.allocationsystem.dto.report.allocation.TeacherUtilizationDto;
import de.unipassau.allocationsystem.dto.report.allocation.UtilizationAnalysisDto;
import de.unipassau.allocationsystem.entity.AllocationPlan;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.TeacherAssignment;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.AllocationPlanRepository;
import de.unipassau.allocationsystem.repository.TeacherAssignmentRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for generating allocation reports.
 * Provides report generation for allocation plans including budget analysis and utilization metrics.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AllocationReportService {

    private final AllocationPlanRepository planRepository;
    private final TeacherAssignmentRepository assignmentRepository;
    private final TeacherRepository teacherRepository;

    @Transactional(readOnly = true)
    public AllocationReportDto generateReportForLatest() {
        List<AllocationPlan> recentPlans = planRepository.findMostRecentPlan();
        if (recentPlans.isEmpty()) {
            throw new ResourceNotFoundException("No allocation plans found");
        }

        AllocationPlan plan = recentPlans.get(0);

        log.info("Recent plans found (ordered by createdAt DESC, id DESC):");
        for (int i = 0; i < Math.min(5, recentPlans.size()); i++) {
            AllocationPlan p = recentPlans.get(i);
            log.info("  [{}] Plan ID: {}, Name: {}, Status: {}, IsCurrent: {}, CreatedAt: {}",
                    i, p.getId(), p.getPlanName(), p.getStatus(), p.getIsCurrent(), p.getCreatedAt());
        }

        log.info("Using most recent plan: {} (ID: {}, Status: {}, IsCurrent: {}, CreatedAt: {})",
                plan.getPlanName(), plan.getId(), plan.getStatus(), plan.getIsCurrent(), plan.getCreatedAt());

        return generateReportForPlan(plan);
    }

    @Transactional(readOnly = true)
    public AllocationReportDto generateReport(Long planId) {
        AllocationPlan plan = planRepository.findByIdWithAcademicYear(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));
        return generateReportForPlan(plan);
    }

    private AllocationReportDto generateReportForPlan(AllocationPlan plan) {
        log.info("Generating report for plan ID: {}, Name: {}, Status: {}",
                plan.getId(), plan.getPlanName(), plan.getStatus());

        List<TeacherAssignment> assignments = assignmentRepository.findAllByPlanIdWithDetails(plan.getId());
        log.info("Found {} assignments for plan ID: {}", assignments.size(), plan.getId());

        List<Teacher> allTeachers = teacherRepository.findAllByEmploymentStatus(Teacher.EmploymentStatus.ACTIVE);
        log.info("Found {} active teachers", allTeachers.size());

        List<TeacherAssignmentDetailDto> detailDtos = assignments.stream()
                .map(this::mapToDetailDto)
                .collect(Collectors.toList());

        BudgetSummaryDto budgetSummary = calculateBudget(plan, assignments);
        UtilizationAnalysisDto utilizationAnalysis = analyzeUtilization(allTeachers, assignments);

        String academicYearName = getAcademicYearName(plan);

        return AllocationReportDto.builder()
                .header(ReportHeaderDto.builder()
                        .planName(plan.getPlanName())
                        .planVersion(plan.getPlanVersion())
                        .academicYear(academicYearName)
                        .status(plan.getStatus().name())
                        .generatedAt(LocalDateTime.now())
                        .build())
                .assignments(detailDtos)
                .budgetSummary(budgetSummary)
                .utilizationAnalysis(utilizationAnalysis)
                .build();
    }

    private String getAcademicYearName(AllocationPlan plan) {
        String academicYearName = "Unknown";
        if (plan.getAcademicYear() != null && plan.getAcademicYear().getYearName() != null) {
            academicYearName = plan.getAcademicYear().getYearName();
        }
        return academicYearName;
    }

    // =========================
    // Shortened methods
    // =========================

    private TeacherAssignmentDetailDto mapToDetailDto(TeacherAssignment ta) {
        Teacher teacher = getTeacherOrNull(ta);

        return TeacherAssignmentDetailDto.builder()
                .assignmentId(getAssignmentIdOrNull(ta))
                .teacherName(buildTeacherName(teacher))
                .teacherEmail(getTeacherEmailOrNull(teacher))
                .schoolName(getSchoolNameOrUnknown(teacher))
                .schoolZone(getSchoolZoneOrUnknown(teacher))
                .internshipCode(getInternshipCodeOrUnknown(ta))
                .subjectCode(getSubjectCodeOrUnknown(ta))
                .studentGroupSize(getStudentGroupSizeOrZero(ta))
                .assignmentStatus(getAssignmentStatusOrUnknown(ta))
                .build();
    }

    private UtilizationAnalysisDto analyzeUtilization(List<Teacher> allTeachers, List<TeacherAssignment> assignments) {
        Map<Long, Long> assignmentCounts = buildAssignmentCounts(assignments);

        List<TeacherUtilizationDto> unassigned = new ArrayList<>();
        List<TeacherUtilizationDto> underUtilized = new ArrayList<>();
        List<TeacherUtilizationDto> overUtilized = new ArrayList<>();
        List<TeacherUtilizationDto> perfect = new ArrayList<>();

        for (Teacher teacher : allTeachers) {
            long count = assignmentCounts.getOrDefault(teacher.getId(), 0L);
            TeacherUtilizationDto dto = buildUtilizationDto(teacher, count);
            addToUtilizationBucket(dto, count, unassigned, underUtilized, perfect, overUtilized);
        }

        return UtilizationAnalysisDto.builder()
                .unassignedTeachers(unassigned)
                .underUtilizedTeachers(underUtilized)
                .perfectlyUtilizedTeachers(perfect)
                .overUtilizedTeachers(overUtilized)
                .build();
    }

    // =========================
    // Helper methods for mapToDetailDto
    // =========================

    private Teacher getTeacherOrNull(TeacherAssignment ta) {
        if (ta == null) {
            return null;
        }
        return ta.getTeacher();
    }

    private Long getAssignmentIdOrNull(TeacherAssignment ta) {
        if (ta == null) {
            return null;
        }
        return ta.getId();
    }

    private String getTeacherEmailOrNull(Teacher teacher) {
        if (teacher == null) {
            return null;
        }
        return teacher.getEmail();
    }

    private String getSchoolNameOrUnknown(Teacher teacher) {
        if (teacher == null || teacher.getSchool() == null) {
            return "Unknown";
        }
        if (teacher.getSchool().getSchoolName() == null) {
            return "Unknown";
        }
        return teacher.getSchool().getSchoolName();
    }

    private String getSchoolZoneOrUnknown(Teacher teacher) {
        if (teacher == null || teacher.getSchool() == null) {
            return "Unknown";
        }
        return "Zone " + teacher.getSchool().getZoneNumber();
    }

    private String getInternshipCodeOrUnknown(TeacherAssignment ta) {
        if (ta == null || ta.getInternshipType() == null) {
            return "Unknown";
        }
        if (ta.getInternshipType().getInternshipCode() == null) {
            return "Unknown";
        }
        return ta.getInternshipType().getInternshipCode();
    }

    private String getSubjectCodeOrUnknown(TeacherAssignment ta) {
        if (ta == null || ta.getSubject() == null) {
            return "Unknown";
        }
        if (ta.getSubject().getSubjectCode() == null) {
            return "Unknown";
        }
        return ta.getSubject().getSubjectCode();
    }

    private int getStudentGroupSizeOrZero(TeacherAssignment ta) {
        if (ta == null || ta.getStudentGroupSize() == null) {
            return 0;
        }
        return ta.getStudentGroupSize();
    }

    private String getAssignmentStatusOrUnknown(TeacherAssignment ta) {
        if (ta == null || ta.getAssignmentStatus() == null) {
            return "UNKNOWN";
        }
        return ta.getAssignmentStatus().name();
    }

    private String buildTeacherName(Teacher teacher) {
        if (teacher == null) {
            return "Unknown";
        }
        String last = teacher.getLastName();
        String first = teacher.getFirstName();

        StringBuilder sb = new StringBuilder();
        if (last != null) {
            sb.append(last);
        }
        sb.append(", ");
        if (first != null) {
            sb.append(first);
        }
        return sb.toString();
    }

    // =========================
    // Helper methods for analyzeUtilization
    // =========================

    private Map<Long, Long> buildAssignmentCounts(List<TeacherAssignment> assignments) {
        return assignments.stream()
                .filter(a -> a.getTeacher() != null && a.getTeacher().getId() != null)
                .collect(Collectors.groupingBy(a -> a.getTeacher().getId(), Collectors.counting()));
    }

    private TeacherUtilizationDto buildUtilizationDto(Teacher teacher, long count) {
        String teacherName = buildTeacherName(teacher);
        String schoolName = getSchoolNameOrUnknown(teacher);

        return TeacherUtilizationDto.builder()
                .teacherId(teacher.getId())
                .teacherName(teacherName)
                .email(teacher.getEmail())
                .schoolName(schoolName)
                .assignmentCount((int) count)
                .build();
    }

    private void addToUtilizationBucket(TeacherUtilizationDto dto,
                                        long count,
                                        List<TeacherUtilizationDto> unassigned,
                                        List<TeacherUtilizationDto> underUtilized,
                                        List<TeacherUtilizationDto> perfect,
                                        List<TeacherUtilizationDto> overUtilized) {
        if (count == 0) {
            dto.setNotes("Warning: Unused Resource");
            unassigned.add(dto);
            return;
        }
        if (count == 1) {
            dto.setNotes("Warning: Only 1 assignment (Needs 2 for credit)");
            underUtilized.add(dto);
            return;
        }
        if (count == 2) {
            perfect.add(dto);
            return;
        }
        dto.setNotes("Alert: Overloaded (" + count + " assignments)");
        overUtilized.add(dto);
    }

    // =========================
    // Existing logic (unchanged)
    // =========================

    private BudgetSummaryDto calculateBudget(AllocationPlan plan, List<TeacherAssignment> assignments) {
        double hoursUsed = assignments.size() * 0.5;

        long elementaryAssignments = assignments.stream()
                .filter(a -> a.getTeacher() != null
                        && a.getTeacher().getSchool() != null
                        && a.getTeacher().getSchool().getSchoolType() == School.SchoolType.PRIMARY)
                .count();

        long middleAssignments = assignments.stream()
                .filter(a -> a.getTeacher() != null
                        && a.getTeacher().getSchool() != null
                        && a.getTeacher().getSchool().getSchoolType() == School.SchoolType.MIDDLE)
                .count();

        double totalBudgetHours = 0.0;
        if (plan.getAcademicYear() != null && plan.getAcademicYear().getTotalCreditHours() != null) {
            totalBudgetHours = plan.getAcademicYear().getTotalCreditHours();
        }

        return BudgetSummaryDto.builder()
                .totalBudgetHours(totalBudgetHours)
                .usedHours(hoursUsed)
                .remainingHours(totalBudgetHours - hoursUsed)
                .elementaryHoursUsed(elementaryAssignments * 0.5)
                .middleSchoolHoursUsed(middleAssignments * 0.5)
                .isOverBudget(hoursUsed > totalBudgetHours)
                .build();
    }
}
