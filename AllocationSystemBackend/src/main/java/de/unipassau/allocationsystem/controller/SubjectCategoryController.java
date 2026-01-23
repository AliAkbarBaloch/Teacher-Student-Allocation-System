package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.subjectcategory.SubjectCategoryCreateDto;
import de.unipassau.allocationsystem.dto.subjectcategory.SubjectCategoryResponseDto;
import de.unipassau.allocationsystem.dto.subjectcategory.SubjectCategoryUpdateDto;
import de.unipassau.allocationsystem.entity.SubjectCategory;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.SubjectCategoryMapper;
import de.unipassau.allocationsystem.service.SubjectCategoryService;
import de.unipassau.allocationsystem.utils.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

/**
 * REST controller for managing subject categories.
 * Provides CRUD operations for subject category entities.
 */
@RestController
@RequestMapping("/subject-categories")
@RequiredArgsConstructor
@Tag(name = "SubjectCategories", description = "Subject Category management APIs")
public class SubjectCategoryController {

    private final SubjectCategoryService subjectCategoryService;
    private final SubjectCategoryMapper subjectCategoryMapper;

    /**
     * Retrieves available fields for sorting subject categories.
     *
     * @return Available sort fields with labels
     */
    @Operation(
            summary = "Get sort fields",
            description = "Retrieves available fields that can be used for sorting subject categories"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sort fields retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        List<Map<String, String>> result = subjectCategoryService.getSortFields();
        return ResponseHandler.success("Sort fields retrieved successfully", result);
    }

    /**
     * Retrieves a specific subject category by its ID.
     *
     * @param id Subject category ID
     * @return Subject category details
     * @throws ResourceNotFoundException if subject category not found
     */
    @Operation(
            summary = "Get subject category by ID",
            description = "Retrieves a specific subject category by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Subject category found",
                    content = @Content(schema = @Schema(implementation = SubjectCategoryResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Subject category not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        SubjectCategoryResponseDto result = subjectCategoryService.getById(id)
                .map(subjectCategoryMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Subject category not found with id: " + id));
        return ResponseHandler.success("Subject category retrieved successfully", result);
    }

    /**
     * Retrieves subject categories with pagination and sorting.
     *
     * @param queryParams Pagination and sorting parameters
     * @param includeRelations Whether to include related entities
     * @param searchValue Optional search term
     * @return Paginated list of subject categories
     */
    @Operation(
            summary = "Get paginated subject categories",
            description = "Retrieves subject categories with pagination, sorting, and optional search"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subject categories retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        Map<String, Object> result = subjectCategoryService.getPaginated(queryParams, searchValue);
        return ResponseHandler.success("Subject categories retrieved successfully (paginated)", result);
    }

    /**
     * Retrieves all subject categories without pagination.
     *
     * @param includeRelations Whether to include related entities
     * @return List of all subject categories
     */
    @Operation(
            summary = "Get all subject categories",
            description = "Retrieves all subject categories without pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Subject categories retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SubjectCategoryResponseDto.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("")
    public ResponseEntity<?> getAll(@RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations) {
        List<SubjectCategoryResponseDto> result = subjectCategoryMapper.toResponseDtoList(subjectCategoryService.getAll());
        return ResponseHandler.success("Subject categories retrieved successfully", result);
    }

    /**
     * Creates a new subject category.
     *
     * @param dto Subject category creation data
     * @return Created subject category
     */
    @Operation(
            summary = "Create new subject category",
            description = "Creates a new subject category with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Subject category created successfully",
                    content = @Content(schema = @Schema(implementation = SubjectCategoryResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate subject category"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SubjectCategoryCreateDto dto) {
        SubjectCategory subjectCategory = subjectCategoryMapper.toEntityCreate(dto);
        SubjectCategory created = subjectCategoryService.create(subjectCategory);
        return ResponseHandler.created("Subject category created successfully", subjectCategoryMapper.toResponseDto(created));
    }

    /**
     * Updates an existing subject category.
     *
     * @param id Subject category ID
     * @param dto Updated subject category data
     * @return Updated subject category
     */
    @Operation(
            summary = "Update subject category",
            description = "Updates an existing subject category with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Subject category updated successfully",
                    content = @Content(schema = @Schema(implementation = SubjectCategoryResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate title"),
            @ApiResponse(responseCode = "404", description = "Subject category not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody SubjectCategoryUpdateDto dto) {
        SubjectCategory subjectCategory = subjectCategoryMapper.toEntityUpdate(dto);
        SubjectCategory updated = subjectCategoryService.update(id, subjectCategory);
        return ResponseHandler.updated("Subject category updated successfully", subjectCategoryMapper.toResponseDto(updated));
    }

    /**
     * Deletes a subject category by its ID.
     *
     * @param id Subject category ID
     * @return No content response
     */
    @Operation(
            summary = "Delete subject category",
            description = "Deletes a subject category by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Subject category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Subject category not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        subjectCategoryService.delete(id);
        return ResponseHandler.noContent();
    }
}