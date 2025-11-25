package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.teacher.availability.TeacherAvailabilityCreateDto;
import de.unipassau.allocationsystem.dto.teacher.availability.TeacherAvailabilityResponseDto;
import de.unipassau.allocationsystem.dto.teacher.availability.TeacherAvailabilityUpdateDto;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.service.TeacherAvailabilityService;
import de.unipassau.allocationsystem.utils.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/teacher-availability")
@RequiredArgsConstructor
@Tag(name = "TeacherAvailability", description = "Teacher availability management APIs (Permission-style)")
public class TeacherAvailabilityController {

    private final TeacherAvailabilityService teacherAvailabilityService;

    @Operation(summary = "Get sort fields", description = "Retrieves available fields that can be used for sorting teacher availability")
    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        List<Map<String, String>> result = List.of(
                Map.of("key", "availabilityId", "label", "Availability ID"),
                Map.of("key", "teacherId", "label", "Teacher ID"),
                Map.of("key", "yearId", "label", "Year ID"),
                Map.of("key", "internshipTypeId", "label", "Internship Type ID"),
                Map.of("key", "isAvailable", "label", "Is Available"),
                Map.of("key", "preferenceRank", "label", "Preference Rank"),
                Map.of("key", "createdAt", "label", "Created At"),
                Map.of("key", "updatedAt", "label", "Updated At")
        );
        return ResponseHandler.success("Sort fields retrieved successfully", result);
    }

    @Operation(summary = "Get availability by ID", description = "Retrieves a specific teacher availability entry by its ID. Requires teacherId as query param.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Availability found",
                    content = @Content(schema = @Schema(implementation = TeacherAvailabilityResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Availability not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id, @RequestParam Long teacherId) {
        try {
            TeacherAvailabilityResponseDto dto = teacherAvailabilityService.getAvailabilityById(teacherId, id);
            return ResponseHandler.success("Teacher availability retrieved successfully", dto);
        } catch (ResourceNotFoundException e) {
            return ResponseHandler.notFound(e.getMessage());
        }
    }

    @Operation(summary = "Get paginated teacher availability", description = "Retrieves teacher availability with pagination, sorting and optional filters. Provide teacherId, yearId and internshipTypeId as query params if needed.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Availability entries retrieved (paginated)"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "teacherId", required = false) Long teacherId,
            @RequestParam(value = "yearId", required = false) Long yearId,
            @RequestParam(value = "internshipTypeId", required = false) Long internshipTypeId
    ) {
        Map<String, Object> result = teacherAvailabilityService.getTeacherAvailability(teacherId, yearId, internshipTypeId, queryParams);
        return ResponseHandler.success("Teacher availability retrieved successfully (paginated)", result);
    }

    @Operation(summary = "Get all teacher availability", description = "Retrieves all teacher availability entries (non-paginated). Optional query params: teacherId, yearId, internshipTypeId")
    @GetMapping("")
    public ResponseEntity<?> getAll(
            @RequestParam(value = "teacherId", required = false) Long teacherId,
            @RequestParam(value = "yearId", required = false) Long yearId,
            @RequestParam(value = "internshipTypeId", required = false) Long internshipTypeId
    ) {
        // Delegate to the same service call with empty query params to retrieve default page and return content
        Map<String, Object> result = teacherAvailabilityService.getTeacherAvailability(teacherId, yearId, internshipTypeId, Map.of());
        return ResponseHandler.success("Teacher availability retrieved successfully", result);
    }

    @Operation(summary = "Create new teacher availability", description = "Creates a new teacher availability entry. TeacherId must be set in the request body.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created",
                    content = @Content(schema = @Schema(implementation = TeacherAvailabilityResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate entry"),
            @ApiResponse(responseCode = "404", description = "Related resource not found")
    })
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody TeacherAvailabilityCreateDto dto) {
        try {
            TeacherAvailabilityResponseDto created = teacherAvailabilityService.createAvailability(dto.getTeacherId(), dto);
            return ResponseHandler.created("Teacher availability created successfully", created);
        } catch (ResourceNotFoundException e) {
            return ResponseHandler.notFound(e.getMessage());
        } catch (DuplicateResourceException | DataIntegrityViolationException | IllegalArgumentException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @Operation(summary = "Update teacher availability", description = "Updates an existing teacher availability. Request body must include teacherId if changing scope.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated",
                    content = @Content(schema = @Schema(implementation = TeacherAvailabilityResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate entry"),
            @ApiResponse(responseCode = "404", description = "Availability not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody TeacherAvailabilityUpdateDto dto) {
        try {
            if (dto.getTeacherId() == null) {
                return ResponseHandler.badRequest("teacherId is required in request body for update", Map.of());
            }
            TeacherAvailabilityResponseDto updated = teacherAvailabilityService.updateAvailability(dto.getTeacherId(), id, dto);
            return ResponseHandler.updated("Teacher availability updated successfully", updated);
        } catch (ResourceNotFoundException e) {
            return ResponseHandler.notFound(e.getMessage());
        } catch (DuplicateResourceException | DataIntegrityViolationException | IllegalArgumentException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @Operation(summary = "Delete teacher availability", description = "Deletes a teacher availability entry. Requires teacherId as query param.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Availability not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, @RequestParam Long teacherId) {
        try {
            teacherAvailabilityService.deleteAvailability(teacherId, id);
            return ResponseHandler.noContent();
        } catch (ResourceNotFoundException e) {
            return ResponseHandler.notFound(e.getMessage());
        }
    }
}
