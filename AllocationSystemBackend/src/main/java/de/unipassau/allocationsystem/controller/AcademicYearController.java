package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.academicyear.AcademicYearCreateDto;
import de.unipassau.allocationsystem.dto.academicyear.AcademicYearResponseDto;
import de.unipassau.allocationsystem.dto.academicyear.AcademicYearUpdateDto;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.AcademicYearMapper;
import de.unipassau.allocationsystem.service.AcademicYearService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/academic-years")
@RequiredArgsConstructor
@Tag(name = "AcademicYears", description = "Academic Year management APIs")
public class AcademicYearController {

    private final AcademicYearService academicYearService;
    private final AcademicYearMapper academicYearMapper;

    @Operation(
            summary = "Get sort fields",
            description = "Retrieves available fields that can be used for sorting academic years"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sort fields retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        List<Map<String, String>> result = academicYearService.getSortFields();
        return ResponseHandler.success("Sort fields retrieved successfully", result);
    }

    @Operation(
            summary = "Get academic year by ID",
            description = "Retrieves a specific academic year by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Academic year found",
                    content = @Content(schema = @Schema(implementation = AcademicYearResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Academic year not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        AcademicYearResponseDto result = academicYearService.getById(id)
                .map(academicYearMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + id));
        return ResponseHandler.success("Academic year retrieved successfully", result);
    }

    @Operation(
            summary = "Get paginated academic years",
            description = "Retrieves academic years with pagination, sorting, and optional search"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Academic years retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        Map<String, Object> result = academicYearService.getPaginated(queryParams, searchValue);
        return ResponseHandler.success("Academic years retrieved successfully (paginated)", result);
    }

    @Operation(
            summary = "Get all academic years",
            description = "Retrieves all academic years without pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Academic years retrieved successfully",
                    content = @Content(schema = @Schema(implementation = AcademicYearResponseDto.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("")
    public ResponseEntity<?> getAll(@RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations) {
        List<AcademicYearResponseDto> result = academicYearMapper.toResponseDtoList(academicYearService.getAll());
        return ResponseHandler.success("Academic years retrieved successfully", result);
    }

    @Operation(
            summary = "Create new academic year",
            description = "Creates a new academic year with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Academic year created successfully",
                    content = @Content(schema = @Schema(implementation = AcademicYearResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate academic year"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody AcademicYearCreateDto dto) {
        AcademicYear academicYear = academicYearMapper.toEntityCreate(dto);
        AcademicYear created = academicYearService.create(academicYear);
        return ResponseHandler.created("Academic year created successfully", academicYearMapper.toResponseDto(created));
    }

    @Operation(
            summary = "Update academic year",
            description = "Updates an existing academic year with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Academic year updated successfully",
                    content = @Content(schema = @Schema(implementation = AcademicYearResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate year name"),
            @ApiResponse(responseCode = "404", description = "Academic year not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody AcademicYearUpdateDto dto) {
        AcademicYear academicYear = academicYearMapper.toEntityUpdate(dto);
        AcademicYear updated = academicYearService.update(id, academicYear);
        return ResponseHandler.updated("Academic year updated successfully", academicYearMapper.toResponseDto(updated));
    }

    @Operation(
            summary = "Delete academic year",
            description = "Deletes an academic year by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Academic year deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Academic year not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        academicYearService.delete(id);
        return ResponseHandler.noContent();
    }
}