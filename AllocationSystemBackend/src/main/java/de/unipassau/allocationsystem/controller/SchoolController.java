package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.SchoolCreateDto;
import de.unipassau.allocationsystem.dto.SchoolResponseDto;
import de.unipassau.allocationsystem.dto.SchoolStatusUpdateDto;
import de.unipassau.allocationsystem.dto.SchoolUpdateDto;
import de.unipassau.allocationsystem.entity.School.SchoolType;
import de.unipassau.allocationsystem.service.SchoolService;
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

import java.util.Map;

/**
 * REST controller for School management endpoints.
 */
@RestController
@RequestMapping("/schools")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "School Management", description = "APIs for managing schools")
@SecurityRequirement(name = "Bearer Authentication")
public class SchoolController {

    private final SchoolService schoolService;

    /**
     * Get all schools with optional filters and pagination.
     * 
     * @param queryParams  Query parameters for pagination (page, pageSize, sortBy, sortOrder)
     * @param search       Optional text search by school name (case-insensitive, partial match)
     * @param schoolType   Optional filter by school type
     * @param zoneNumber   Optional filter by zone number
     * @param isActive     Optional filter by active status
     * @return Paginated list of schools
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all schools", description = "Get paginated list of schools with optional filters (requires ADMIN role)")
    public ResponseEntity<?> getAllSchools(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) SchoolType schoolType,
            @RequestParam(required = false) Integer zoneNumber,
            @RequestParam(required = false) Boolean isActive) {
        
        log.info("GET /schools - search: {}, type: {}, zone: {}, active: {}", search, schoolType, zoneNumber, isActive);
        
        // Build queryParams map for PaginationUtils
        java.util.Map<String, String> queryParams = new java.util.HashMap<>();
        if (page != null) queryParams.put("page", String.valueOf(page));
        if (pageSize != null) queryParams.put("pageSize", String.valueOf(pageSize));
        if (sortBy != null) queryParams.put("sortBy", sortBy);
        if (sortOrder != null) queryParams.put("sortOrder", sortOrder);
        
        Map<String, Object> result = schoolService.getAllSchools(queryParams, search, schoolType, zoneNumber, isActive);
        return ResponseHandler.success("Schools retrieved successfully (paginated)", result);
    }

    /**
     * Get single school by ID.
     * 
     * @param id School ID
     * @return School details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get school by ID", description = "Retrieve detailed information about a specific school (requires ADMIN role)")
    public ResponseEntity<?> getSchoolById(@PathVariable Long id) {
        log.info("GET /schools/{}", id);
        SchoolResponseDto school = schoolService.getSchoolById(id);
        return ResponseHandler.success("School retrieved successfully", school);
    }

    /**
     * Create a new school.
     * Requires SCHOOL_MANAGE permission.
     * 
     * @param createDto School creation data
     * @return Created school
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new school", description = "Create a new school (requires ADMIN role)")
    public ResponseEntity<?> createSchool(@Valid @RequestBody SchoolCreateDto createDto) {
        log.info("POST /schools - Creating school: {}", createDto.getSchoolName());
        SchoolResponseDto createdSchool = schoolService.createSchool(createDto);
        return ResponseHandler.created("School created successfully", createdSchool);
    }

    /**
     * Update an existing school.
     * Requires SCHOOL_MANAGE permission.
     * 
     * @param id        School ID
     * @param updateDto School update data
     * @return Updated school
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update school", description = "Update an existing school (requires ADMIN role)")
    public ResponseEntity<?> updateSchool(
            @PathVariable Long id,
            @Valid @RequestBody SchoolUpdateDto updateDto) {
        
        log.info("PUT /schools/{} - Updating school", id);
        SchoolResponseDto updatedSchool = schoolService.updateSchool(id, updateDto);
        return ResponseHandler.updated("School updated successfully", updatedSchool);
    }

    /**
     * Update school status (activate/deactivate).
     * Requires SCHOOL_MANAGE permission.
     * 
     * @param id        School ID
     * @param statusDto Status update data
     * @return Updated school
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update school status", description = "Activate or deactivate a school (requires ADMIN role)")
    public ResponseEntity<?> updateSchoolStatus(
            @PathVariable Long id,
            @Valid @RequestBody SchoolStatusUpdateDto statusDto) {
        
        log.info("PATCH /schools/{}/status - Setting isActive to {}", id, statusDto.getIsActive());
        SchoolResponseDto updatedSchool = schoolService.updateSchoolStatus(id, statusDto.getIsActive());
        return ResponseHandler.updated("School status updated successfully", updatedSchool);
    }

    /**
     * Soft delete school (deactivate).
     * Requires SCHOOL_MANAGE permission.
     * 
     * @param id School ID
     * @return No content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete school", description = "Soft delete a school by deactivating it (requires ADMIN role)")
    public ResponseEntity<?> deleteSchool(@PathVariable Long id) {
        log.info("DELETE /schools/{} - Deactivating school", id);
        schoolService.deleteSchool(id);
        return ResponseHandler.noContent();
    }
}
