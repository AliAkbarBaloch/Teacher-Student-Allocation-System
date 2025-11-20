package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.entity.PlanChangeLog;
import de.unipassau.allocationsystem.mapper.PlanChangeLogMapper;
import de.unipassau.allocationsystem.service.PlanChangeLogService;
import de.unipassau.allocationsystem.utils.ResponseHandler;
import de.unipassau.allocationsystem.dto.planchangelog.PlanChangeLogFilterDto;
import de.unipassau.allocationsystem.dto.planchangelog.PlanChangeLogDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

 

@RestController
@RequestMapping("/plan-change-logs")
@RequiredArgsConstructor
@Tag(name = "PlanChangeLogs", description = "Plan change log read-only APIs")
@SecurityRequirement(name = "bearerAuth")
public class PlanChangeLogController {

    private final PlanChangeLogService service;
    private final PlanChangeLogMapper mapper;

    @GetMapping("/plans/{planId}/change-logs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get change logs for a plan", description = "Retrieve change logs for a specific plan. Admin access required.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Change logs retrieved successfully", content = @Content(schema = @Schema(implementation = PlanChangeLogDto.class))),
            @ApiResponse(responseCode = "404", description = "Allocation plan not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getLogsForPlan(
            @PathVariable Long planId,
            @ModelAttribute PlanChangeLogFilterDto filter
    ) {
        try {
            // ensure planId is bound to filter
            filter.setPlanId(planId);

            int page = filter.getPage() != null ? filter.getPage() : 0;
            int size = filter.getSize() != null ? filter.getSize() : 20;
            String sortBy = filter.getSortBy() != null ? filter.getSortBy() : "eventTimestamp";
            String sortDirection = filter.getSortDirection() != null ? filter.getSortDirection() : "DESC";

            Pageable pageable = createPageable(page, size, sortBy, sortDirection);
            Page<PlanChangeLog> logs = service.getLogsByPlan(filter.getPlanId(), filter.getUserId(), filter.getEntityType(), filter.getChangeType(), filter.getStartDate(), filter.getEndDate(), pageable);
            return ResponseHandler.success("Plan change logs retrieved successfully", logs.map(mapper::toDto));
        } catch (de.unipassau.allocationsystem.exception.ResourceNotFoundException e) {
            return ResponseHandler.notFound(e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get change logs across plans", description = "Retrieve change logs across all plans with optional filters and pagination. Admin access required.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Change logs retrieved successfully", content = @Content(schema = @Schema(implementation = PlanChangeLogDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getLogs(@ModelAttribute PlanChangeLogFilterDto filter) {
        int page = filter.getPage() != null ? filter.getPage() : 0;
        int size = filter.getSize() != null ? filter.getSize() : 20;
        String sortBy = filter.getSortBy() != null ? filter.getSortBy() : "eventTimestamp";
        String sortDirection = filter.getSortDirection() != null ? filter.getSortDirection() : "DESC";

        Pageable pageable = createPageable(page, size, sortBy, sortDirection);
        Page<PlanChangeLog> logs = service.getLogs(filter.getPlanId(), filter.getUserId(), filter.getEntityType(), filter.getChangeType(), filter.getStartDate(), filter.getEndDate(), pageable);
        return ResponseHandler.success("Plan change logs retrieved successfully", logs.map(mapper::toDto));
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}
