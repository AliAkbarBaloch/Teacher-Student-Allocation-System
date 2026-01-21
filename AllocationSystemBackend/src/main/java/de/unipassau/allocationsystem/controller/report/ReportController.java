package de.unipassau.allocationsystem.controller.report;

import de.unipassau.allocationsystem.dto.report.school.SchoolStatusReportDto;
import de.unipassau.allocationsystem.dto.report.teacher.TeacherStatusReportDto;
import de.unipassau.allocationsystem.service.report.ReportService;
import de.unipassau.allocationsystem.service.report.SchoolReportService;
import de.unipassau.allocationsystem.service.report.TeacherReportService;
import de.unipassau.allocationsystem.utils.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for generating analytical reports.
 * Provides endpoints for teacher, school, and plan analysis reports.
 */
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Analytical reports for teachers and allocations")
public class ReportController {

    private final TeacherReportService teacherReportService;
    private final SchoolReportService schoolReportService;
    private final ReportService reportingService;

    /**
     * Retrieves teacher status report.
     * Returns aggregated metrics and detailed profiles for all teachers,
     * optionally contextualized by an academic year.
     * 
     * @param academicYearId Optional academic year ID for filtering
     * @return ResponseEntity containing the teacher status report
     */
    @Operation(summary = "Get Teacher Status Report",
            description = "Returns aggregated metrics and detailed profiles for all teachers, optionally contextualized by an academic year.")
    @GetMapping("/teachers/status")
    public ResponseEntity<?> getTeacherStatusReport(
            @RequestParam(required = false) Long academicYearId) {

        TeacherStatusReportDto report = teacherReportService.generateTeacherStatusReport(academicYearId);
        return ResponseHandler.success("Teacher status report generated successfully", report);
    }

    /**
     * Retrieves school network status report.
     * Returns metrics on school distribution (type, zone, accessibility) and teacher capacity per school.
     * 
     * @return ResponseEntity containing the school status report
     */
    @Operation(summary = "Get School Network Report",
            description = "Returns metrics on school distribution (type, zone, accessibility) and teacher capacity per school.")
    @GetMapping("/schools/status")
    public ResponseEntity<?> getSchoolStatusReport() {
        SchoolStatusReportDto report = schoolReportService.generateSchoolStatusReport();
        return ResponseHandler.success("School status report generated successfully", report);
    }

    /**
     * Retrieves plan health and budget compliance report.
     * Checks budget compliance and demand fulfillment for a specific allocation plan.
     * 
     * @param planId The ID of the allocation plan
     * @return ResponseEntity containing the health report
     */
    @GetMapping("/plan/{planId}/health")
    @Operation(summary = "Get Plan Health & Budget Report", description = "Checks budget compliance and demand fulfillment.")
    public ResponseEntity<?> getHealthReport(@PathVariable Long planId) {
        return ResponseHandler.success("budget compliance and demand fulfillment generated successfully", reportingService.generateHealthReport(planId));
    }

    /**
     * Retrieves subject bottleneck analysis report.
     * Analyzes supply vs demand for subjects to identify resource constraints.
     * 
     * @param planId The ID of the allocation plan
     * @return ResponseEntity containing the bottleneck report
     */
    @GetMapping("/plan/{planId}/bottlenecks")
    @Operation(summary = "Get Subject Bottlenecks", description = "Analyzes supply vs demand for subjects (English, etc).")
    public ResponseEntity<?> getBottleneckReport(@PathVariable Long planId) {
        return ResponseHandler.success("Analyzes supply vs demand for subjects generated successfully", reportingService.generateBottleneckReport(planId));
    }

    /**
     * Retrieves teacher utilization report.
     * Provides a list of teachers with their workload and credit balance.
     * 
     * @param planId The ID of the allocation plan
     * @return ResponseEntity containing the utilization report
     */
    @GetMapping("/plan/{planId}/utilization")
    @Operation(summary = "Get Teacher Utilization", description = "List of teachers, their workload, and credit balance.")
    public ResponseEntity<?> getUtilizationReport(@PathVariable Long planId) {
        return ResponseHandler.success("List of teachers, their workload, and credit balance.", reportingService.generateUtilizationReport(planId));
    }
}
