package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.report.teacher.TeacherStatusReportDto;
import de.unipassau.allocationsystem.service.TeacherReportService;
import de.unipassau.allocationsystem.utils.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Analytical reports for teachers and allocations")
public class ReportController {

    private final TeacherReportService teacherReportService;

    @Operation(summary = "Get Teacher Status Report",
            description = "Returns aggregated metrics and detailed profiles for all teachers, optionally contextualized by an academic year.")
    @GetMapping("/teachers/status")
    public ResponseEntity<?> getTeacherStatusReport(
            @RequestParam(required = false) Long academicYearId) {

        TeacherStatusReportDto report = teacherReportService.generateTeacherStatusReport(academicYearId);
        return ResponseHandler.success("Teacher status report generated successfully", report);
    }
}
