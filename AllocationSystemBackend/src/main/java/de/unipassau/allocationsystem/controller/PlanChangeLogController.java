package de.unipassau.allocationsystem.controller;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.dao.DataIntegrityViolationException;
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

import de.unipassau.allocationsystem.dto.planchangelog.PlanChangeLogCreateDto;
import de.unipassau.allocationsystem.dto.planchangelog.PlanChangeLogFilterDto;
import de.unipassau.allocationsystem.dto.planchangelog.PlanChangeLogResponseDto;
import de.unipassau.allocationsystem.dto.planchangelog.PlanChangeLogUpdateDto;
import de.unipassau.allocationsystem.entity.PlanChangeLog;
import de.unipassau.allocationsystem.mapper.PlanChangeLogMapper;
import de.unipassau.allocationsystem.service.PlanChangeLogService;
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

 

@RestController
@RequestMapping("/plan-change-logs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "PlanChangeLogs", description = "Plan change log read-only APIs")
public class PlanChangeLogController {

    private final PlanChangeLogService planChangeLogService;
    private final PlanChangeLogMapper planChangeLogMapper;

    @Operation(
            summary = "Get sort fields",
            description = "Retrieves available fields that can be used for sorting plan change logs"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sort fields retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        log.info("Fetching plan change log sort fields");
        List<Map<String, String>> result = planChangeLogService.getSortFields();
        return ResponseHandler.success("Sort fields retrieved successfully", result);
    }

    @Operation(
            summary = "Get paginated plan change logs",
            description = "Retrieves plan change logs with pagination and sorting"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plan change logs retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        log.info("Fetching paginated plan change logs with params: {}", queryParams);
        Map<String, Object> result = planChangeLogService.getPaginated(queryParams, searchValue);

        // Convert items to DTOs
        if (result.containsKey("items")) {
            List<PlanChangeLog> items = (List<PlanChangeLog>) result.get("items");
            List<PlanChangeLogResponseDto> dtoItems = planChangeLogMapper.toResponseDtoList(items);
            result.put("items", dtoItems);
        }

        return ResponseHandler.success("Plan change logs retrieved successfully (paginated)", result);
    }

    @Operation(
            summary = "Get all plan change logs",
            description = "Retrieves all plan change logs without pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Plan change logs retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PlanChangeLogResponseDto.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<?> getAll() {
        log.info("Fetching all plan change logs");
        List<PlanChangeLogResponseDto> result = planChangeLogMapper.toResponseDtoList(planChangeLogService.getAll());
        return ResponseHandler.success("Plan change logs retrieved successfully", result);
    }

    @Operation(
            summary = "Get plan change log by ID",
            description = "Retrieves a specific plan change log by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Plan change log found",
                    content = @Content(schema = @Schema(implementation = PlanChangeLogResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Plan change log not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        log.info("Fetching plan change log by id: {}", id);
        PlanChangeLogResponseDto result = planChangeLogService.getById(id)
                .map(planChangeLogMapper::toResponseDto)
                .orElseThrow(() -> new NoSuchElementException("Plan change log not found with id: " + id));
        return ResponseHandler.success("Plan change log retrieved successfully", result);
    }

    @Operation(
            summary = "Create new plan change log",
            description = "Creates a new plan change log with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Plan change log created successfully",
                    content = @Content(schema = @Schema(implementation = PlanChangeLogResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody PlanChangeLogCreateDto dto) {
        log.info("Creating plan change log with payload {}", dto);
        try {
            PlanChangeLog entity = planChangeLogMapper.toEntityCreate(dto);
            PlanChangeLog created = planChangeLogService.create(entity);
            return ResponseHandler.created("Plan change log created successfully", planChangeLogMapper.toResponseDto(created));
        } catch (DataIntegrityViolationException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @Operation(
            summary = "Update plan change log",
            description = "Updates an existing plan change log with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Plan change log updated successfully",
                    content = @Content(schema = @Schema(implementation = PlanChangeLogResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Plan change log not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody PlanChangeLogUpdateDto dto) {
        log.info("Updating plan change log {} with payload {}", id, dto);
        try {
            PlanChangeLog entity = planChangeLogMapper.toEntityUpdate(dto);
            PlanChangeLog updated = planChangeLogService.update(id, entity);
            return ResponseHandler.updated("Plan change log updated successfully", planChangeLogMapper.toResponseDto(updated));
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Plan change log not found");
        } catch (DataIntegrityViolationException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @Operation(
            summary = "Delete plan change log",
            description = "Deletes a plan change log by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Plan change log deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Plan change log not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        log.info("Deleting plan change log {}", id);
        try {
            planChangeLogService.delete(id);
            return ResponseHandler.noContent();
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Plan change log not found");
        }
    }

    @GetMapping("/plans/{planId}/change-logs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get change logs for a plan",
        description = "Retrieve change logs for a specific plan. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Change logs retrieved successfully", content = @Content(schema = @Schema(implementation = PlanChangeLogResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Allocation plan not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getLogsForPlan(
            @PathVariable Long planId,
            @ModelAttribute PlanChangeLogFilterDto filter
    ) {
        try {
            filter.setPlanId(planId);

            int page = filter.getPage() != null ? filter.getPage() : 1;
            int size = filter.getSize() != null ? filter.getSize() : 20;
            String sortBy = filter.getSortBy() != null ? filter.getSortBy() : "createdAt";
            String sortDirection = filter.getSortDirection() != null ? filter.getSortDirection() : "DESC";

            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
            Page<PlanChangeLog> logs = planChangeLogService.getLogsByPlan(
                    filter.getPlanId(),
                    filter.getEntityType(),
                    filter.getChangeType(),
                    filter.getStartDate(),
                    filter.getEndDate(),
                    pageable
            );
            return ResponseHandler.success("Plan change logs retrieved successfully", logs.map(planChangeLogMapper::toResponseDto));
        } catch (de.unipassau.allocationsystem.exception.ResourceNotFoundException e) {
            return ResponseHandler.notFound(e.getMessage());
        }
    }

    @GetMapping("/filter")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get change logs across plans",
        description = "Retrieve change logs across all plans with optional filters and pagination. Admin access required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Change logs retrieved successfully", content = @Content(schema = @Schema(implementation = PlanChangeLogResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid filter parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getLogs(@ModelAttribute PlanChangeLogFilterDto filter) {
        int page = filter.getPage() != null ? filter.getPage() : 1;
        int size = filter.getSize() != null ? filter.getSize() : 20;
        String sortBy = filter.getSortBy() != null ? filter.getSortBy() : "createdAt";
        String sortDirection = filter.getSortDirection() != null ? filter.getSortDirection() : "DESC";

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.fromString(sortDirection), sortBy));
        Page<PlanChangeLog> logs = planChangeLogService.getLogs(
                filter.getPlanId(),
                filter.getEntityType(),
                filter.getChangeType(),
                filter.getStartDate(),
                filter.getEndDate(),
                pageable
        );
        return ResponseHandler.success("Plan change logs retrieved successfully", logs.map(planChangeLogMapper::toResponseDto));
    }
}
