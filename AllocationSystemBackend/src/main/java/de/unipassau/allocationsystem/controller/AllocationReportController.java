package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.report.AllocationReportDto;
import de.unipassau.allocationsystem.service.AllocationReportService;
import de.unipassau.allocationsystem.utils.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Allocation Reports", description = "Generate statistics and final reports for allocation plans")
public class AllocationReportController {

    private final AllocationReportService reportService;

    @Operation(summary = "Get Full Allocation Report", description = "Generates a detailed report of assignments, budget usage, and teacher utilization.")
    @GetMapping("/allocation/{planId}")
    public ResponseEntity<?> getAllocationReport(@PathVariable Long planId) {
        AllocationReportDto report = reportService.generateReport(planId);
        return ResponseHandler.success("Report generated successfully", report);
    }

    @GetMapping(value = "/allocation/{planId}/export/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportExcel(@PathVariable Long planId) throws IOException {
        byte[] excelContent = reportService.generateExcelReport(planId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=allocation_report.xlsx")
                .body(excelContent);
    }
}