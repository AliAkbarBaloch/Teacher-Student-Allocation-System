package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.teacher.formsubmission.TeacherFormSubmissionCreateDto;
import de.unipassau.allocationsystem.dto.teacher.formsubmission.TeacherFormSubmissionResponseDto;
import de.unipassau.allocationsystem.dto.teacher.formsubmission.TeacherFormSubmissionStatusUpdateDto;
import de.unipassau.allocationsystem.service.TeacherFormSubmissionService;
import de.unipassau.allocationsystem.utils.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for managing teacher form submissions.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/teacher-form-submissions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "TeacherFormSubmissions", description = "Teacher form submission management APIs")
public class TeacherFormSubmissionController {

    private final TeacherFormSubmissionService teacherFormSubmissionService;

    /**
     * GET /teacher-form-submissions
     * List all form submissions with optional filters and pagination.
     *
     * @param teacherId  Optional teacher filter
     * @param yearId     Optional academic year filter
     * @param isProcessed Optional processing status filter
     * @param queryParams Pagination parameters (page, pageSize, sortBy, sortOrder)
     * @return Paginated list of form submissions
     */
        @Operation(summary = "List form submissions", description = "List teacher form submissions with optional filters and pagination")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Form submissions retrieved successfully",
                content = @Content(schema = @Schema(implementation = TeacherFormSubmissionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        @GetMapping
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> getFormSubmissions(
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long yearId,
            @RequestParam(required = false) Boolean isProcessed,
            @RequestParam Map<String, String> queryParams) {

        log.info("GET /teacher-form-submissions called with filters - teacherId: {}, yearId: {}, isProcessed: {}",
                teacherId, yearId, isProcessed);

        Map<String, Object> result = teacherFormSubmissionService
                .getFormSubmissions(teacherId, yearId, isProcessed, queryParams);

        return ResponseHandler.success("Form submissions retrieved successfully (paginated)", result);
    }

    /**
     * GET /teacher-form-submissions/{id}
     * Get a specific form submission by ID.
     *
     * @param id Form submission ID
     * @return Form submission details
     */
        @Operation(summary = "Get form submission by ID", description = "Retrieve a specific form submission by its ID")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Form submission found",
                content = @Content(schema = @Schema(implementation = TeacherFormSubmissionResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Form submission not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        @GetMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> getFormSubmissionById(@PathVariable Long id) {
        log.info("GET /teacher-form-submissions/{} called", id);

        TeacherFormSubmissionResponseDto result = teacherFormSubmissionService.getFormSubmissionById(id);

        return ResponseHandler.success("Form submission retrieved successfully", result);
    }

    /**
     * POST /teacher-form-submissions
     * Create a new form submission.
     *
     * @param createDto Request body with submission details
     * @return Created form submission
     */
        @Operation(summary = "Create form submission", description = "Create a new teacher form submission")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Form submission created",
                content = @Content(schema = @Schema(implementation = TeacherFormSubmissionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        @PostMapping
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> createFormSubmission(@Valid @RequestBody TeacherFormSubmissionCreateDto createDto) {
        log.info("POST /teacher-form-submissions called with data: {}", createDto);

        TeacherFormSubmissionResponseDto result = teacherFormSubmissionService.createFormSubmission(createDto);

        return ResponseHandler.created("Form submission created successfully", result);
    }

    /**
     * PATCH /teacher-form-submissions/{id}/status
     * Update the processing status of a form submission.
     *
     * @param id        Form submission ID
     * @param statusDto Request body with new status
     * @return Updated form submission
     */
        @Operation(summary = "Update form submission status", description = "Update the processing status of a form submission")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Form submission status updated",
                content = @Content(schema = @Schema(implementation = TeacherFormSubmissionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Form submission not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        @PatchMapping("/{id}/status")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> updateFormSubmissionStatus(
            @PathVariable Long id,
            @Valid @RequestBody TeacherFormSubmissionStatusUpdateDto statusDto) {

        log.info("PATCH /teacher-form-submissions/{}/status called with status: {}", id, statusDto.getIsProcessed());

        TeacherFormSubmissionResponseDto result = teacherFormSubmissionService
                .updateFormSubmissionStatus(id, statusDto);

        return ResponseHandler.updated("Form submission status updated successfully", result);
    }
}
