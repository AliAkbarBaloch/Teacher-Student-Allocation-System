package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.teacher.availability.TeacherAvailabilityCreateDto;
import de.unipassau.allocationsystem.dto.teacher.availability.TeacherAvailabilityResponseDto;
import de.unipassau.allocationsystem.dto.teacher.availability.TeacherAvailabilityUpdateDto;
import de.unipassau.allocationsystem.entity.TeacherAvailability;
import de.unipassau.allocationsystem.mapper.TeacherAvailabilityMapper;
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
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/teacher-availability")
@RequiredArgsConstructor
@Tag(name = "TeacherAvailability", description = "Teacher availability management APIs")
public class TeacherAvailabilityController {

    private final TeacherAvailabilityService teacherAvailabilityService;
    private final TeacherAvailabilityMapper teacherAvailabilityMapper;

    @Operation(
            summary = "Get sort fields",
            description = "Retrieves available fields that can be used for sorting teacher availability"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sort fields retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        List<Map<String, String>> result = teacherAvailabilityService.getSortFields();
        return ResponseHandler.success("Sort fields retrieved successfully", result);
    }

    @Operation(
            summary = "Get teacher availability by ID",
            description = "Retrieves a specific teacher availability by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Availability found",
                    content = @Content(schema = @Schema(implementation = TeacherAvailabilityResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Availability not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        TeacherAvailabilityResponseDto result = teacherAvailabilityService.getById(id)
                .map(teacherAvailabilityMapper::toResponseDto)
                .orElseThrow(() -> new NoSuchElementException("Teacher availability not found with id: " + id));
        return ResponseHandler.success("Teacher availability retrieved successfully", result);
    }

    @Operation(
            summary = "Get paginated teacher availability",
            description = "Retrieves teacher availability with pagination, sorting, and optional search"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Availability entries retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        Map<String, Object> result = teacherAvailabilityService.getPaginated(queryParams, searchValue);
        return ResponseHandler.success("Teacher availability retrieved successfully (paginated)", result);
    }

    @Operation(
            summary = "Get all teacher availability",
            description = "Retrieves all teacher availability entries without pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Availability entries retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TeacherAvailabilityResponseDto.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("")
    public ResponseEntity<?> getAll() {
        List<TeacherAvailabilityResponseDto> result = teacherAvailabilityMapper.toResponseDtoList(teacherAvailabilityService.getAll());
        return ResponseHandler.success("Teacher availability retrieved successfully", result);
    }

    @Operation(
            summary = "Create new teacher availability",
            description = "Creates a new teacher availability entry"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Availability created successfully",
                    content = @Content(schema = @Schema(implementation = TeacherAvailabilityResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate entry"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody TeacherAvailabilityCreateDto dto) {
        try {
            TeacherAvailability entity = teacherAvailabilityMapper.toEntityCreate(dto);
            TeacherAvailability created = teacherAvailabilityService.create(entity);
            return ResponseHandler.created("Availability created successfully", teacherAvailabilityMapper.toResponseDto(created));
        } catch (DataIntegrityViolationException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @Operation(
            summary = "Update teacher availability",
            description = "Updates an existing teacher availability entry"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Availability updated successfully",
                    content = @Content(schema = @Schema(implementation = TeacherAvailabilityResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate entry"),
            @ApiResponse(responseCode = "404", description = "Availability not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody TeacherAvailabilityUpdateDto dto) {
        try {
            TeacherAvailability entity = teacherAvailabilityMapper.toEntityUpdate(dto);
            TeacherAvailability updated = teacherAvailabilityService.update(id, entity);
            return ResponseHandler.updated("Availability updated successfully", teacherAvailabilityMapper.toResponseDto(updated));
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Teacher availability not found");
        } catch (DataIntegrityViolationException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @Operation(
            summary = "Delete teacher availability",
            description = "Deletes a teacher availability entry by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Availability deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Availability not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            teacherAvailabilityService.delete(id);
            return ResponseHandler.noContent();
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Teacher availability not found");
        }
    }
}
