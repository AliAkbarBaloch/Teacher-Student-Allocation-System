package de.unipassau.allocationsystem.controller.report;

import de.unipassau.allocationsystem.dto.report.allocation.AllocationReportDto;
import de.unipassau.allocationsystem.service.report.AllocationReportService;
import de.unipassau.allocationsystem.utils.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * REST controller for generating allocation reports.
 * Provides endpoints for retrieving and exporting allocation plan statistics.
 */
@Slf4j
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Allocation Reports", description = "Generate statistics and final reports for allocation plans")
public class AllocationReportController {

    private final AllocationReportService reportService;

    /**
     * Retrieves the latest allocation report.
     * Generates a report for the current or most recent approved allocation plan.
     * 
     * @return ResponseEntity containing the allocation report
     */
    @Operation(summary = "Get Latest Allocation Report", description = "Generates a report for the current or most recent approved allocation plan.")
    @GetMapping("/allocation/latest")
    public ResponseEntity<?> getLatestAllocationReport() {
        AllocationReportDto report = reportService.generateReportForLatest();
        return ResponseHandler.success("Report generated successfully", report);
    }

    /**
     * Retrieves a full allocation report for a specific plan.
     * Generates a detailed report of assignments, budget usage, and teacher utilization.
     * 
     * @param planId The ID of the allocation plan
     * @return ResponseEntity containing the allocation report
     */
    @Operation(summary = "Get Full Allocation Report", description = "Generates a detailed report of assignments, budget usage, and teacher utilization.")
    @GetMapping("/allocation/{planId}")
    public ResponseEntity<?> getAllocationReport(@PathVariable Long planId) {
        AllocationReportDto report = reportService.generateReport(planId);
        return ResponseHandler.success("Report generated successfully", report);
    }

    /**
     * Exports allocation report as an Excel file.
     * Generates a downloadable Excel spreadsheet containing the allocation plan data.
     * 
     * @param planId The ID of the allocation plan to export
     * @return ResponseEntity containing the Excel file as byte array
     * @throws IOException if file generation fails
     */
    @GetMapping("/allocation-export/{planId}")
    public ResponseEntity<byte[]> exportExcel(@PathVariable Long planId) throws IOException {
        log.info("Generating Excel report for plan ID: {}", planId);
        byte[] excelContent = reportService.generateExcelReport(planId);
        log.info("Successfully generated Excel report for plan ID: {}, size: {} bytes", planId, excelContent.length);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=allocation_report_" + planId + ".xlsx")
                .body(excelContent);
    }
}