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
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Allocation Reports", description = "Generate statistics and final reports for allocation plans")
public class AllocationReportController {

    private final AllocationReportService reportService;

    @Operation(summary = "Get Latest Allocation Report", description = "Generates a report for the current or most recent approved allocation plan.")
    @GetMapping("/allocation/latest")
    public ResponseEntity<?> getLatestAllocationReport() {
        AllocationReportDto report = reportService.generateReportForLatest();
        return ResponseHandler.success("Report generated successfully", report);
    }

    @Operation(summary = "Get Full Allocation Report", description = "Generates a detailed report of assignments, budget usage, and teacher utilization.")
    @GetMapping("/allocation/{planId}")
    public ResponseEntity<?> getAllocationReport(@PathVariable Long planId) {
        AllocationReportDto report = reportService.generateReport(planId);
        return ResponseHandler.success("Report generated successfully", report);
    }

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