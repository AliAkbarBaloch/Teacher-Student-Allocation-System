package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.allocationplan.AllocationPlanCreateDto;
import de.unipassau.allocationsystem.dto.allocationplan.AllocationPlanResponseDto;
import de.unipassau.allocationsystem.dto.allocationplan.AllocationPlanUpdateDto;
import de.unipassau.allocationsystem.entity.AllocationPlan.PlanStatus;
import de.unipassau.allocationsystem.mapper.AllocationPlanMapper;
import de.unipassau.allocationsystem.service.AllocationPlanService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for managing allocation plans.
 * Handles plan creation, updates, versioning, and status workflow.
 */
@RestController
@RequestMapping("/allocation-plans")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Allocation Plans", description = "APIs for managing allocation plan instances with versioning and status workflow")
public class AllocationPlanController {

    private final AllocationPlanService allocationPlanService;
    private final AllocationPlanMapper allocationPlanMapper;

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get sort fields", description = "Retrieves available fields that can be used for sorting allocation plans")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sort fields retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        List<Map<String, String>> result = allocationPlanService.getSortFields();
        return ResponseHandler.success("Sort fields retrieved successfully", result);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all allocation plans", description = "Retrieves all allocation plans without pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Allocation plans retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("")
    public ResponseEntity<?> getAll(@RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations) {
        List<AllocationPlanResponseDto> result = allocationPlanMapper.toResponseDtoList(allocationPlanService.getAll());
        return ResponseHandler.success("Allocation plans retrieved successfully", result);
    }


    /**
     * Get all allocation plans with filtering and pagination.
     * Required parameter: yearId
     * Optional filters: status, isCurrent
     * Optional pagination: page, pageSize, sortBy, sortOrder
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all allocation plans",
               description = "Retrieve allocation plans with filtering by year, status, and current flag. Supports pagination.")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Allocation plans retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination or filter parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginated(
            @RequestParam(required = false) Long yearId,
            @RequestParam(required = false) PlanStatus status,
            @RequestParam(required = false) Boolean isCurrent,
            @RequestParam Map<String, String> queryParams) {
        log.info("GET /api/allocation-plans - yearId: {}, status: {}, isCurrent: {}",
                yearId, status, isCurrent);

        Map<String, Object> result = allocationPlanService.getAllPlans(
                yearId, status, isCurrent, queryParams);

        return ResponseHandler.success("Allocation plans retrieved successfully", result);
    }

    /**
     * Get a specific allocation plan by ID.
     */

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get allocation plan by ID",
               description = "Retrieve details of a specific allocation plan")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Allocation plan found",
                content = @Content(schema = @Schema(implementation = AllocationPlanResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Allocation plan not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
    @GetMapping("/{id}")
    public ResponseEntity<?> getPlanById(@PathVariable Long id) {
        log.info("GET /api/allocation-plans/{}", id);

        AllocationPlanResponseDto plan = allocationPlanService.getPlanById(id);
        return ResponseHandler.success("Allocation plan retrieved successfully", plan);
    }

    /**
     * Create a new allocation plan.
     * Only users with CREATE permission can create plans.
     */

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create new allocation plan", 
               description = "Create a new allocation plan instance for an academic year")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Allocation plan created successfully",
                content = @Content(schema = @Schema(implementation = AllocationPlanResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate plan"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
    @PostMapping
    public ResponseEntity<?> createPlan(
            @Valid @RequestBody AllocationPlanCreateDto createDto) {
        log.info("POST /api/allocation-plans - Creating plan: {} v{}", 
                createDto.getPlanName(), createDto.getPlanVersion());

        AllocationPlanResponseDto created = allocationPlanService.createPlan(createDto);
        return ResponseHandler.created("Allocation plan created successfully", created);
    }

    /**
     * Update an existing allocation plan.
     * Only users with UPDATE permission can update plans.
     */

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update allocation plan", 
               description = "Update metadata of an existing allocation plan (name, status, notes, isCurrent)")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Allocation plan updated successfully",
                content = @Content(schema = @Schema(implementation = AllocationPlanResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Allocation plan not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePlan(
            @PathVariable Long id,
            @Valid @RequestBody AllocationPlanUpdateDto updateDto) {
        log.info("PUT /api/allocation-plans/{}", id);

        AllocationPlanResponseDto updated = allocationPlanService.updatePlan(id, updateDto);
        return ResponseHandler.updated("Allocation plan updated successfully", updated);
    }

    /**
     * Set a specific plan as the current plan for its academic year.
     * This will unset is_current on all other plans for the same year.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Set plan as current", 
               description = "Mark this plan as the current active plan for its academic year")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Allocation plan set as current successfully",
                content = @Content(schema = @Schema(implementation = AllocationPlanResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Allocation plan not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
    @PostMapping("/{id}/current")
    public ResponseEntity<?> setCurrentPlan(@PathVariable Long id) {
        log.info("POST /api/allocation-plans/{}/current", id);

        AllocationPlanResponseDto updated = allocationPlanService.setCurrentPlan(id);
        return ResponseHandler.updated("Allocation plan set as current successfully", updated);
    }

    /**
     * Archive an allocation plan (soft delete).
     * Sets status to ARCHIVED and removes is_current flag.
     */
    @PostMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Archive allocation plan", 
               description = "Archive a plan by setting its status to ARCHIVED")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Allocation plan archived successfully",
                content = @Content(schema = @Schema(implementation = AllocationPlanResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Allocation plan not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
    public ResponseEntity<?> archivePlan(@PathVariable Long id) {
        log.info("POST /api/allocation-plans/{}/archive", id);

        AllocationPlanResponseDto archived = allocationPlanService.archivePlan(id);
        return ResponseHandler.updated("Allocation plan archived successfully", archived);
    }

    /**
     * Get the current allocation plan for a specific academic year.
     */
    @GetMapping("/current")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get current plan for year", 
               description = "Retrieve the current active plan for a specific academic year")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Current allocation plan retrieved successfully",
                content = @Content(schema = @Schema(implementation = AllocationPlanResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Missing or invalid yearId parameter"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
    public ResponseEntity<?> getCurrentPlanForYear(
            @RequestParam(required = true) Long yearId) {
        log.info("GET /api/allocation-plans/current?yearId={}", yearId);

        AllocationPlanResponseDto plan = allocationPlanService.getCurrentPlanForYear(yearId);
        return ResponseHandler.success("Current allocation plan retrieved successfully", plan);
    }

    /**
     * Run the allocation algorithm for a specific allocation plan.
     * This triggers the teacher allocation process for the academic year associated with the plan.
     * Used by Create Allocation Plan functionality.
     */
    @PostMapping("/{id}/run-allocation")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Run allocation algorithm",
               description = "Trigger the teacher allocation algorithm for the academic year associated with this plan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Allocation algorithm executed successfully"),
            @ApiResponse(responseCode = "404", description = "Allocation plan not found"),
            @ApiResponse(responseCode = "500", description = "Allocation algorithm execution failed")
    })
    public ResponseEntity<?> runAllocationAlgorithm(@PathVariable Long id) {
        log.info("POST /api/allocation-plans/{}/run-allocation", id);

        Long newPlanId = allocationPlanService.runAllocationForPlan(id);
        log.info("New allocation plan created with ID: {}", newPlanId);
        return ResponseHandler.success("Allocation algorithm executed successfully. A new allocation plan has been created.", 
            java.util.Map.of("newPlanId", newPlanId));
    }
}
