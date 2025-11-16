package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.TeacherAvailabilityCreateDto;
import de.unipassau.allocationsystem.dto.TeacherAvailabilityResponseDto;
import de.unipassau.allocationsystem.dto.TeacherAvailabilityUpdateDto;
import de.unipassau.allocationsystem.service.TeacherAvailabilityService;
import de.unipassau.allocationsystem.utils.ResponseHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for managing teacher availability records.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
@Slf4j
public class TeacherAvailabilityController {

    private final TeacherAvailabilityService teacherAvailabilityService;

    /**
     * GET /teachers/{teacherId}/availability
     * Get all availability entries for a teacher with optional filters and pagination.
     *
     * @param teacherId        Teacher ID (path parameter)
     * @param yearId           Optional academic year filter
     * @param internshipTypeId Optional internship type filter
     * @param queryParams      Pagination parameters (page, pageSize, sortBy, sortOrder)
     * @return Paginated list of availability entries
     */
    @GetMapping("/{teacherId}/availability")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getTeacherAvailability(
            @PathVariable Long teacherId,
            @RequestParam(required = false) Long yearId,
            @RequestParam(required = false) Long internshipTypeId,
            @RequestParam Map<String, String> queryParams) {

        log.info("GET /teachers/{}/availability called with filters - yearId: {}, internshipTypeId: {}, params: {}",
                teacherId, yearId, internshipTypeId, queryParams);

        Map<String, Object> result = teacherAvailabilityService
                .getTeacherAvailability(teacherId, yearId, internshipTypeId, queryParams);

        return ResponseHandler.success("Teacher availability retrieved successfully (paginated)", result);
    }

    /**
     * GET /teachers/{teacherId}/availability/{availabilityId}
     * Get a specific availability entry by ID.
     *
     * @param teacherId      Teacher ID (path parameter)
     * @param availabilityId Availability entry ID
     * @return Availability entry details
     */
    @GetMapping("/{teacherId}/availability/{availabilityId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAvailabilityById(
            @PathVariable Long teacherId,
            @PathVariable Long availabilityId) {

        log.info("GET /teachers/{}/availability/{} called", teacherId, availabilityId);

        TeacherAvailabilityResponseDto result = teacherAvailabilityService
                .getAvailabilityById(teacherId, availabilityId);

        return ResponseHandler.success("Availability entry retrieved successfully", result);
    }

    /**
     * POST /teachers/{teacherId}/availability
     * Create a new availability entry for a teacher.
     *
     * @param teacherId Teacher ID (path parameter)
     * @param createDto Request body with availability details
     * @return Created availability entry
     */
    @PostMapping("/{teacherId}/availability")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createAvailability(
            @PathVariable Long teacherId,
            @Valid @RequestBody TeacherAvailabilityCreateDto createDto) {

        log.info("POST /teachers/{}/availability called with data: {}", teacherId, createDto);

        TeacherAvailabilityResponseDto result = teacherAvailabilityService
                .createAvailability(teacherId, createDto);

        return ResponseHandler.created("Availability entry created successfully", result);
    }

    /**
     * PUT /teachers/{teacherId}/availability/{availabilityId}
     * Update an existing availability entry.
     *
     * @param teacherId      Teacher ID (path parameter)
     * @param availabilityId Availability entry ID
     * @param updateDto      Request body with fields to update
     * @return Updated availability entry
     */
    @PutMapping("/{teacherId}/availability/{availabilityId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateAvailability(
            @PathVariable Long teacherId,
            @PathVariable Long availabilityId,
            @Valid @RequestBody TeacherAvailabilityUpdateDto updateDto) {

        log.info("PUT /teachers/{}/availability/{} called with data: {}", teacherId, availabilityId, updateDto);

        TeacherAvailabilityResponseDto result = teacherAvailabilityService
                .updateAvailability(teacherId, availabilityId, updateDto);

        return ResponseHandler.updated("Availability entry updated successfully", result);
    }

    /**
     * DELETE /teachers/{teacherId}/availability/{availabilityId}
     * Delete an availability entry.
     *
     * @param teacherId      Teacher ID (path parameter)
     * @param availabilityId Availability entry ID
     * @return No content response
     */
    @DeleteMapping("/{teacherId}/availability/{availabilityId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteAvailability(
            @PathVariable Long teacherId,
            @PathVariable Long availabilityId) {

        log.info("DELETE /teachers/{}/availability/{} called", teacherId, availabilityId);

        teacherAvailabilityService.deleteAvailability(teacherId, availabilityId);

        return ResponseHandler.noContent();
    }
}
