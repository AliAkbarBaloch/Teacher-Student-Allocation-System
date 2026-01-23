package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.controller.docs.CreateDocs;
import de.unipassau.allocationsystem.controller.docs.DeleteDocs;
import de.unipassau.allocationsystem.controller.docs.GetAllDocs;
import de.unipassau.allocationsystem.controller.docs.GetByIdDocs;
import de.unipassau.allocationsystem.controller.docs.GetPaginatedDocs;
import de.unipassau.allocationsystem.controller.docs.GetSortFieldsDocs;
import de.unipassau.allocationsystem.controller.docs.UpdateDocs;
import de.unipassau.allocationsystem.dto.credittracking.CreditHourTrackingCreateDto;
import de.unipassau.allocationsystem.dto.credittracking.CreditHourTrackingResponseDto;
import de.unipassau.allocationsystem.dto.credittracking.CreditHourTrackingUpdateDto;
import de.unipassau.allocationsystem.entity.CreditHourTracking;
import de.unipassau.allocationsystem.mapper.CreditHourTrackingMapper;
import de.unipassau.allocationsystem.service.CreditHourTrackingService;
import de.unipassau.allocationsystem.utils.ResponseHandler;
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
    @GetSortFieldsDocs
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
    /**
     * Retrieves credit hour tracking with pagination, sorting, and optional search.
     * 
     * @param queryParams Map containing pagination parameters (page, size, sort)
     * @param searchValue Optional search term for filtering
     * @return ResponseEntity containing paginated credit hour tracking entries
     */
    @GetPaginatedDocs
    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        Map<String, Object> result = service.getPaginated(queryParams, searchValue);

        // Convert items to DTOs to avoid lazy loading serialization issues
        if (result.containsKey("items")) {
            @SuppressWarnings("unchecked")
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
    @GetAllDocs
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
    @GetByIdDocs
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
    @CreateDocs
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
    @UpdateDocs
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
    @DeleteDocs
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseHandler.noContent();
    }
}