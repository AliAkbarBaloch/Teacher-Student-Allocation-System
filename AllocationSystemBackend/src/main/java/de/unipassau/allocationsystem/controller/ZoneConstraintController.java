package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintCreateDto;
import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintResponseDto;
import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintUpdateDto;
import de.unipassau.allocationsystem.mapper.ZoneConstraintMapper;
import de.unipassau.allocationsystem.service.ZoneConstraintService;
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

/**
 * REST controller for managing zone constraints.
 * Handles CRUD operations for zone-based internship type restrictions.
 */
@RestController
@RequestMapping("/zone-constraints")
@RequiredArgsConstructor
@Tag(name = "Zone Constraints", description = "Zone constraint management APIs")
public class ZoneConstraintController {

    private final ZoneConstraintService zoneConstraintService;
    private final ZoneConstraintMapper zoneConstraintMapper;

    @Operation(
            summary = "Get sort fields",
            description = "Retrieves available fields that can be used for sorting zone constraints"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sort fields retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        List<Map<String, String>> result = zoneConstraintService.getSortFields();
        return ResponseHandler.success("Sort fields retrieved successfully", result);
    }

    @Operation(
            summary = "Get zone constraint by ID",
            description = "Retrieves a specific zone constraint by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Zone constraint found",
                    content = @Content(schema = @Schema(implementation = ZoneConstraintResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Zone constraint not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        ZoneConstraintResponseDto result = zoneConstraintService.getById(id)
                .map(zoneConstraintMapper::toResponseDto)
                .orElseThrow(() -> new NoSuchElementException("Zone constraint not found with id: " + id));
        return ResponseHandler.success("Zone constraint retrieved successfully", result);
    }

    @Operation(
            summary = "Get paginated zone constraints",
            description = "Retrieves zone constraints with pagination, sorting, and optional search"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zone constraints retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        Map<String, Object> result = zoneConstraintService.getPaginated(queryParams, searchValue);

        // Convert items to DTOs to avoid lazy loading serialization issues
        if (result.containsKey("items")) {
            List<?> items = (List<?>) result.get("items");
            List<ZoneConstraintResponseDto> dtoItems = zoneConstraintMapper.toResponseDtoList((List) items);
            result.put("items", dtoItems);
        }

        return ResponseHandler.success("Zone constraints retrieved successfully (paginated)", result);
    }

    @Operation(
            summary = "Get all zone constraints",
            description = "Retrieves all zone constraints without pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Zone constraints retrieved successfully",
                    content = @Content(schema = @Schema(implementation = ZoneConstraintResponseDto.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("")
    public ResponseEntity<?> getAll() {
        List<ZoneConstraintResponseDto> result = zoneConstraintMapper.toResponseDtoList(zoneConstraintService.getAll());
        return ResponseHandler.success("Zone constraints retrieved successfully", result);
    }

    @Operation(
            summary = "Create new zone constraint",
            description = "Creates a new zone constraint with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Zone constraint created successfully",
                    content = @Content(schema = @Schema(implementation = ZoneConstraintResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate constraint"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ZoneConstraintCreateDto dto) {
        try {
            ZoneConstraintResponseDto created = zoneConstraintService.create(dto);
            return ResponseHandler.created("Zone constraint created successfully", created);
        } catch (DataIntegrityViolationException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @Operation(
            summary = "Update zone constraint",
            description = "Updates an existing zone constraint with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Zone constraint updated successfully",
                    content = @Content(schema = @Schema(implementation = ZoneConstraintResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate constraint"),
            @ApiResponse(responseCode = "404", description = "Zone constraint not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ZoneConstraintUpdateDto dto) {
        try {
            ZoneConstraintResponseDto updated = zoneConstraintService.update(id, dto);
            return ResponseHandler.updated("Zone constraint updated successfully", updated);
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Zone constraint not found");
        } catch (DataIntegrityViolationException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @Operation(
            summary = "Delete zone constraint",
            description = "Deletes a zone constraint by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Zone constraint deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Zone constraint not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            zoneConstraintService.delete(id);
            return ResponseHandler.noContent();
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Zone constraint not found");
        }
    }
}
