package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.dto.report.*;
import de.unipassau.allocationsystem.entity.*;
import de.unipassau.allocationsystem.repository.*;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AllocationReportService {

    private final AllocationPlanRepository planRepository;
    private final TeacherAssignmentRepository assignmentRepository;
    private final TeacherRepository teacherRepository;

    @Transactional(readOnly = true)
    public AllocationReportDto generateReport(Long planId) {
        // 1. Fetch Plan
        AllocationPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));

        // 2. Fetch Assignments
        List<TeacherAssignment> assignments = assignmentRepository.findAllByPlanIdWithDetails(planId);

        // 3. Fetch All Active Teachers (to find unassigned ones)
        // Assuming you have a method to find active teachers linked to the plan's year context
        // For simplicity, fetching all active teachers here.
        List<Teacher> allTeachers = teacherRepository.findAllByIsActiveTrue();

        // 4. Build Assignment Details List
        List<TeacherAssignmentDetailDto> detailDtos = assignments.stream()
                .map(this::mapToDetailDto)
                .collect(Collectors.toList());

        // 5. Calculate Budget Stats
        BudgetSummaryDto budgetSummary = calculateBudget(plan, assignments);

        // 6. Analyze Utilization (The "2 assignments" rule)
        UtilizationAnalysisDto utilizationAnalysis = analyzeUtilization(allTeachers, assignments);

        // 7. Assemble Report
        return AllocationReportDto.builder()
                .header(ReportHeaderDto.builder()
                        .planName(plan.getPlanName())
                        .planVersion(plan.getPlanVersion())
                        .academicYear(plan.getAcademicYear().getYearName())
                        .status(plan.getStatus().name())
                        .generatedAt(LocalDateTime.now())
                        .build())
                .assignments(detailDtos)
                .budgetSummary(budgetSummary)
                .utilizationAnalysis(utilizationAnalysis)
                .build();
    }

    private TeacherAssignmentDetailDto mapToDetailDto(TeacherAssignment ta) {
        return TeacherAssignmentDetailDto.builder()
                .assignmentId(ta.getId())
                .teacherName(ta.getTeacher().getLastName() + ", " + ta.getTeacher().getFirstName())
                .schoolName(ta.getTeacher().getSchool().getSchoolName())
                .schoolZone("Zone " + ta.getTeacher().getSchool().getZoneNumber())
                .internshipCode(ta.getInternshipType().getInternshipCode())
                .subjectCode(ta.getSubject().getSubjectCode())
                .studentGroupSize(ta.getStudentGroupSize())
                .assignmentStatus(ta.getAssignmentStatus().name())
                .build();
    }

    private BudgetSummaryDto calculateBudget(AllocationPlan plan, List<TeacherAssignment> assignments) {
        // Rule: 2 Assignments = 1 Credit Hour.
        // Therefore, 1 Assignment = 0.5 Credit Hours (Conceptually for calculation)

        double hoursUsed = assignments.size() * 0.5;

        // Split by School Type (Elementary vs Middle)
        long elementaryAssignments = assignments.stream()
                .filter(a -> a.getTeacher().getSchool().getSchoolType() == School.SchoolType.PRIMARY)
                .count();

        long middleAssignments = assignments.stream()
                .filter(a -> a.getTeacher().getSchool().getSchoolType() == School.SchoolType.MIDDLE)
                .count();

        return BudgetSummaryDto.builder()
                .totalBudgetHours((double) plan.getAcademicYear().getTotalCreditHours())
                .usedHours(hoursUsed)
                .remainingHours(plan.getAcademicYear().getTotalCreditHours() - hoursUsed)
                .elementaryHoursUsed(elementaryAssignments * 0.5)
                .middleSchoolHoursUsed(middleAssignments * 0.5)
                .isOverBudget(hoursUsed > plan.getAcademicYear().getTotalCreditHours())
                .build();
    }

    private UtilizationAnalysisDto analyzeUtilization(List<Teacher> allTeachers, List<TeacherAssignment> assignments) {
        // Group assignments by Teacher ID
        Map<Long, Long> assignmentCounts = assignments.stream()
                .collect(Collectors.groupingBy(a -> a.getTeacher().getId(), Collectors.counting()));

        List<TeacherUtilizationDto> unassigned = new ArrayList<>();
        List<TeacherUtilizationDto> underUtilized = new ArrayList<>();
        List<TeacherUtilizationDto> overUtilized = new ArrayList<>();
        List<TeacherUtilizationDto> perfect = new ArrayList<>();

        for (Teacher teacher : allTeachers) {
            long count = assignmentCounts.getOrDefault(teacher.getId(), 0L);

            TeacherUtilizationDto dto = TeacherUtilizationDto.builder()
                    .teacherId(teacher.getId())
                    .teacherName(teacher.getLastName() + ", " + teacher.getFirstName())
                    .email(teacher.getEmail())
                    .schoolName(teacher.getSchool().getSchoolName())
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

    public byte[] generateExcelReport(Long planId) throws IOException {
        AllocationReportDto data = generateReport(planId);

        try (Workbook workbook = new XSSFWorkbook()) {
            // Sheet 1: Assignments
            Sheet sheet = workbook.createSheet("Assignments");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Teacher");
            header.createCell(1).setCellValue("School");
            header.createCell(2).setCellValue("Internship");
            header.createCell(3).setCellValue("Subject");

            int rowIdx = 1;
            for (TeacherAssignmentDetailDto item : data.getAssignments()) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(item.getTeacherName());
                row.createCell(1).setCellValue(item.getSchoolName());
                row.createCell(2).setCellValue(item.getInternshipCode());
                row.createCell(3).setCellValue(item.getSubjectCode());
            }

            // Sheet 2: Discrepancies (Unassigned/Overloaded)
            Sheet warningsSheet = workbook.createSheet("Warnings");
            // ... fill logic similar to above using data.getUtilizationAnalysis()

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }
}