package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.internshiptype.InternshipTypeCreateDto;
import de.unipassau.allocationsystem.dto.internshiptype.InternshipTypeResponseDto;
import de.unipassau.allocationsystem.dto.internshiptype.InternshipTypeUpdateDto;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.InternshipTypeMapper;
import de.unipassau.allocationsystem.service.InternshipTypeService;
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
 * REST controller for managing internship types.
 * Provides CRUD operations for internship type entities.
 */
@RestController
@RequestMapping("/internship-types")
@RequiredArgsConstructor
@Tag(name = "Internship Types", description = "Internship type management APIs")
public class InternshipTypeController {

    private final InternshipTypeService internshipTypeService;
    private final InternshipTypeMapper internshipTypeMapper;

    /**
     * Retrieves available fields for sorting internship types.
     * 
     * @return ResponseEntity containing list of sortable fields
     */
    @Operation(
            summary = "Get sort fields",
            description = "Retrieves available fields that can be used for sorting internship types"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sort fields retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        List<Map<String, String>> result = internshipTypeService.getSortFields();
        return ResponseHandler.success("Sort fields retrieved successfully", result);
    }

    /**
     * Retrieves a specific internship type by its ID.
     * 
     * @param id The ID of the internship type
     * @return ResponseEntity containing the internship type details
     * @throws ResourceNotFoundException if internship type not found
     */
    @Operation(
            summary = "Get internship type by ID",
            description = "Retrieves a specific internship type by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Internship type found",
                    content = @Content(schema = @Schema(implementation = InternshipTypeResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Internship type not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        InternshipTypeResponseDto result = internshipTypeService.getById(id)
                .map(internshipTypeMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("InternshipType not found with id: " + id));
        return ResponseHandler.success("Internship type retrieved successfully", result);
    }

    /**
     * Retrieves internship types with pagination and optional search.
     * 
     * @param queryParams Map containing pagination parameters (page, size, sort)
     * @param includeRelations Flag to include related entities
     * @param searchValue Optional search term for filtering
     * @return ResponseEntity containing paginated internship types
     */
    @Operation(
            summary = "Get paginated internship types",
            description = "Retrieves internship types with pagination, sorting, and optional search"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Internship types retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        Map<String, Object> result = internshipTypeService.getPaginated(queryParams, searchValue);
        return ResponseHandler.success("Internship types retrieved successfully (paginated)", result);
    }

    /**
     * Retrieves all internship types without pagination.
     * 
     * @param includeRelations Flag to include related entities
     * @return ResponseEntity containing list of all internship types
     */
    @Operation(
            summary = "Get all internship types",
            description = "Retrieves all internship types without pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Internship types retrieved successfully",
                    content = @Content(schema = @Schema(implementation = InternshipTypeResponseDto.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("")
    public ResponseEntity<?> getAll(@RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations) {
        List<InternshipTypeResponseDto> result = internshipTypeMapper.toResponseDtoList(internshipTypeService.getAll());
        return ResponseHandler.success("Internship types retrieved successfully", result);
    }

    /**
     * Creates a new internship type.
     * 
     * @param dto Internship type creation data
     * @return ResponseEntity containing the created internship type
     */
    @Operation(
            summary = "Create new internship type",
            description = "Creates a new internship type with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Internship type created successfully",
                    content = @Content(schema = @Schema(implementation = InternshipTypeResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate internship type"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody InternshipTypeCreateDto dto) {
        InternshipType internshipType = internshipTypeMapper.toEntityCreate(dto);
        InternshipType created = internshipTypeService.create(internshipType);
        return ResponseHandler.created("Internship type created successfully", internshipTypeMapper.toResponseDto(created));
    }

    /**
     * Updates an existing internship type.
     * 
     * @param id The ID of the internship type to update
     * @param dto Internship type update data
     * @return ResponseEntity containing the updated internship type
     */
    @Operation(
            summary = "Update internship type",
            description = "Updates an existing internship type with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Internship type updated successfully",
                    content = @Content(schema = @Schema(implementation = InternshipTypeResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate code"),
            @ApiResponse(responseCode = "404", description = "Internship type not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody InternshipTypeUpdateDto dto) {
        InternshipType internshipType = internshipTypeMapper.toEntityUpdate(dto);
        InternshipType updated = internshipTypeService.update(id, internshipType);
        return ResponseHandler.updated("Internship type updated successfully", internshipTypeMapper.toResponseDto(updated));
    }

    /**
     * Deletes an internship type by its ID.
     * 
     * @param id The ID of the internship type to delete
     * @return ResponseEntity with no content
     */
    @Operation(
            summary = "Delete internship type",
            description = "Deletes an internship type by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Internship type deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Internship type not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        internshipTypeService.delete(id);
        return ResponseHandler.noContent();
    }
}