package de.unipassau.allocationsystem.controller.report;

import de.unipassau.allocationsystem.dto.report.school.SchoolStatusReportDto;
import de.unipassau.allocationsystem.dto.report.teacher.TeacherStatusReportDto;
import de.unipassau.allocationsystem.service.report.SchoolReportService;
import de.unipassau.allocationsystem.service.report.TeacherReportService;
import de.unipassau.allocationsystem.utils.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Analytical reports for teachers and allocations")
public class ReportController {

    private final TeacherReportService teacherReportService;
    private final SchoolReportService schoolReportService;

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


}
