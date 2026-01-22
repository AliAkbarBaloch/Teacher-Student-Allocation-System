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

    /**
     * Generates allocation report for the most recently created plan.
     *
     * @return allocation report DTO
     * @throws ResourceNotFoundException if no plans exist
     */
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

    /**
     * Generates allocation report for a specific plan.
     *
     * @param planId the allocation plan ID
     * @return allocation report DTO
     * @throws ResourceNotFoundException if plan not found
     */
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

        String academicYearName = "Unknown";
        if (plan.getAcademicYear() != null && plan.getAcademicYear().getYearName() != null) {
            academicYearName = plan.getAcademicYear().getYearName();
        }

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

    private TeacherAssignmentDetailDto mapToDetailDto(TeacherAssignment ta) {
        String teacherName = buildTeacherName(ta != null ? ta.getTeacher() : null);

        String teacherEmail = null;
        String schoolName = "Unknown";
        String schoolZone = "Unknown";

        if (ta != null && ta.getTeacher() != null) {
            teacherEmail = ta.getTeacher().getEmail();

            if (ta.getTeacher().getSchool() != null) {
                School s = ta.getTeacher().getSchool();
                if (s.getSchoolName() != null) {
                    schoolName = s.getSchoolName();
                }
                schoolZone = "Zone " + s.getZoneNumber();
            }
        }

        String internshipCode = "Unknown";
        if (ta != null && ta.getInternshipType() != null && ta.getInternshipType().getInternshipCode() != null) {
            internshipCode = ta.getInternshipType().getInternshipCode();
        }

        String subjectCode = "Unknown";
        if (ta != null && ta.getSubject() != null && ta.getSubject().getSubjectCode() != null) {
            subjectCode = ta.getSubject().getSubjectCode();
        }

        int groupSize = 0;
        if (ta != null && ta.getStudentGroupSize() != null) {
            groupSize = ta.getStudentGroupSize();
        }

        String assignmentStatus = "UNKNOWN";
        if (ta != null && ta.getAssignmentStatus() != null) {
            assignmentStatus = ta.getAssignmentStatus().name();
        }

        return TeacherAssignmentDetailDto.builder()
                .assignmentId(ta != null ? ta.getId() : null)
                .teacherName(teacherName)
                .teacherEmail(teacherEmail)
                .schoolName(schoolName)
                .schoolZone(schoolZone)
                .internshipCode(internshipCode)
                .subjectCode(subjectCode)
                .studentGroupSize(groupSize)
                .assignmentStatus(assignmentStatus)
                .build();
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

    private UtilizationAnalysisDto analyzeUtilization(List<Teacher> allTeachers, List<TeacherAssignment> assignments) {
        Map<Long, Long> assignmentCounts = assignments.stream()
                .filter(a -> a.getTeacher() != null && a.getTeacher().getId() != null)
                .collect(Collectors.groupingBy(a -> a.getTeacher().getId(), Collectors.counting()));

        List<TeacherUtilizationDto> unassigned = new ArrayList<>();
        List<TeacherUtilizationDto> underUtilized = new ArrayList<>();
        List<TeacherUtilizationDto> overUtilized = new ArrayList<>();
        List<TeacherUtilizationDto> perfect = new ArrayList<>();

        for (Teacher teacher : allTeachers) {
            long count = assignmentCounts.getOrDefault(teacher.getId(), 0L);

            String teacherName = buildTeacherName(teacher);
            String schoolName = "Unknown";
            if (teacher.getSchool() != null && teacher.getSchool().getSchoolName() != null) {
                schoolName = teacher.getSchool().getSchoolName();
            }

            TeacherUtilizationDto dto = TeacherUtilizationDto.builder()
                    .teacherId(teacher.getId())
                    .teacherName(teacherName)
                    .email(teacher.getEmail())
                    .schoolName(schoolName)
                    .assignmentCount((int) count)
                    .build();

            if (count == 0) {
                dto.setNotes("Warning: Unused Resource");
                unassigned.add(dto);
            } else if (count == 1) {
                dto.setNotes("Warning: Only 1 assignment (Needs 2 for credit)");
                underUtilized.add(dto);
            } else if (count == 2) {
                perfect.add(dto);
            } else {
                dto.setNotes("Alert: Overloaded (" + count + " assignments)");
                overUtilized.add(dto);
            }
        }

        return UtilizationAnalysisDto.builder()
                .unassignedTeachers(unassigned)
                .underUtilizedTeachers(underUtilized)
                .perfectlyUtilizedTeachers(perfect)
                .overUtilizedTeachers(overUtilized)
                .build();
    }
}
