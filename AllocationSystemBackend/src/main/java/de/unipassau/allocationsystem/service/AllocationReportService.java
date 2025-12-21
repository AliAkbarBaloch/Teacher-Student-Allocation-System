package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.dto.report.allocation.*;
import de.unipassau.allocationsystem.entity.*;
import de.unipassau.allocationsystem.repository.*;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AllocationReportService {

    private final AllocationPlanRepository planRepository;
    private final TeacherAssignmentRepository assignmentRepository;
    private final TeacherRepository teacherRepository;

    @Transactional(readOnly = true)
    public AllocationReportDto generateReportForLatest() {
        // Get the most recently created plan (regardless of status)
        // This ensures newly created APPROVED plans from running the algorithm are selected
        List<AllocationPlan> recentPlans = planRepository.findMostRecentPlan();
        
        if (recentPlans.isEmpty()) {
            throw new ResourceNotFoundException("No allocation plans found");
        }
        
        AllocationPlan plan = recentPlans.get(0);
        
        // Log all recent plans for debugging
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
        // 1. Fetch Plan with eagerly loaded academic year
        AllocationPlan plan = planRepository.findByIdWithAcademicYear(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found: " + planId));
        return generateReportForPlan(plan);
    }

    private AllocationReportDto generateReportForPlan(AllocationPlan plan) {
        log.info("Generating report for plan ID: {}, Name: {}, Status: {}", 
                plan.getId(), plan.getPlanName(), plan.getStatus());

        // 2. Fetch Assignments
        List<TeacherAssignment> assignments = assignmentRepository.findAllByPlanIdWithDetails(plan.getId());
        log.info("Found {} assignments for plan ID: {}", assignments.size(), plan.getId());

        // 3. Fetch All Active Teachers (to find unassigned ones)
        List<Teacher> allTeachers = teacherRepository.findAllByEmploymentStatus(Teacher.EmploymentStatus.ACTIVE);
        log.info("Found {} active teachers", allTeachers.size());

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
        String teacherName = "Unknown";
        if (ta.getTeacher() != null) {
            teacherName = (ta.getTeacher().getLastName() != null ? ta.getTeacher().getLastName() : "")
                    + ", " + (ta.getTeacher().getFirstName() != null ? ta.getTeacher().getFirstName() : "");
        }

        return TeacherAssignmentDetailDto.builder()
                .assignmentId(ta.getId())
                .teacherName(teacherName)
                .teacherEmail(ta.getTeacher() != null ? ta.getTeacher().getEmail() : null)
                .schoolName(ta.getTeacher() != null && ta.getTeacher().getSchool() != null 
                        ? ta.getTeacher().getSchool().getSchoolName() : "Unknown")
                .schoolZone(ta.getTeacher() != null && ta.getTeacher().getSchool() != null 
                        ? "Zone " + ta.getTeacher().getSchool().getZoneNumber() : "Unknown")
                .internshipCode(ta.getInternshipType() != null ? ta.getInternshipType().getInternshipCode() : "Unknown")
                .subjectCode(ta.getSubject() != null ? ta.getSubject().getSubjectCode() : "Unknown")
                .studentGroupSize(ta.getStudentGroupSize() != null ? ta.getStudentGroupSize() : 0)
                .assignmentStatus(ta.getAssignmentStatus() != null ? ta.getAssignmentStatus().name() : "UNKNOWN")
                .build();
    }

    private BudgetSummaryDto calculateBudget(AllocationPlan plan, List<TeacherAssignment> assignments) {
        // Rule: 2 Assignments = 1 Credit Hour.
        // Therefore, 1 Assignment = 0.5 Credit Hours (Conceptually for calculation)

        double hoursUsed = assignments.size() * 0.5;

        // Split by School Type (Elementary vs Middle)
        long elementaryAssignments = assignments.stream()
                .filter(a -> a.getTeacher() != null && a.getTeacher().getSchool() != null 
                        && a.getTeacher().getSchool().getSchoolType() == School.SchoolType.PRIMARY)
                .count();

        long middleAssignments = assignments.stream()
                .filter(a -> a.getTeacher() != null && a.getTeacher().getSchool() != null 
                        && a.getTeacher().getSchool().getSchoolType() == School.SchoolType.MIDDLE)
                .count();

        // Safely get total credit hours with default value
        double totalBudgetHours = plan.getAcademicYear() != null && plan.getAcademicYear().getTotalCreditHours() != null 
                ? plan.getAcademicYear().getTotalCreditHours() : 0.0;

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
        // Group assignments by Teacher ID - filter out null teachers for safety
        Map<Long, Long> assignmentCounts = assignments.stream()
                .filter(a -> a.getTeacher() != null && a.getTeacher().getId() != null)
                .collect(Collectors.groupingBy(a -> a.getTeacher().getId(), Collectors.counting()));

        List<TeacherUtilizationDto> unassigned = new ArrayList<>();
        List<TeacherUtilizationDto> underUtilized = new ArrayList<>();
        List<TeacherUtilizationDto> overUtilized = new ArrayList<>();
        List<TeacherUtilizationDto> perfect = new ArrayList<>();

        for (Teacher teacher : allTeachers) {
            long count = assignmentCounts.getOrDefault(teacher.getId(), 0L);

            String teacherName = (teacher.getLastName() != null ? teacher.getLastName() : "")
                    + ", " + (teacher.getFirstName() != null ? teacher.getFirstName() : "");
            String schoolName = teacher.getSchool() != null ? teacher.getSchool().getSchoolName() : "Unknown";

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

    @Transactional(readOnly = true)
    public byte[] generateExcelReport(Long planId) throws IOException {
        AllocationReportDto data = generateReport(planId);

        try (Workbook workbook = new XSSFWorkbook()) {
            // Sheet 1: Assignments Details
            Sheet assignmentsSheet = workbook.createSheet("Assignments");
            Row assignmentHeader = assignmentsSheet.createRow(0);
            assignmentHeader.createCell(0).setCellValue("Assignment ID");
            assignmentHeader.createCell(1).setCellValue("Teacher Name");
            assignmentHeader.createCell(2).setCellValue("Teacher Email");
            assignmentHeader.createCell(3).setCellValue("School Name");
            assignmentHeader.createCell(4).setCellValue("School Zone");
            assignmentHeader.createCell(5).setCellValue("Internship Type");
            assignmentHeader.createCell(6).setCellValue("Subject Code");
            assignmentHeader.createCell(7).setCellValue("Student Group Size");
            assignmentHeader.createCell(8).setCellValue("Assignment Status");

            int rowIdx = 1;
            for (TeacherAssignmentDetailDto item : data.getAssignments()) {
                Row row = assignmentsSheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(item.getAssignmentId());
                row.createCell(1).setCellValue(item.getTeacherName());
                row.createCell(2).setCellValue(item.getTeacherEmail() != null ? item.getTeacherEmail() : "");
                row.createCell(3).setCellValue(item.getSchoolName());
                row.createCell(4).setCellValue(item.getSchoolZone());
                row.createCell(5).setCellValue(item.getInternshipCode());
                row.createCell(6).setCellValue(item.getSubjectCode());
                row.createCell(7).setCellValue(item.getStudentGroupSize());
                row.createCell(8).setCellValue(item.getAssignmentStatus());
            }

            // Auto-size columns
            for (int i = 0; i < 9; i++) {
                assignmentsSheet.autoSizeColumn(i);
            }

            // Sheet 2: Budget Summary
            Sheet budgetSheet = workbook.createSheet("Budget Summary");
            Row budgetHeader = budgetSheet.createRow(0);
            budgetHeader.createCell(0).setCellValue("Metric");
            budgetHeader.createCell(1).setCellValue("Value");

            int budgetRowIdx = 1;
            BudgetSummaryDto budget = data.getBudgetSummary();
            
            budgetSheet.createRow(budgetRowIdx++).createCell(0).setCellValue("Total Budget Hours");
            budgetSheet.getRow(budgetRowIdx - 1).createCell(1).setCellValue(budget.getTotalBudgetHours());
            
            budgetSheet.createRow(budgetRowIdx++).createCell(0).setCellValue("Used Hours");
            budgetSheet.getRow(budgetRowIdx - 1).createCell(1).setCellValue(budget.getUsedHours());
            
            budgetSheet.createRow(budgetRowIdx++).createCell(0).setCellValue("Remaining Hours");
            budgetSheet.getRow(budgetRowIdx - 1).createCell(1).setCellValue(budget.getRemainingHours());
            
            budgetSheet.createRow(budgetRowIdx++).createCell(0).setCellValue("Elementary School Hours Used");
            budgetSheet.getRow(budgetRowIdx - 1).createCell(1).setCellValue(budget.getElementaryHoursUsed());
            
            budgetSheet.createRow(budgetRowIdx++).createCell(0).setCellValue("Middle School Hours Used");
            budgetSheet.getRow(budgetRowIdx - 1).createCell(1).setCellValue(budget.getMiddleSchoolHoursUsed());
            
            budgetSheet.createRow(budgetRowIdx++).createCell(0).setCellValue("Over Budget");
            budgetSheet.getRow(budgetRowIdx - 1).createCell(1).setCellValue(budget.isOverBudget() ? "YES" : "NO");

            budgetSheet.autoSizeColumn(0);
            budgetSheet.autoSizeColumn(1);

            // Sheet 3: Unassigned Teachers
            Sheet unassignedSheet = workbook.createSheet("Unassigned Teachers");
            Row unassignedHeader = unassignedSheet.createRow(0);
            unassignedHeader.createCell(0).setCellValue("Teacher ID");
            unassignedHeader.createCell(1).setCellValue("Teacher Name");
            unassignedHeader.createCell(2).setCellValue("Email");
            unassignedHeader.createCell(3).setCellValue("School Name");
            unassignedHeader.createCell(4).setCellValue("Assignment Count");
            unassignedHeader.createCell(5).setCellValue("Notes");

            int unassignedRowIdx = 1;
            for (TeacherUtilizationDto teacher : data.getUtilizationAnalysis().getUnassignedTeachers()) {
                Row row = unassignedSheet.createRow(unassignedRowIdx++);
                row.createCell(0).setCellValue(teacher.getTeacherId());
                row.createCell(1).setCellValue(teacher.getTeacherName());
                row.createCell(2).setCellValue(teacher.getEmail());
                row.createCell(3).setCellValue(teacher.getSchoolName());
                row.createCell(4).setCellValue(teacher.getAssignmentCount());
                row.createCell(5).setCellValue(teacher.getNotes() != null ? teacher.getNotes() : "");
            }

            for (int i = 0; i < 6; i++) {
                unassignedSheet.autoSizeColumn(i);
            }

            // Sheet 4: Under-Utilized Teachers (Only 1 assignment)
            Sheet underUtilizedSheet = workbook.createSheet("Under-Utilized Teachers");
            Row underHeader = underUtilizedSheet.createRow(0);
            underHeader.createCell(0).setCellValue("Teacher ID");
            underHeader.createCell(1).setCellValue("Teacher Name");
            underHeader.createCell(2).setCellValue("Email");
            underHeader.createCell(3).setCellValue("School Name");
            underHeader.createCell(4).setCellValue("Assignment Count");
            underHeader.createCell(5).setCellValue("Notes");

            int underRowIdx = 1;
            for (TeacherUtilizationDto teacher : data.getUtilizationAnalysis().getUnderUtilizedTeachers()) {
                Row row = underUtilizedSheet.createRow(underRowIdx++);
                row.createCell(0).setCellValue(teacher.getTeacherId());
                row.createCell(1).setCellValue(teacher.getTeacherName());
                row.createCell(2).setCellValue(teacher.getEmail());
                row.createCell(3).setCellValue(teacher.getSchoolName());
                row.createCell(4).setCellValue(teacher.getAssignmentCount());
                row.createCell(5).setCellValue(teacher.getNotes() != null ? teacher.getNotes() : "");
            }

            for (int i = 0; i < 6; i++) {
                underUtilizedSheet.autoSizeColumn(i);
            }

            // Sheet 5: Over-Utilized Teachers (More than 2 assignments)
            Sheet overUtilizedSheet = workbook.createSheet("Over-Utilized Teachers");
            Row overHeader = overUtilizedSheet.createRow(0);
            overHeader.createCell(0).setCellValue("Teacher ID");
            overHeader.createCell(1).setCellValue("Teacher Name");
            overHeader.createCell(2).setCellValue("Email");
            overHeader.createCell(3).setCellValue("School Name");
            overHeader.createCell(4).setCellValue("Assignment Count");
            overHeader.createCell(5).setCellValue("Notes");

            int overRowIdx = 1;
            for (TeacherUtilizationDto teacher : data.getUtilizationAnalysis().getOverUtilizedTeachers()) {
                Row row = overUtilizedSheet.createRow(overRowIdx++);
                row.createCell(0).setCellValue(teacher.getTeacherId());
                row.createCell(1).setCellValue(teacher.getTeacherName());
                row.createCell(2).setCellValue(teacher.getEmail());
                row.createCell(3).setCellValue(teacher.getSchoolName());
                row.createCell(4).setCellValue(teacher.getAssignmentCount());
                row.createCell(5).setCellValue(teacher.getNotes() != null ? teacher.getNotes() : "");
            }

            for (int i = 0; i < 6; i++) {
                overUtilizedSheet.autoSizeColumn(i);
            }

            // Sheet 6: Perfectly Utilized Teachers (Exactly 2 assignments)
            Sheet perfectSheet = workbook.createSheet("Perfectly Utilized Teachers");
            Row perfectHeader = perfectSheet.createRow(0);
            perfectHeader.createCell(0).setCellValue("Teacher ID");
            perfectHeader.createCell(1).setCellValue("Teacher Name");
            perfectHeader.createCell(2).setCellValue("Email");
            perfectHeader.createCell(3).setCellValue("School Name");
            perfectHeader.createCell(4).setCellValue("Assignment Count");
            perfectHeader.createCell(5).setCellValue("Notes");

            int perfectRowIdx = 1;
            for (TeacherUtilizationDto teacher : data.getUtilizationAnalysis().getPerfectlyUtilizedTeachers()) {
                Row row = perfectSheet.createRow(perfectRowIdx++);
                row.createCell(0).setCellValue(teacher.getTeacherId());
                row.createCell(1).setCellValue(teacher.getTeacherName());
                row.createCell(2).setCellValue(teacher.getEmail());
                row.createCell(3).setCellValue(teacher.getSchoolName());
                row.createCell(4).setCellValue(teacher.getAssignmentCount());
                row.createCell(5).setCellValue(teacher.getNotes() != null ? teacher.getNotes() : "");
            }

            for (int i = 0; i < 6; i++) {
                perfectSheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }
}