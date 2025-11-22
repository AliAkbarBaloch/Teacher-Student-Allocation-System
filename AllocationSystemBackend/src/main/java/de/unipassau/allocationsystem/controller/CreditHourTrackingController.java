package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.credittracking.CreditHourTrackingResponseDto;
import de.unipassau.allocationsystem.dto.credittracking.CreditHourTrackingUpdateDto;
import de.unipassau.allocationsystem.service.CreditHourTrackingService;
import de.unipassau.allocationsystem.utils.ResponseHandler;
import de.unipassau.allocationsystem.utils.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
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
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/credit-hour-tracking")
@RequiredArgsConstructor
@Tag(name = "Credit Hour Tracking", description = "Manage supervising teacher credit hour tracking per academic year")
public class CreditHourTrackingController {

    private final CreditHourTrackingService service;

    @Operation(summary = "List credit tracking entries for a year")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entries retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/credit-tracking")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> list(
            @RequestParam(name = "year_id") Long yearId,
            @RequestParam(name = "teacher_id", required = false) Long teacherId,
            @RequestParam(name = "min_balance", required = false) Double minBalance,
            @RequestParam(name = "max_balance", required = false) Double maxBalance,
            @RequestParam(name = "min_hours", required = false) Double minHours,
            @RequestParam(name = "max_hours", required = false) Double maxHours,
            @RequestParam Map<String, String> queryParams
    ) {
        // Validate and extract pagination params using PaginationUtils
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        int page = Math.max(0, params.page() - 1); // PaginationUtils pages are 1-based
        int size = params.pageSize();
        Sort.Direction dir = params.sortOrder();
        String sortBy = params.sortBy();

        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sortBy));
        Page<CreditHourTrackingResponseDto> pageResult = service.listByYearWithFilters(yearId, teacherId, minBalance, maxBalance, minHours, maxHours, pageable);
        return ResponseHandler.paginated("Credit tracking entries retrieved", PaginationUtils.formatPaginationResponse(pageResult));
    }

    @Operation(summary = "Get credit tracking entry by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/credit-tracking/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            CreditHourTrackingResponseDto res = service.getById(id);
            return ResponseHandler.success("Credit tracking entry retrieved", res);
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Credit tracking entry not found");
        }
    }

    @Operation(summary = "Get all credit tracking entries for a year (no paging)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/years/{yearId}/credit-tracking")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> listForYear(@PathVariable Long yearId) {
        // reuse list endpoint with large page size
        Page<CreditHourTrackingResponseDto> result = service.listByYearWithFilters(yearId, null, null, null, null, null, PageRequest.of(0, Integer.MAX_VALUE));
        return ResponseHandler.success("Credit tracking entries for year retrieved", result.getContent());
    }

    @Operation(summary = "Update credit tracking entry (admin)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PutMapping("/credit-tracking/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody CreditHourTrackingUpdateDto dto) {
        try {
            CreditHourTrackingResponseDto updated = service.update(id, dto);
            return ResponseHandler.updated("Credit tracking updated", updated);
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Credit tracking entry not found");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }
}
