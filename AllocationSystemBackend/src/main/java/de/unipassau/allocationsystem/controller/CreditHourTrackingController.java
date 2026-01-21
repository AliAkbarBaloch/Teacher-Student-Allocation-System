package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.credittracking.CreditHourTrackingCreateDto;
import de.unipassau.allocationsystem.dto.credittracking.CreditHourTrackingResponseDto;
import de.unipassau.allocationsystem.dto.credittracking.CreditHourTrackingUpdateDto;
import de.unipassau.allocationsystem.entity.CreditHourTracking;
import de.unipassau.allocationsystem.mapper.CreditHourTrackingMapper;
import de.unipassau.allocationsystem.service.CreditHourTrackingService;
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
 * REST controller for managing credit hour tracking entries.
 * Tracks teaching credit hours per teacher across academic years.
 */
@RestController
@RequestMapping("/credit-hour-tracking")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Credit Hour Tracking", description = "Credit hour tracking management APIs")
public class CreditHourTrackingController {

    private final CreditHourTrackingService service;
    private final CreditHourTrackingMapper mapper;

    /**
     * Retrieves available fields for sorting credit hour tracking entries.
     * 
     * @return ResponseEntity containing list of sortable fields
     */
    @Operation(
            summary = "Get sort fields",
            description = "Retrieves available fields that can be used for sorting credit hour tracking"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sort fields retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        List<Map<String, String>> result = service.getSortFields();
        return ResponseHandler.success("Sort fields retrieved successfully", result);
    }

    /**
     * Retrieves credit hour tracking entries with pagination and optional search.
     * 
     * @param queryParams Map containing pagination parameters (page, size, sort)
     * @param searchValue Optional search term for filtering
     * @return ResponseEntity containing paginated credit hour tracking entries
     */
    @Operation(
            summary = "Get paginated credit hour tracking",
            description = "Retrieves credit hour tracking with pagination, sorting, and optional search"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Entries retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        Map<String, Object> result = service.getPaginated(queryParams, searchValue);

        // Convert items to DTOs to avoid lazy loading serialization issues
        if (result.containsKey("items")) {
            List<CreditHourTracking> items = (List<CreditHourTracking>) result.get("items");
            List<CreditHourTrackingResponseDto> dtoItems = mapper.toResponseDtoList(items);
            result.put("items", dtoItems);
        }

        return ResponseHandler.success("Credit hour tracking retrieved successfully (paginated)", result);
    }

    /**
     * Retrieves all credit hour tracking entries without pagination.
     * 
     * @return ResponseEntity containing list of all credit hour tracking entries
     */
    @Operation(
            summary = "Get all credit hour tracking entries",
            description = "Retrieves all credit hour tracking entries without pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Entries retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CreditHourTrackingResponseDto.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<?> getAll() {
        List<CreditHourTrackingResponseDto> result = mapper.toResponseDtoList(service.getAll());
        return ResponseHandler.success("Credit hour tracking entries retrieved successfully", result);
    }

    /**
     * Retrieves a specific credit hour tracking entry by its ID.
     * 
     * @param id The ID of the credit hour tracking entry
     * @return ResponseEntity containing the entry details
     * @throws NoSuchElementException if entry not found
     */
    @Operation(
            summary = "Get credit hour tracking by ID",
            description = "Retrieves a specific credit hour tracking entry by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Entry found",
                    content = @Content(schema = @Schema(implementation = CreditHourTrackingResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Entry not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        CreditHourTrackingResponseDto result = service.getById(id)
                .map(mapper::toResponseDto)
                .orElseThrow(() -> new NoSuchElementException("Credit hour tracking not found with id: " + id));
        return ResponseHandler.success("Credit hour tracking entry retrieved successfully", result);
    }

    /**
     * Creates a new credit hour tracking entry.
     * 
     * @param dto Credit hour tracking creation data
     * @return ResponseEntity containing the created entry
     */
    @Operation(
            summary = "Create new credit hour tracking entry",
            description = "Creates a new credit hour tracking entry with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Entry created successfully",
                    content = @Content(schema = @Schema(implementation = CreditHourTrackingResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate entry"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreditHourTrackingCreateDto dto) {
        CreditHourTracking entity = mapper.toEntityCreate(dto);
        CreditHourTracking created = service.create(entity);
        return ResponseHandler.created("Credit hour tracking entry created successfully", mapper.toResponseDto(created));
    }

    /**
     * Updates an existing credit hour tracking entry.
     * 
     * @param id The ID of the entry to update
     * @param dto Credit hour tracking update data
     * @return ResponseEntity containing the updated entry
     */
    @Operation(
            summary = "Update credit hour tracking entry",
            description = "Updates an existing credit hour tracking entry with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Entry updated successfully",
                    content = @Content(schema = @Schema(implementation = CreditHourTrackingResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Entry not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody CreditHourTrackingUpdateDto dto) {
        CreditHourTracking entity = mapper.toEntityUpdate(dto);
        CreditHourTracking updated = service.update(id, entity);
        return ResponseHandler.updated("Credit hour tracking entry updated successfully", mapper.toResponseDto(updated));
    }

    /**
     * Deletes a credit hour tracking entry by its ID.
     * 
     * @param id The ID of the entry to delete
     * @return ResponseEntity with no content
     */
    @Operation(
            summary = "Delete credit hour tracking entry",
            description = "Deletes a credit hour tracking entry by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Entry deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Entry not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseHandler.noContent();
    }
}