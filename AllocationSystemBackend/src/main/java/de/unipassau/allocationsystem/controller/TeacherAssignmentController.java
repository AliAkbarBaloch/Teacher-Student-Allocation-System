package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.teacherassignment.TeacherAssignmentCreateDto;
import de.unipassau.allocationsystem.dto.teacherassignment.TeacherAssignmentResponseDto;
import de.unipassau.allocationsystem.dto.teacherassignment.TeacherAssignmentUpdateDto;
import de.unipassau.allocationsystem.entity.TeacherAssignment;
import de.unipassau.allocationsystem.mapper.TeacherAssignmentMapper;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * REST controller for managing teacher assignments.
 * Provides CRUD operations for teacher assignment entities within allocation plans.
 */
@RestController
@RequestMapping("/teacher-assignments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Teacher Assignments", description = "Manage teacher assignments within allocation plans")
public class TeacherAssignmentController {

    private final TeacherAssignmentService assignmentService;
    private final TeacherAssignmentMapper assignmentMapper;

    /**
     * Retrieves available fields for sorting teacher assignments.
     *
     * @return Available sort fields with labels
     */
    @Operation(
        summary = "Get sort fields",
        description = "Retrieves available fields that can be used for sorting teacher assignments"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sort fields retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        log.info("Fetching teacher assignment sort fields");
        List<Map<String, String>> result = assignmentService.getSortFields();
        return ResponseHandler.success("Sort fields retrieved successfully", result);
    }

    /**
     * Retrieves teacher assignments with pagination and sorting.
     *
     * @param planId Allocation plan ID
     * @param queryParams Pagination and sorting parameters
     * @param includeRelations Whether to include related entities
     * @param searchValue Optional search term
     * @return Paginated list of assignments
     */
    @Operation(
        summary = "Get paginated assignments",
        description = "Retrieves assignments with pagination, sorting, and optional search"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Assignments retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
        @PathVariable Long planId,
        @RequestParam Map<String, String> queryParams,
        @RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations,
        @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        log.info("Fetching paginated teacher assignments with params: {}", queryParams);
        Map<String, Object> result = assignmentService.getPaginated(queryParams, searchValue);

        if (result.containsKey("items")) {
            List<TeacherAssignment> items = (List<TeacherAssignment>) result.get("items");
            List<TeacherAssignmentResponseDto> dtoItems = assignmentMapper.toResponseDtoList(items);
            result.put("items", dtoItems);
        }

        return ResponseHandler.success("Assignments retrieved successfully (paginated)", result);
    }

    /**
     * Retrieves all teacher assignments without pagination.
     *
     * @param includeRelations Whether to include related entities
     * @return List of all assignments
     */
    @Operation(
        summary = "Get all assignments",
        description = "Retrieves all teacher assignments for the allocation plan"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Assignments retrieved successfully", content = @Content(schema = @Schema(implementation = TeacherAssignmentResponseDto.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations) {
        log.info("Fetching all teacher assignments");
        List<TeacherAssignmentResponseDto> result = assignmentMapper.toResponseDtoList(assignmentService.getAll());
        return ResponseHandler.success("Assignments retrieved successfully", result);
    }

    /**
     * Retrieves a specific teacher assignment by its ID.
     *
     * @param id Assignment ID
     * @return Assignment details
     * @throws NoSuchElementException if assignment not found
     */
    @Operation(
        summary = "Get assignment by ID",
        description = "Retrieves a specific teacher assignment by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Assignment found", content = @Content(schema = @Schema(implementation = TeacherAssignmentResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Assignment not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        log.info("Fetching teacher assignment by id: {}", id);
        TeacherAssignmentResponseDto result = assignmentService.getById(id)
            .map(assignmentMapper::toResponseDto)
            .orElseThrow(() -> new NoSuchElementException("Assignment not found with id: " + id));
        return ResponseHandler.success("Assignment retrieved successfully", result);
    }

    /**
     * Creates a new teacher assignment.
     *
     * @param dto Assignment creation data
     * @return Created assignment
     */
    @Operation(
        summary = "Create new assignment",
        description = "Creates a new teacher assignment with the provided details"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Assignment created successfully", content = @Content(schema = @Schema(implementation = TeacherAssignmentResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input or duplicate assignment"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody TeacherAssignmentCreateDto dto) {
        log.info("Creating teacher assignment with payload {}", dto);
        TeacherAssignment entity = assignmentMapper.toEntityCreate(dto);
        TeacherAssignment created = assignmentService.create(entity);
        return ResponseHandler.created("Assignment created successfully", assignmentMapper.toResponseDto(created));
    }

    /**
     * Updates an existing teacher assignment.
     *
     * @param id Assignment ID
     * @param dto Updated assignment data
     * @return Updated assignment
     */
    @Operation(
        summary = "Update assignment",
        description = "Updates an existing teacher assignment with the provided details"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Assignment updated successfully", content = @Content(schema = @Schema(implementation = TeacherAssignmentResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Assignment not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody TeacherAssignmentUpdateDto dto) {
        log.info("Updating teacher assignment {} with payload {}", id, dto);
        TeacherAssignment entity = assignmentMapper.toEntityUpdate(dto);
        TeacherAssignment updated = assignmentService.update(id, entity);
        return ResponseHandler.updated("Assignment updated successfully", assignmentMapper.toResponseDto(updated));
    }

    /**
     * Deletes a teacher assignment by its ID.
     *
     * @param id Assignment ID
     * @return No content response
     */
    @Operation(
        summary = "Delete assignment",
        description = "Deletes a teacher assignment by its ID"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Assignment deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Assignment not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        log.info("Deleting teacher assignment {}", id);
        assignmentService.delete(id);
        return ResponseHandler.noContent();
    }
}