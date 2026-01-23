package de.unipassau.allocationsystem.controller;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.unipassau.allocationsystem.dto.internshipdemand.InternshipDemandCreateDto;
import de.unipassau.allocationsystem.dto.internshipdemand.InternshipDemandFilterDto;
import de.unipassau.allocationsystem.dto.internshipdemand.InternshipDemandResponseDto;
import de.unipassau.allocationsystem.dto.internshipdemand.InternshipDemandUpdateDto;
import de.unipassau.allocationsystem.entity.InternshipDemand;
import de.unipassau.allocationsystem.mapper.InternshipDemandMapper;
import de.unipassau.allocationsystem.service.InternshipDemandService;
import de.unipassau.allocationsystem.utils.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for managing internship demands.
 * Handles CRUD operations for internship demand entries per year/subject/school type.
 */
@RestController
@RequestMapping("/internship-demands")
@RequiredArgsConstructor
@Tag(name = "Internship Demand", description = "Manage official internship demand per year/subject/school type")
public class InternshipDemandController {

private final InternshipDemandService service;
private final InternshipDemandMapper internshipDemandMapper;

/**
 * Retrieves available fields for sorting internship demands.
 * 
 * @return ResponseEntity containing list of sortable fields
 */
@Operation(
        summary = "Get sort fields",
        description = "Retrieves available fields that can be used for sorting internship demands"
)
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sort fields retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
})
@GetMapping("/sort-fields")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> getSortFields() {
        // Implement this in your InternshipDemandService similar to SubjectService
        var result = service.getSortFields();
        return ResponseHandler.success("Sort fields retrieved successfully", result);
}

/**
 * Retrieves internship demands with pagination and optional search.
 * 
 * @param queryParams Map containing pagination parameters (page, size, sort)
 * @param searchValue Optional search term for filtering
 * @return ResponseEntity containing paginated internship demands
 */
@Operation(
        summary = "Get paginated internship demands",
        description = "Retrieves internship demands with pagination, sorting, and optional search"
)
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Internship demands retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
})
@GetMapping("/paginate")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> getPaginate(
        @RequestParam Map<String, String> queryParams,
        @RequestParam(value = "searchValue", required = false) String searchValue
) {
        var result = service.getPaginated(queryParams, searchValue);
        // Convert items to DTOs to avoid lazy loading serialization issues
        if (result.containsKey("items")) {
                @SuppressWarnings("unchecked")
                List<InternshipDemand> items = (List<InternshipDemand>) result.get("items");
                List<InternshipDemandResponseDto> dtoItems = internshipDemandMapper.toResponseDtoList(items);
                result.put("items", dtoItems);
        }
        return ResponseHandler.success("Internship demands retrieved successfully (paginated)", result);
}

/**
 * Retrieves all internship demands without pagination.
 * 
 * @return ResponseEntity containing list of all internship demands
 */
@Operation(
        summary = "Get all internship demands",
        description = "Retrieves all internship demands without pagination"
        )
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Internship demands retrieved successfully",
                content = @Content(schema = @Schema(implementation = InternshipDemandResponseDto.class))
        ),
        @ApiResponse(responseCode = "500", description = "Internal server error")
})
@GetMapping
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> getAll() {
        List<InternshipDemandResponseDto> result = internshipDemandMapper.toResponseDtoList(service.getAll());
        return ResponseHandler.success("Internship demands retrieved successfully", result);
}

/**
 * Lists internship demand entries filtered by year and optional dimensions.
 * Supports pagination and filtering by multiple criteria.
 * 
 * @param filterDto Filter criteria including yearId, internshipTypeId, schoolType, subjectId, isForecasted, page, size, sort, direction
 * @return ResponseEntity containing paginated and filtered demand entries
 */
@Operation(summary = "Get internship demand list", description = "List internship demand entries filtered by year and optional dimensions")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Demand entries retrieved"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
})

