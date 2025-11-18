package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.teacher.TeacherCreateDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherResponseDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherUpdateDto;
import de.unipassau.allocationsystem.mapper.TeacherMapper;
import de.unipassau.allocationsystem.service.TeacherService;
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
@RequestMapping("/teachers")
@RequiredArgsConstructor
@Tag(name = "Teachers", description = "Teacher management APIs")
public class TeacherController {

    private final TeacherService teacherService;
    private final TeacherMapper teacherMapper;

    @Operation(
            summary = "Get sort fields",
            description = "Retrieves available fields that can be used for sorting teachers"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sort fields retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        List<Map<String, String>> result = teacherService.getSortFields();
        return ResponseHandler.success("Sort fields retrieved successfully", result);
    }

    @Operation(
            summary = "Get teacher by ID",
            description = "Retrieves a specific teacher by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Teacher found",
                    content = @Content(schema = @Schema(implementation = TeacherResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Teacher not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        TeacherResponseDto result = teacherService.getById(id)
                .orElseThrow(() -> new NoSuchElementException("Teacher not found with id: " + id));
        return ResponseHandler.success("Teacher retrieved successfully", result);
    }

    @Operation(
            summary = "Get paginated teachers",
            description = "Retrieves teachers with pagination, sorting, and optional search"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teachers retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        Map<String, Object> result = teacherService.getPaginated(queryParams, searchValue);
        return ResponseHandler.success("Teachers retrieved successfully (paginated)", result);
    }

    @Operation(
            summary = "Get all teachers",
            description = "Retrieves all teachers without pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Teachers retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TeacherResponseDto.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("")
    public ResponseEntity<?> getAll(@RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations) {
        List<TeacherResponseDto> result = teacherService.getAll();
        return ResponseHandler.success("Teachers retrieved successfully", result);
    }

    @Operation(
            summary = "Create new teacher",
            description = "Creates a new teacher with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Teacher created successfully",
                    content = @Content(schema = @Schema(implementation = TeacherResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate teacher"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody TeacherCreateDto dto) {
        try {
            TeacherResponseDto created = teacherService.createTeacher(dto);
            return ResponseHandler.created("Teacher created successfully", created);
        } catch (DataIntegrityViolationException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @Operation(
            summary = "Update teacher",
            description = "Updates an existing teacher with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Teacher updated successfully",
                    content = @Content(schema = @Schema(implementation = TeacherResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate email"),
            @ApiResponse(responseCode = "404", description = "Teacher not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody TeacherUpdateDto dto) {
        try {
            TeacherResponseDto updated = teacherService.updateTeacher(id, dto);
            return ResponseHandler.updated("Teacher updated successfully", updated);
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Teacher not found");
        } catch (DataIntegrityViolationException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @Operation(
            summary = "Delete teacher",
            description = "Deletes a teacher by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Teacher deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Teacher not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            teacherService.deleteTeacher(id);
            return ResponseHandler.noContent();
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Teacher not found");
        }
    }
}