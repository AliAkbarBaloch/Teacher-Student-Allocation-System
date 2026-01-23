package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.allocation.ImprovedTeacherAllocationService;
import de.unipassau.allocationsystem.allocation.TeacherAllocationService;
import de.unipassau.allocationsystem.dto.allocation.AllocationParameters;
import de.unipassau.allocationsystem.dto.allocation.AllocationRequestDto;
import de.unipassau.allocationsystem.entity.AllocationPlan;
import de.unipassau.allocationsystem.utils.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for executing the teacher allocation process.
 * Provides endpoints for administrators to trigger allocation runs from the frontend.
 */
@Slf4j
@RestController
@RequestMapping("/allocation")
@RequiredArgsConstructor
@Tag(name = "Allocation Execution", description = "Endpoints to trigger and manage teacher allocation process")
public class AllocationController {

    private final TeacherAllocationService teacherAllocationService;
    private final ImprovedTeacherAllocationService improvedTeacherAllocationService;

    /**
     * Triggers the allocation process for a specific academic year.
     * This endpoint is used by the Create Allocation Plan functionality.
     * 
     * @param academicYearId The ID of the academic year to allocate
     * @param requestBody Optional request body containing isCurrent and planVersion
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
        Boolean isCurrent;
        if (requestBody != null && requestBody.containsKey("isCurrent")) {
            isCurrent = (Boolean) requestBody.get("isCurrent");
        } else {
            isCurrent = false;
        }
        
        String customVersion;
        if (requestBody != null && requestBody.containsKey("planVersion")) {
            customVersion = (String) requestBody.get("planVersion");
        } else {
            customVersion = null;
        }
        log.info("Allocation process triggered from API for academic year ID: {}, isCurrent: {}, customVersion: {}", 
                academicYearId, isCurrent, customVersion);
        
        AllocationPlan allocationPlan = teacherAllocationService.performAllocation(academicYearId, isCurrent, customVersion);
        
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
    }

    /**
     * Triggers the improved allocation process with customizable parameters.
     * * @param academicYearId The ID of the academic year to allocate
     * @param requestDto Configuration for the algorithm (optional, defaults will be used)
     * @return ResponseEntity containing the created AllocationPlan details
     */
    @Operation(
            summary = "Run Improved Allocation Process",
            description = "Executes the bottleneck-aware allocation algorithm. " +
                    "Accepts parameters for scarcity handling, surplus utilization, and optimization weights."
    )
    @PostMapping("/run-improved/{academicYearId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> runImprovedAllocation(
            @Parameter(description = "ID of the Academic Year")
            @PathVariable Long academicYearId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Algorithm parameters and settings")
            @RequestBody(required = false) AllocationRequestDto requestDto) {

        // 1. Handle Null Request
        if (requestDto == null) {
            requestDto = new AllocationRequestDto(); // Use defaults
        }

        log.info("Allocation triggered for Year ID: {}. Scarcity: {}, Surplus: {}",
                academicYearId, requestDto.getPrioritizeScarcity(), requestDto.getForceUtilizationOfSurplus());

        // 2. Map DTO to Domain Parameters with defaults
        Boolean prioritizeScarcity = (requestDto.getPrioritizeScarcity() != null) 
                ? requestDto.getPrioritizeScarcity() : true;
        Boolean forceUtilizationOfSurplus = (requestDto.getForceUtilizationOfSurplus() != null) 
                ? requestDto.getForceUtilizationOfSurplus() : true;
        Boolean allowGroupSizeExpansion = (requestDto.getAllowGroupSizeExpansion() != null) 
                ? requestDto.getAllowGroupSizeExpansion() : true;
        Integer standardAssignments = (requestDto.getStandardAssignmentsPerTeacher() != null) 
                ? requestDto.getStandardAssignmentsPerTeacher() : 2;
        Integer maxAssignments = (requestDto.getMaxAssignmentsPerTeacher() != null) 
                ? requestDto.getMaxAssignmentsPerTeacher() : 3;
        Integer maxGroupWednesday = (requestDto.getMaxGroupSizeWednesday() != null) 
                ? requestDto.getMaxGroupSizeWednesday() : 4;
        Integer maxGroupBlock = (requestDto.getMaxGroupSizeBlock() != null) 
                ? requestDto.getMaxGroupSizeBlock() : 2;
        Integer weightMain = (requestDto.getWeightMainSubject() != null) 
                ? requestDto.getWeightMainSubject() : 10;
        Integer weightZone = (requestDto.getWeightZonePreference() != null) 
                ? requestDto.getWeightZonePreference() : 5;
        
        AllocationParameters params = AllocationParameters.builder()
                .prioritizeScarcity(prioritizeScarcity)
                .forceUtilizationOfSurplus(forceUtilizationOfSurplus)
                .allowGroupSizeExpansion(allowGroupSizeExpansion)
                .standardAssignmentsPerTeacher(standardAssignments)
                .maxAssignmentsPerTeacher(maxAssignments)
                .maxGroupSizeWednesday(maxGroupWednesday)
                .maxGroupSizeBlock(maxGroupBlock)
                .weightMainSubject(weightMain)
                .weightZonePreference(weightZone)
                .build();

        // 3. Execute Algorithm
        // Note: The improved service generates its own version number internally for consistency
        AllocationPlan allocationPlan = improvedTeacherAllocationService.performAllocation(academicYearId, params);

        // 4. Update metadata if passed (isCurrent/PlanVersion)
        // Since the service logic is strictly business logic, we can update specific user metadata here if needed
        // (Optional: Implement separate service method to update plan metadata if required)

        // 5. Construct Response
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("planId", allocationPlan.getId());
        responseData.put("planName", allocationPlan.getPlanName());
        responseData.put("planVersion", allocationPlan.getPlanVersion());
        responseData.put("status", allocationPlan.getStatus().name());
        responseData.put("academicYear", allocationPlan.getAcademicYear().getYearName());
        responseData.put("algorithmSettings", params); // Return settings used for verification

        return ResponseHandler.success(
                "Allocation completed successfully. Plan created.",
                responseData
        );
    }

    /**
     * Activates and approves an allocation plan.
     * Promotes a Draft plan to APPROVED status and updates the official Credit Hour Tracking table.
     * 
     * @param planId The ID of the allocation plan to activate
     * @return ResponseEntity confirming activation
     */
    @Operation(
            summary = "Activate/Approve Allocation Plan",
            description = "Promotes a Draft plan to APPROVED status. This updates the official Credit Hour Tracking table."
    )
    @PostMapping("/activate/{planId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> activatePlan(@PathVariable Long planId) {
        log.info("Activating plan ID: {}", planId);
        improvedTeacherAllocationService.activateAllocationPlan(planId);
        return ResponseHandler.success("Plan activated successfully. Official records updated.", Map.of("planId", planId));
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
