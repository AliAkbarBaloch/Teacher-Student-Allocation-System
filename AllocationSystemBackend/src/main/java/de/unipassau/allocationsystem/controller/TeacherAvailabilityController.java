package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.teacher.availability.TeacherAvailabilityCreateDto;
import de.unipassau.allocationsystem.dto.teacher.availability.TeacherAvailabilityResponseDto;
import de.unipassau.allocationsystem.dto.teacher.availability.TeacherAvailabilityUpdateDto;
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

import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/teacher-availability")
@RequiredArgsConstructor
@Tag(name = "TeacherAvailability", description = "Teacher Availability management APIs")
public class TeacherAvailabilityController {

    private final TeacherAvailabilityService teacherAvailabilityService;

    @Operation(
            summary = "Get paginated teacher availability",
            description = "Retrieves teacher availability with pagination, sorting, and optional filters"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teacher availability retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Long teacherId,
            @RequestParam(required = false) Long yearId,
            @RequestParam(required = false) Long internshipTypeId,
            @RequestParam Map<String, String> queryParams
    ) {
        Map<String, Object> result = teacherAvailabilityService.getTeacherAvailability(teacherId, yearId, internshipTypeId, queryParams);
        return ResponseHandler.success("Teacher availability retrieved successfully (paginated)", result);
    }

    @Operation(
            summary = "Get teacher availability by ID",
            description = "Retrieves a specific teacher availability entry by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Teacher availability found",
                    content = @Content(schema = @Schema(implementation = TeacherAvailabilityResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Teacher availability not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{teacherId}/{availabilityId}")
    public ResponseEntity<?> getById(
            @PathVariable Long teacherId,
            @PathVariable Long availabilityId
    ) {
        TeacherAvailabilityResponseDto result = teacherAvailabilityService.getAvailabilityById(teacherId, availabilityId);
        return ResponseHandler.success("Teacher availability retrieved successfully", result);
    }

    @Operation(
            summary = "Create new teacher availability",
            description = "Creates a new teacher availability entry with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Teacher availability created successfully",
                    content = @Content(schema = @Schema(implementation = TeacherAvailabilityResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate entry"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{teacherId}")
    public ResponseEntity<?> create(
            @PathVariable Long teacherId,
            @Valid @RequestBody TeacherAvailabilityCreateDto dto
    ) {
        try {
            TeacherAvailabilityResponseDto result = teacherAvailabilityService.createAvailability(teacherId, dto);
            return ResponseHandler.created("Teacher availability created successfully", result);
        } catch (DataIntegrityViolationException | IllegalArgumentException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @Operation(
            summary = "Update teacher availability",
            description = "Updates an existing teacher availability entry with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Teacher availability updated successfully",
                    content = @Content(schema = @Schema(implementation = TeacherAvailabilityResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate entry"),
            @ApiResponse(responseCode = "404", description = "Teacher availability not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{teacherId}/{availabilityId}")
    public ResponseEntity<?> update(
            @PathVariable Long teacherId,
            @PathVariable Long availabilityId,
            @RequestBody TeacherAvailabilityUpdateDto dto
    ) {
        try {
            TeacherAvailabilityResponseDto result = teacherAvailabilityService.updateAvailability(teacherId, availabilityId, dto);
            return ResponseHandler.updated("Teacher availability updated successfully", result);
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Teacher availability not found");
        } catch (DataIntegrityViolationException | IllegalArgumentException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @Operation(
            summary = "Delete teacher availability",
            description = "Deletes a teacher availability entry by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Teacher availability deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Teacher availability not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{teacherId}/{availabilityId}")
    public ResponseEntity<?> delete(
            @PathVariable Long teacherId,
            @PathVariable Long availabilityId
    ) {
        try {
            teacherAvailabilityService.deleteAvailability(teacherId, availabilityId);
            return ResponseHandler.noContent();
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Teacher availability not found");
        }
    }
}