package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.subject.SubjectCreateDto;
import de.unipassau.allocationsystem.dto.subject.SubjectResponseDto;
import de.unipassau.allocationsystem.dto.subject.SubjectUpdateDto;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.mapper.SubjectMapper;
import de.unipassau.allocationsystem.service.SubjectService;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * REST Controller for comprehensive subject management operations.
 * Provides endpoints for CRUD operations on subjects with pagination, search, and validation.
 */
@RestController
@RequestMapping("/subjects")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subjects", description = "Subject management APIs")
public class SubjectController {

    private final SubjectService subjectService;
    private final SubjectMapper subjectMapper;

    /**
     * Exposes the allowed sort keys and their labels so the frontend
     * can build dropdowns without hardcoding backend field names.
     */
    @Operation(
            summary = "Get sort fields",
            description = "Retrieves available fields that can be used for sorting subjects"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sort fields retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        log.info("Fetching subject sort fields");
        List<Map<String, String>> result = subjectService.getSortFields();
        return ResponseHandler.success("Sort fields retrieved successfully", result);
    }

    /**
     * Returns a paginated subset of subjects, optionally
     * filtered by a case-insensitive search value and sorted by the
     * requested field/order combination.
     */
    @Operation(
            summary = "Get paginated subjects",
            description = "Retrieves subjects with pagination, sorting, and optional search"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subjects retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        log.info("Fetching paginated subjects with params: {}", queryParams);
        Map<String, Object> result = subjectService.getPaginated(queryParams, searchValue);

        // Convert items to DTOs to avoid lazy loading serialization issues
        if (result.containsKey("items")) {
            List<Subject> items = (List<Subject>) result.get("items");
            List<SubjectResponseDto> dtoItems = subjectMapper.toResponseDtoList(items);
            result.put("items", dtoItems);
        }

        return ResponseHandler.success("Subjects retrieved successfully (paginated)", result);
    }

    /**
     * Fetches all subjects without pagination. Useful for
     * populating dropdowns where the total item count is small.
     */
    @Operation(
            summary = "Get all subjects",
            description = "Retrieves all subjects without pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Subjects retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SubjectResponseDto.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations) {
        log.info("Fetching all subjects");
        List<SubjectResponseDto> result = subjectMapper.toResponseDtoList(subjectService.getAll());
        return ResponseHandler.success("Subjects retrieved successfully", result);
    }

    /**
     * Retrieves a single subject by id or returns 404 if the
     * requested entity is not present in the database.
     */
    @Operation(
            summary = "Get subject by ID",
            description = "Retrieves a specific subject by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Subject found",
                    content = @Content(schema = @Schema(implementation = SubjectResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Subject not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        log.info("Fetching subject by id: {}", id);
        SubjectResponseDto result = subjectService.getById(id)
                .map(subjectMapper::toResponseDto)
                .orElseThrow(() -> new NoSuchElementException("Subject not found with id: " + id));
        return ResponseHandler.success("Subject retrieved successfully", result);
    }

    /**
     * Creates a new subject after validating the request body,
     * handling duplicate codes via a 400 response instead of a 500.
     */
    @Operation(
            summary = "Create new subject",
            description = "Creates a new subject with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Subject created successfully",
                    content = @Content(schema = @Schema(implementation = SubjectResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate subject code"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SubjectCreateDto dto) {
        log.info("Creating subject with payload {}", dto);
        try {
            Subject subject = subjectMapper.toEntityCreate(dto);
            Subject created = subjectService.create(subject);
            return ResponseHandler.created("Subject created successfully", subjectMapper.toResponseDto(created));
        } catch (DataIntegrityViolationException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    /**
     * Updates the subject information of an existing subject. When
     * the target id does not exist a 404 is returned; duplicate codes
     * yield a 400 with the repository error message.
     */
    @Operation(
            summary = "Update subject",
            description = "Updates an existing subject with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Subject updated successfully",
                    content = @Content(schema = @Schema(implementation = SubjectResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate code"),
            @ApiResponse(responseCode = "404", description = "Subject not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody SubjectUpdateDto dto) {
        log.info("Updating subject {} with payload {}", id, dto);
        try {
            Subject subject = subjectMapper.toEntityUpdate(dto);
            Subject updated = subjectService.update(id, subject);
            return ResponseHandler.updated("Subject updated successfully", subjectMapper.toResponseDto(updated));
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Subject not found");
        } catch (DataIntegrityViolationException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    /**
     * Deletes a subject by id, surfacing a 404 response when
     * the entity has already been removed or never existed.
     */
    @Operation(
            summary = "Delete subject",
            description = "Deletes a subject by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Subject deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Subject not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        log.info("Deleting subject {}", id);
        try {
            subjectService.delete(id);
            return ResponseHandler.noContent();
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Subject not found");
        }
    }
}

