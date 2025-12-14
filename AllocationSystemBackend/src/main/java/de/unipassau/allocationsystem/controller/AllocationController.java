package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.allocation.TeacherAllocationService;
import de.unipassau.allocationsystem.entity.AllocationPlan;
import de.unipassau.allocationsystem.utils.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for executing the teacher allocation process.
 * Provides endpoints for administrators to trigger allocation runs from the frontend.
 */
@Slf4j
@RestController
@RequestMapping("/api/allocation")
@RequiredArgsConstructor
@Tag(name = "Allocation Execution", description = "Endpoints to trigger and manage teacher allocation process")
public class AllocationController {

    private final TeacherAllocationService teacherAllocationService;

    /**
     * Triggers the allocation process for a specific academic year.
     * This endpoint is used by the Create Allocation Plan functionality.
     * 
     * @param academicYearId The ID of the academic year to allocate
     * @return ResponseEntity containing the created AllocationPlan details
     */
    @Operation(
        summary = "Run Allocation Process",
        description = "Executes the teacher allocation algorithm for the specified academic year. " +
                     "Creates a new AllocationPlan with teacher assignments based on priorities: " +
                     "1) SFP (highest), 2) ZSP (medium), 3) PDP (lowest). " +
                     "Requires ADMIN role."
    )
    @PostMapping("/run/{academicYearId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> runAllocation(
            @PathVariable Long academicYearId,
            @RequestBody(required = false) Map<String, Object> requestBody) {
        Boolean isCurrent = requestBody != null && requestBody.containsKey("isCurrent") 
            ? (Boolean) requestBody.get("isCurrent") 
            : false;
        log.info("Allocation process triggered from API for academic year ID: {}, isCurrent: {}", academicYearId, isCurrent);
        
        try {
            AllocationPlan allocationPlan = teacherAllocationService.performAllocation(academicYearId, isCurrent);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("planId", allocationPlan.getId());
            responseData.put("planName", allocationPlan.getPlanName());
            responseData.put("planVersion", allocationPlan.getPlanVersion());
            responseData.put("status", allocationPlan.getStatus().name());
            responseData.put("academicYearId", allocationPlan.getAcademicYear().getId());
            responseData.put("academicYearName", allocationPlan.getAcademicYear().getYearName());
            responseData.put("createdAt", allocationPlan.getCreatedAt());
            
            log.info("Allocation completed successfully. Plan ID: {}, Status: {}", 
                    allocationPlan.getId(), allocationPlan.getStatus());
            
            return ResponseHandler.success(
                "Allocation process completed successfully. Plan created with ID: " + allocationPlan.getId(),
                responseData
            );
            
        } catch (IllegalArgumentException e) {
            log.error("Allocation failed due to invalid input: {}", e.getMessage());
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
            
        } catch (Exception e) {
            log.error("Unexpected error during allocation process", e);
            return ResponseHandler.serverError(
                "Allocation process failed: " + e.getMessage(),
                Map.of()
            );
        }
    }

    /**
     * Gets the current status of an allocation plan.
     * Useful for checking the result after running allocation.
     * 
     * @param planId The ID of the allocation plan
     * @return ResponseEntity with plan status information
     */
    @Operation(
        summary = "Get Allocation Status",
        description = "Retrieves the current status of an allocation plan"
    )
    @GetMapping("/status/{planId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> getAllocationStatus(@PathVariable Long planId) {
        // This would call AllocationPlanService to get plan details
        // For now, returning a simple message
        return ResponseHandler.success(
            "Use /api/allocation-plans/" + planId + " to get full plan details",
            Map.of("planId", planId)
        );
    }
}
