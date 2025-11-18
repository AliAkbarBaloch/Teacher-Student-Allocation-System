package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.teacher.TeacherCreateDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherResponseDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherStatusUpdateDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherUpdateDto;
import de.unipassau.allocationsystem.entity.Teacher.EmploymentStatus;
import de.unipassau.allocationsystem.service.TeacherService;
import de.unipassau.allocationsystem.utils.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for Teacher management endpoints.
 */
@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Teacher Management", description = "APIs for managing teachers")
@SecurityRequirement(name = "Bearer Authentication")
public class TeacherController {

    private final TeacherService teacherService;

    /**
     * Get all teachers with optional filters and pagination.
     * 
     * @param page             Page number (1-based)
     * @param pageSize         Number of items per page
     * @param sortBy           Field to sort by
     * @param sortOrder        Sort direction (asc/desc)
     * @param schoolId         Optional filter by school ID
     * @param employmentStatus Optional filter by employment status
     * @param isActive         Optional filter by active status
     * @param search           Optional text search by name or email (case-insensitive, partial match)
     * @return Paginated list of teachers
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all teachers", description = "Get paginated list of teachers with optional filters (requires ADMIN role)")
    public ResponseEntity<?> getAllTeachers(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) EmploymentStatus employmentStatus,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) String search) {
        
        log.info("GET /teachers - Fetching teachers with filters");

        Map<String, String> queryParams = new HashMap<>();
        if (page != null) {
            queryParams.put("page", String.valueOf(page));
        }
        if (pageSize != null) {
            queryParams.put("pageSize", String.valueOf(pageSize));
        }
        if (sortBy != null) {
            queryParams.put("sortBy", sortBy);
        }
        if (sortOrder != null) {
            queryParams.put("sortOrder", sortOrder);
        }
        if (schoolId != null) {
            queryParams.put("schoolId", String.valueOf(schoolId));
        }
        if (employmentStatus != null) {
            queryParams.put("employmentStatus", employmentStatus.name());
        }
        if (isActive != null) {
            queryParams.put("isActive", String.valueOf(isActive));
        }
        if (search != null) {
            queryParams.put("search", search);
        }

        Map<String, Object> result = teacherService.getAllTeachers(queryParams);
        return ResponseHandler.success("Teachers retrieved successfully (paginated)", result);
    }

    /**
     * Get teacher by ID.
     * 
     * @param id Teacher ID
     * @return Teacher details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get teacher by ID", description = "Retrieve a specific teacher by ID (requires ADMIN role)")
    public ResponseEntity<?> getTeacherById(@PathVariable Long id) {
        log.info("GET /teachers/{} - Fetching teacher", id);
        
        Optional<TeacherResponseDto> teacher = teacherService.getById(id);
        return ResponseHandler.success("Teacher retrieved successfully", teacher.get());
    }

    /**
     * Create a new teacher.
     * Requires ADMIN role.
     * 
     * @param createDto Teacher creation data
     * @return Created teacher
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create teacher", description = "Create a new teacher (requires ADMIN role)")
    public ResponseEntity<?> createTeacher(@Valid @RequestBody TeacherCreateDto createDto) {
        log.info("POST /teachers - Creating new teacher");
        
        TeacherResponseDto created = teacherService.createTeacher(createDto);
        return ResponseHandler.created("Teacher created successfully", created);
    }

    /**
     * Update an existing teacher.
     * Requires ADMIN role.
     * 
     * @param id        Teacher ID
     * @param updateDto Teacher update data
     * @return Updated teacher
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update teacher", description = "Update an existing teacher (requires ADMIN role)")
    public ResponseEntity<?> updateTeacher(
            @PathVariable Long id,
            @Valid @RequestBody TeacherUpdateDto updateDto) {
        
        log.info("PUT /teachers/{} - Updating teacher", id);
        TeacherResponseDto updatedTeacher = teacherService.updateTeacher(id, updateDto);
        return ResponseHandler.updated("Teacher updated successfully", updatedTeacher);
    }

    /**
     * Update teacher status (activate/deactivate).
     * Requires ADMIN role.
     * 
     * @param id        Teacher ID
     * @param statusDto Status update data
     * @return Updated teacher
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update teacher status", description = "Activate or deactivate a teacher (requires ADMIN role)")
    public ResponseEntity<?> updateTeacherStatus(
            @PathVariable Long id,
            @Valid @RequestBody TeacherStatusUpdateDto statusDto) {
        
        log.info("PATCH /teachers/{}/status - Setting isActive to {}", id, statusDto.getIsActive());
        TeacherResponseDto updatedTeacher = teacherService.updateTeacherStatus(id, statusDto.getIsActive());
        return ResponseHandler.updated("Teacher status updated successfully", updatedTeacher);
    }

    /**
     * Soft delete teacher (deactivate).
     * Requires ADMIN role.
     * 
     * @param id Teacher ID
     * @return No content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete teacher", description = "Soft delete (deactivate) a teacher (requires ADMIN role)")
    public ResponseEntity<?> deleteTeacher(@PathVariable Long id) {
        log.info("DELETE /teachers/{} - Soft deleting teacher", id);
        
        teacherService.deleteTeacher(id);
        return ResponseHandler.noContent();
    }
}
