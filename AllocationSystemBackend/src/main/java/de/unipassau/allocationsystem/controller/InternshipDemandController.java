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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.unipassau.allocationsystem.dto.internshipdemand.InternshipDemandCreateDto;
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

@RestController
@RequestMapping("/internship-demands")
@RequiredArgsConstructor
@Tag(name = "Internship Demand", description = "Manage official internship demand per year/subject/school type")
public class InternshipDemandController {

    private final InternshipDemandService service;
    private final InternshipDemandMapper internshipDemandMapper;

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
            List<InternshipDemand> items = (List<InternshipDemand>) result.get("items");
            List<InternshipDemandResponseDto> dtoItems = internshipDemandMapper.toResponseDtoList(items);
            result.put("items", dtoItems);
        }
        return ResponseHandler.success("Internship demands retrieved successfully (paginated)", result);
    }

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
    
    @Operation(summary = "Get internship demand list", description = "List internship demand entries filtered by year and optional dimensions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Demand entries retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("list-filter")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> list(
            @RequestParam(name = "academic_year_id") Long yearId,
            @RequestParam(name = "internship_type_id", required = false) Long internshipTypeId,
            @RequestParam(name = "school_type", required = false) String schoolType,
            @RequestParam(name = "subject_id", required = false) Long subjectId,
            @RequestParam(name = "is_forecasted", required = false) Boolean isForecasted,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "sort", defaultValue = "id") String sort,
            @RequestParam(name = "direction", defaultValue = "ASC") String direction
    ) {
        Sort.Direction dir = Sort.Direction.fromString(Objects.requireNonNull(direction));
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));
        Page<InternshipDemand> result = service.listByYearWithFilters(
                yearId,
                internshipTypeId,
                schoolType != null ? de.unipassau.allocationsystem.entity.School.SchoolType.valueOf(schoolType) : null,
                subjectId,
                isForecasted,
                pageable
        );
        Page<InternshipDemandResponseDto> dtoPage = result.map(internshipDemandMapper::toResponseDto);
        return ResponseHandler.success("Internship demand retrieved successfully", dtoPage);
    }

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
