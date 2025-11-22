package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.internshipdemand.InternshipDemandCreateDto;
import de.unipassau.allocationsystem.dto.internshipdemand.InternshipDemandResponseDto;
import de.unipassau.allocationsystem.dto.internshipdemand.InternshipDemandUpdateDto;
import java.util.NoSuchElementException;
import java.util.Objects;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/internship-demand")
@RequiredArgsConstructor
@Tag(name = "Internship Demand", description = "Manage official internship demand per year/subject/school type")
public class InternshipDemandController {

    private final InternshipDemandService service;

    @Operation(summary = "Get internship demand list", description = "List internship demand entries filtered by year and optional dimensions")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Demand entries retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> list(
            @RequestParam(name = "year_id") Long yearId,
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
        Page<?> result = service.listByYearWithFilters(yearId, internshipTypeId, schoolType != null ? de.unipassau.allocationsystem.entity.School.SchoolType.valueOf(schoolType) : null, subjectId, isForecasted, pageable);
        return ResponseHandler.success("Internship demand retrieved successfully", result);
    }

    @Operation(summary = "Aggregate internship demand by internship type for a year")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aggregation retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/aggregate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> aggregateByYear(@RequestParam("year_id") Long yearId) {
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
        InternshipDemandResponseDto res = service.getById(id);
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
        try {
            InternshipDemandResponseDto created = service.create(dto);
            return ResponseHandler.created("Internship demand created successfully", created);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
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
        try {
            InternshipDemandResponseDto updated = service.update(id, dto);
            return ResponseHandler.updated("Internship demand updated successfully", updated);
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Internship demand not found");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @Operation(summary = "Delete internship demand")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseHandler.noContent();
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Internship demand not found");
        }
    }
}
