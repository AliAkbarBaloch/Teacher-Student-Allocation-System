package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.TeacherFormSubmissionCreateDto;
import de.unipassau.allocationsystem.dto.TeacherFormSubmissionResponseDto;
import de.unipassau.allocationsystem.dto.TeacherFormSubmissionStatusUpdateDto;
import de.unipassau.allocationsystem.service.TeacherFormSubmissionService;
import de.unipassau.allocationsystem.utils.ResponseHandler;
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