@GetMapping("list-filter")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> list(@ModelAttribute InternshipDemandFilterDto filterDto,
                              @RequestParam(value = "academic_year_id", required = false) Long academicYearId) {
        String direction = Objects.requireNonNullElse(filterDto.getDirection(), "ASC");
        Sort.Direction dir = Sort.Direction.fromString(direction);
        Pageable pageable = createPageable(filterDto, dir);
        de.unipassau.allocationsystem.entity.School.SchoolType schoolType = parseSchoolType(filterDto.getSchoolType());

        Long yearId = filterDto.getYearId() != null ? filterDto.getYearId() : academicYearId;

        Page<InternshipDemand> result = service.listByYearWithFilters(
                yearId,
                filterDto.getInternshipTypeId(),
                schoolType,
                filterDto.getSubjectId(),
                filterDto.getIsForecasted(),
                pageable
        );
        Page<InternshipDemandResponseDto> dtoPage = result.map(internshipDemandMapper::toResponseDto);
        return ResponseHandler.success("Internship demand retrieved successfully", dtoPage);
}

/**
 * Creates a Pageable object from filter parameters with defaults.
 * 
 * @param filterDto Filter data containing pagination parameters
 * @param direction Sort direction
 * @return Pageable instance with validated parameters
 */
private Pageable createPageable(InternshipDemandFilterDto filterDto, Sort.Direction direction) {
        Integer page = filterDto.getPage() != null ? filterDto.getPage() : 0;
        Integer size = filterDto.getSize() != null ? filterDto.getSize() : 20;
        String sort = filterDto.getSort() != null ? filterDto.getSort() : "id";
        return PageRequest.of(page, size, Sort.by(direction, sort));
}

/**
 * Parses school type from string representation.
 * 
 * @param schoolTypeString School type as string, may be null
 * @return Parsed SchoolType or null if not provided
 */
private de.unipassau.allocationsystem.entity.School.SchoolType parseSchoolType(String schoolTypeString) {
        if (schoolTypeString != null) {
                return de.unipassau.allocationsystem.entity.School.SchoolType.valueOf(schoolTypeString);
        }
        return null;
}

/**
 * Aggregates internship demand by internship type for a specific year.
 * Provides summary statistics grouped by internship type.
 * 
 * @param yearId The academic year ID
 * @return ResponseEntity containing aggregated demand data
 */
@Operation(summary = "Aggregate internship demand by internship type for a year")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Aggregation retrieved"),
        @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
})
@GetMapping("/aggregate")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> aggregateByYear(@RequestParam("academic_year_id") Long yearId) {
        var aggs = service.getAggregationForYear(yearId);
        return ResponseHandler.success("Aggregation retrieved", aggs);
}

    /**
     * Retrieves a specific internship demand by its ID.
     * 
     * @param id The ID of the internship demand
     * @return ResponseEntity containing the internship demand details
     * @throws NoSuchElementException if internship demand not found
     */
    @Operation(summary = "Get internship demand by id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Found", content = @Content(schema = @Schema(implementation = InternshipDemandResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        InternshipDemandResponseDto res = service.getById(id)
                .map(internshipDemandMapper::toResponseDto)
                .orElseThrow(() -> new NoSuchElementException("Internship demand not found with id: " + id));
        return ResponseHandler.success("Internship demand retrieved successfully", res);
    }

    /**
     * Creates a new internship demand.
     * 
     * @param dto Internship demand creation data
     * @return ResponseEntity containing the created internship demand
     */
    @Operation(summary = "Create internship demand")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created", content = @Content(schema = @Schema(implementation = InternshipDemandResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody InternshipDemandCreateDto dto) {
        InternshipDemand entity = internshipDemandMapper.toEntityCreate(dto);
        InternshipDemand created = service.create(entity);
        return ResponseHandler.created("Internship demand created successfully", internshipDemandMapper.toResponseDto(created));
    }

    /**
     * Updates an existing internship demand.
     * 
     * @param id The ID of the internship demand to update
     * @param dto Internship demand update data
     * @return ResponseEntity containing the updated internship demand
     */
    @Operation(summary = "Update internship demand")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Updated", content = @Content(schema = @Schema(implementation = InternshipDemandResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody InternshipDemandUpdateDto dto) {
        InternshipDemand update = internshipDemandMapper.toEntityUpdate(dto);
        InternshipDemand updated = service.update(id, update);
        return ResponseHandler.updated("Internship demand updated successfully", internshipDemandMapper.toResponseDto(updated));
    }

    /**
     * Deletes an internship demand by its ID.
     * 
     * @param id The ID of the internship demand to delete
     * @return ResponseEntity with no content
     */
    @Operation(summary = "Delete internship demand")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deleted"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseHandler.noContent();
    }
}
