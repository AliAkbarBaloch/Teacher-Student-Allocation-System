package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.teacherassignment.TeacherAssignmentCreateDto;
import de.unipassau.allocationsystem.dto.teacherassignment.TeacherAssignmentResponseDto;
import de.unipassau.allocationsystem.dto.teacherassignment.TeacherAssignmentUpdateDto;
import de.unipassau.allocationsystem.service.TeacherAssignmentService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Objects;


@RestController
@RequestMapping("/allocation-plans/{planId}/assignments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Teacher Assignments", description = "Manage teacher assignments within allocation plans")
public class TeacherAssignmentController {

    private final TeacherAssignmentService assignmentService;

        @Operation(summary = "List assignments for a plan", description = "Retrieves a paginated list of assignments for the given allocation plan. Requires ADMIN role.")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assignments retrieved successfully", content = @Content(schema = @Schema(implementation = Object.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        @GetMapping
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> list(
            @PathVariable Long planId,
            @RequestParam(value = "teacherId", required = false) Long teacherId,
            @RequestParam(value = "internshipTypeId", required = false) Long internshipTypeId,
            @RequestParam(value = "subjectId", required = false) Long subjectId,
            @RequestParam(value = "assignmentStatus", required = false) String assignmentStatus,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "ASC") String sortDirection
    ) {
        String dir = Objects.requireNonNullElse(sortDirection, "ASC");
        Pageable pageable = PageRequest.of(page, pageSize, Sort.Direction.fromString(Objects.requireNonNull(dir)), sortBy);
        Page<?> result = assignmentService.listByPlan(planId, teacherId, internshipTypeId, subjectId, assignmentStatus, pageable);
        return ResponseHandler.success("Assignments retrieved successfully", result);
    }

    @Operation(summary = "Get single assignment", description = "Retrieves a single teacher assignment by id for the provided allocation plan.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assignment found", content = @Content(schema = @Schema(implementation = de.unipassau.allocationsystem.dto.teacherassignment.TeacherAssignmentResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Assignment not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getById(@PathVariable Long planId, @PathVariable Long id) {
        TeacherAssignmentResponseDto dto = assignmentService.getById(id);
        return ResponseHandler.success("Assignment retrieved successfully", dto);
    }

    @Operation(summary = "Create assignment", description = "Creates a new teacher assignment under the specified allocation plan. ADMIN only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Assignment created successfully", content = @Content(schema = @Schema(implementation = de.unipassau.allocationsystem.dto.teacherassignment.TeacherAssignmentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate assignment"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@PathVariable Long planId, @Valid @RequestBody TeacherAssignmentCreateDto dto) {
        TeacherAssignmentResponseDto created = assignmentService.create(planId, dto, true);
        return ResponseHandler.created("Assignment created successfully", created);
    }

    @Operation(summary = "Update assignment", description = "Updates fields of an existing teacher assignment. ADMIN only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Assignment updated successfully", content = @Content(schema = @Schema(implementation = de.unipassau.allocationsystem.dto.teacherassignment.TeacherAssignmentResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Assignment not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long planId, @PathVariable Long id, @Valid @RequestBody TeacherAssignmentUpdateDto dto) {
        TeacherAssignmentResponseDto updated = assignmentService.update(planId, id, dto, true);
        return ResponseHandler.updated("Assignment updated successfully", updated);
    }

    @Operation(summary = "Delete assignment", description = "Deletes an assignment if plan status allows. ADMIN only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Assignment deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Assignment not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long planId, @PathVariable Long id) {
        assignmentService.delete(planId, id, true);
        return ResponseHandler.noContent();
    }
}
