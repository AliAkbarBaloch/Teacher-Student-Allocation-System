package de.unipassau.allocationsystem.controller.report;

import de.unipassau.allocationsystem.dto.report.allocation.health.AllocationHealthReportDto;
import de.unipassau.allocationsystem.dto.report.school.SchoolStatusReportDto;
import de.unipassau.allocationsystem.dto.report.subject.SubjectBottleneckDto;
import de.unipassau.allocationsystem.dto.report.teacher.TeacherStatusReportDto;
import de.unipassau.allocationsystem.dto.report.teacher.TeacherUtilizationReportDto;
import de.unipassau.allocationsystem.service.report.ReportService;
import de.unipassau.allocationsystem.service.report.SchoolReportService;
import de.unipassau.allocationsystem.service.report.TeacherReportService;
import de.unipassau.allocationsystem.utils.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Analytical reports for teachers and allocations")
public class ReportController {

    private final TeacherReportService teacherReportService;
    private final SchoolReportService schoolReportService;
    private final ReportService reportingService;

    @Operation(summary = "Get Teacher Status Report",
            description = "Returns aggregated metrics and detailed profiles for all teachers, optionally contextualized by an academic year.")
    @GetMapping("/teachers/status")
    public ResponseEntity<?> getTeacherStatusReport(
            @RequestParam(required = false) Long academicYearId) {

        TeacherStatusReportDto report = teacherReportService.generateTeacherStatusReport(academicYearId);
        return ResponseHandler.success("Teacher status report generated successfully", report);
    }

    @Operation(summary = "Get School Network Report",
            description = "Returns metrics on school distribution (type, zone, accessibility) and teacher capacity per school.")
    @GetMapping("/schools/status")
    public ResponseEntity<?> getSchoolStatusReport() {
        SchoolStatusReportDto report = schoolReportService.generateSchoolStatusReport();
        return ResponseHandler.success("School status report generated successfully", report);
    }

    @GetMapping("/plan/{planId}/health")
    @Operation(summary = "Get Plan Health & Budget Report", description = "Checks budget compliance and demand fulfillment.")
    public ResponseEntity<?> getHealthReport(@PathVariable Long planId) {
        return ResponseHandler.success("budget compliance and demand fulfillment generated successfully", reportingService.generateHealthReport(planId));
    }

    @GetMapping("/plan/{planId}/bottlenecks")
    @Operation(summary = "Get Subject Bottlenecks", description = "Analyzes supply vs demand for subjects (English, etc).")
    public ResponseEntity<?> getBottleneckReport(@PathVariable Long planId) {
        return ResponseHandler.success("Analyzes supply vs demand for subjects generated successfully", reportingService.generateBottleneckReport(planId));
    }

    @GetMapping("/plan/{planId}/utilization")
    @Operation(summary = "Get Teacher Utilization", description = "List of teachers, their workload, and credit balance.")
    public ResponseEntity<?> getUtilizationReport(@PathVariable Long planId) {
        return ResponseHandler.success("List of teachers, their workload, and credit balance.", reportingService.generateUtilizationReport(planId));
    }
}
