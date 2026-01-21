package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.teacher.formsubmission.FormLinkResponseDto;
import de.unipassau.allocationsystem.dto.teacher.formsubmission.PublicFormSubmissionDto;
import de.unipassau.allocationsystem.dto.teacher.formsubmission.TeacherFormSubmissionResponseDto;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public REST controller for teacher form submissions.
 * These endpoints do not require authentication.
 */
@RestController
@RequestMapping("/public/teacher-form-submission")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Public Teacher Form", description = "Public APIs for teacher form submission (no authentication required)")
public class PublicTeacherFormController {

    private final TeacherFormSubmissionService teacherFormSubmissionService;

    /**
     * GET /public/teacher-form-submission/{token}
     * Get form details by token (for displaying form to teacher).
     *
     * @param token Form token
     * @return Form details with teacher and year information
     */
    @Operation(summary = "Get form details by token", description = "Retrieve form details using the form token (public endpoint)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Form details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = FormLinkResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid token or form already submitted"),
            @ApiResponse(responseCode = "404", description = "Form token not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{token}")
    public ResponseEntity<?> getFormDetailsByToken(@PathVariable String token) {
        log.info("GET /public/teacher-form-submission/{} called", token);

        FormLinkResponseDto result = teacherFormSubmissionService.getFormDetailsByToken(token);

        return ResponseHandler.success("Form details retrieved successfully", result);
    }

    /**
     * POST /public/teacher-form-submission/{token}
     * Submit a form using the form token (public endpoint, no authentication required).
     *
     * @param token Form token
     * @param submissionDto Submission data
     * @return Created form submission
     */
    @Operation(summary = "Submit form by token", description = "Submit a teacher form using the form token (public endpoint, no authentication required)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Form submitted successfully",
                    content = @Content(schema = @Schema(implementation = TeacherFormSubmissionResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or form already submitted"),
            @ApiResponse(responseCode = "404", description = "Form token not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{token}")
    public ResponseEntity<?> submitFormByToken(
            @PathVariable String token,
            @Valid @RequestBody PublicFormSubmissionDto submissionDto) {
        log.info("POST /public/teacher-form-submission/{} called", token);

        TeacherFormSubmissionResponseDto result = teacherFormSubmissionService.submitFormByToken(token, submissionDto);

        return ResponseHandler.created("Form submitted successfully", result);
    }
}


