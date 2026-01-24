package de.unipassau.allocationsystem.service.report;

import de.unipassau.allocationsystem.dto.report.allocation.AllocationReportDto;
import de.unipassau.allocationsystem.dto.report.allocation.BudgetSummaryDto;
import de.unipassau.allocationsystem.dto.report.allocation.TeacherAssignmentDetailDto;
import de.unipassau.allocationsystem.dto.report.allocation.TeacherUtilizationDto;
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
import java.util.List;

/**
 * Service responsible for exporting allocation reports to Excel.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AllocationReportExcelService {

    private final AllocationReportService reportService;

    /**
     * Generates allocation report in Excel format.
     *
     * @param planId the allocation plan ID
     * @return Excel file as byte array
     * @throws IOException if Excel generation fails
     */
    @Transactional(readOnly = true)
    public byte[] generateExcelReport(Long planId) throws IOException {
        AllocationReportDto data = reportService.generateReport(planId);

        try (Workbook workbook = new XSSFWorkbook()) {
            writeAssignmentsSheet(workbook, data.getAssignments());
            writeBudgetSheet(workbook, data.getBudgetSummary());

            writeUtilizationSheet(
                    workbook,
                    "Unassigned Teachers",
                    data.getUtilizationAnalysis().getUnassignedTeachers()
            );

            writeUtilizationSheet(
                    workbook,
                    "Under-Utilized Teachers",
                    data.getUtilizationAnalysis().getUnderUtilizedTeachers()
            );

            writeUtilizationSheet(
                    workbook,
                    "Over-Utilized Teachers",
                    data.getUtilizationAnalysis().getOverUtilizedTeachers()
            );

            writeUtilizationSheet(
                    workbook,
                    "Perfectly Utilized Teachers",
                    data.getUtilizationAnalysis().getPerfectlyUtilizedTeachers()
            );

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private void writeAssignmentsSheet(Workbook workbook, List<TeacherAssignmentDetailDto> items) {
        Sheet sheet = workbook.createSheet("Assignments");
        String[] headers = {
                "Assignment ID", "Teacher Name", "Teacher Email", "School Name", "School Zone",
                "Internship Type", "Subject Code", "Student Group Size", "Assignment Status"
        };
        createHeaderRow(sheet, headers);

        int rowIdx = 1;
        for (TeacherAssignmentDetailDto item : items) {
            Row row = sheet.createRow(rowIdx++);
            writeString(row, 0, valueOf(item.getAssignmentId()));
            writeString(row, 1, item.getTeacherName());
            writeString(row, 2, safeString(item.getTeacherEmail()));
            writeString(row, 3, item.getSchoolName());
            writeString(row, 4, item.getSchoolZone());
            writeString(row, 5, item.getInternshipCode());
            writeString(row, 6, item.getSubjectCode());
            row.createCell(7).setCellValue(item.getStudentGroupSize());
            writeString(row, 8, item.getAssignmentStatus());
        }

        autoSize(sheet, headers.length);
    }

    private void writeBudgetSheet(Workbook workbook, BudgetSummaryDto budget) {
        Sheet sheet = workbook.createSheet("Budget Summary");
        createHeaderRow(sheet, new String[]{"Metric", "Value"});

        int rowIdx = 1;
        rowIdx = writeMetric(sheet, rowIdx, "Total Budget Hours", budget.getTotalBudgetHours());
        rowIdx = writeMetric(sheet, rowIdx, "Used Hours", budget.getUsedHours());
        rowIdx = writeMetric(sheet, rowIdx, "Remaining Hours", budget.getRemainingHours());
        rowIdx = writeMetric(sheet, rowIdx, "Elementary School Hours Used", budget.getElementaryHoursUsed());
        rowIdx = writeMetric(sheet, rowIdx, "Middle School Hours Used", budget.getMiddleSchoolHoursUsed());

        String overBudget = "NO";
        if (budget.isOverBudget()) {
            overBudget = "YES";
        }
        Row row = sheet.createRow(rowIdx);
        writeString(row, 0, "Over Budget");
        writeString(row, 1, overBudget);

        autoSize(sheet, 2);
    }

    private int writeMetric(Sheet sheet, int rowIdx, String metric, double value) {
        Row row = sheet.createRow(rowIdx++);
        writeString(row, 0, metric);
        row.createCell(1).setCellValue(value);
        return rowIdx;
    }

    private void writeUtilizationSheet(Workbook workbook, String sheetName, List<TeacherUtilizationDto> teachers) {
        Sheet sheet = workbook.createSheet(sheetName);
        String[] headers = {"Teacher ID", "Teacher Name", "Email", "School Name", "Assignment Count", "Notes"};
        createHeaderRow(sheet, headers);

        int rowIdx = 1;
        for (TeacherUtilizationDto teacher : teachers) {
            Row row = sheet.createRow(rowIdx++);
            writeString(row, 0, valueOf(teacher.getTeacherId()));
            writeString(row, 1, teacher.getTeacherName());
            writeString(row, 2, teacher.getEmail());
            writeString(row, 3, teacher.getSchoolName());
            row.createCell(4).setCellValue(teacher.getAssignmentCount());
            writeString(row, 5, safeString(teacher.getNotes()));
        }

        autoSize(sheet, headers.length);
    }

    private void createHeaderRow(Sheet sheet, String[] headers) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            header.createCell(i).setCellValue(headers[i]);
        }
    }

    private void autoSize(Sheet sheet, int columns) {
        for (int i = 0; i < columns; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void writeString(Row row, int col, String value) {
        row.createCell(col).setCellValue(value);
    }

    private String safeString(String s) {
        if (s == null) {
            return "";
        }
        return s;
    }

    private String valueOf(Object o) {
        if (o == null) {
            return "";
        }
        return String.valueOf(o);
    }
}
