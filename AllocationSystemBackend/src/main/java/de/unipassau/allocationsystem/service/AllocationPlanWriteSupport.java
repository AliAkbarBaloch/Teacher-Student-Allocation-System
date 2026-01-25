package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.constant.PlanChangeTypes;
import de.unipassau.allocationsystem.dto.allocationplan.AllocationPlanCreateDto;
import de.unipassau.allocationsystem.dto.allocationplan.AllocationPlanUpdateDto;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.AllocationPlan;
import de.unipassau.allocationsystem.entity.AllocationPlan.PlanStatus;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.AllocationPlanMapper;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.AllocationPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Helper component for allocation plan write operations.
 * Encapsulates validation, persistence, and change logging logic.
 */
@Component
@RequiredArgsConstructor
class AllocationPlanWriteSupport {

    private final AllocationPlanRepository allocationPlanRepository;
    private final AcademicYearRepository academicYearRepository;
    private final AllocationPlanMapper allocationPlanMapper;
    private final PlanChangeLogService planChangeLogService;

    /**
     * Retrieve academic year by ID or throw exception.
     *
     * @param yearId the academic year ID
     * @return the academic year entity
     * @throws ResourceNotFoundException if not found
     */
    AcademicYear requireAcademicYear(Long yearId) {
        return academicYearRepository.findById(yearId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with ID: " + yearId));
    }

    /**
     * Validate that plan version is unique for the given academic year.
     *
     * @param yearId the academic year ID
     * @param planVersion the plan version
     * @throws DuplicateResourceException if version already exists
     */
    void requireUniqueVersion(Long yearId, String planVersion) {
        boolean exists = allocationPlanRepository.existsByAcademicYearIdAndPlanVersion(yearId, planVersion);
        if (exists) {
            throw new DuplicateResourceException(
                    "Allocation plan with version '" + planVersion + "' already exists for this academic year"
            );
        }
    }

    /**
     * Create and persist a new allocation plan.
     *
     * @param dto the creation DTO
     * @param year the academic year entity
     * @return the saved allocation plan
     */
    AllocationPlan createAndSavePlan(AllocationPlanCreateDto dto, AcademicYear year) {
        AllocationPlan plan = new AllocationPlan();
        plan.setAcademicYear(year);
        plan.setPlanName(dto.getPlanName());
        plan.setPlanVersion(dto.getPlanVersion());
        plan.setStatus(dto.getStatus());
        plan.setIsCurrent(Boolean.TRUE.equals(dto.getIsCurrent()));
        plan.setNotes(dto.getNotes());
        return allocationPlanRepository.save(plan);
    }

    /**
     * Log the creation of an allocation plan.
     *
     * @param saved the saved allocation plan
     */
    void logCreateChange(AllocationPlan saved) {
        planChangeLogService.logPlanChange(
                saved.getId(),
                PlanChangeTypes.CREATE,
                AuditEntityNames.ALLOCATION_PLAN,
                saved.getId(),
                null,
                allocationPlanMapper.toResponseDto(saved),
                "Created allocation plan"
        );
    }

    /**
     * Retrieve allocation plan by ID or throw exception.
     *
     * @param id the plan ID
     * @return the allocation plan entity
     * @throws ResourceNotFoundException if not found
     */
    AllocationPlan requirePlan(Long id) {
        return allocationPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation plan not found with ID: " + id));
    }

    /**
     * Extract and validate academic year from plan.
     *
     * @param planId the plan ID
     * @param plan the allocation plan entity
     * @return the academic year entity
     * @throws IllegalStateException if academic year is null
     */
    AcademicYear requirePlanAcademicYear(Long planId, AllocationPlan plan) {
        AcademicYear year = plan.getAcademicYear();
        if (year == null) {
            throw new IllegalStateException("Allocation plan has no associated academic year: " + planId);
        }
        return year;
    }

    /**
     * Validate that plan can be updated.
     *
     * @param plan the allocation plan entity
     * @throws IllegalArgumentException if plan is archived
     */
    void assertUpdatable(AllocationPlan plan) {
        if (plan.getStatus() == PlanStatus.ARCHIVED) {
            throw new IllegalArgumentException("Cannot update archived allocation plan. Create a new version instead.");
        }
    }

    /**
     * Apply the isCurrent flag if the update DTO requests it.
     *
     * @param id the plan ID
     * @param plan the allocation plan entity
     * @param updateDto the update DTO
     */
    void applyCurrentFlagIfNeeded(Long id, AllocationPlan plan, AllocationPlanUpdateDto updateDto) {
        if (Boolean.TRUE.equals(updateDto.getIsCurrent()) && !Boolean.TRUE.equals(plan.getIsCurrent())) {
            allocationPlanRepository.unsetCurrentForYearExcept(plan.getAcademicYear().getId(), id);
        }
    }

    /**
     * Log the update of an allocation plan.
     *
     * @param updated the updated allocation plan
     * @param oldDto the previous state as DTO
     */
    void logUpdateChange(AllocationPlan updated, Object oldDto) {
        planChangeLogService.logPlanChange(
                updated.getId(),
                PlanChangeTypes.UPDATE,
                AuditEntityNames.ALLOCATION_PLAN,
                updated.getId(),
                oldDto,
                allocationPlanMapper.toResponseDto(updated),
                "Updated allocation plan"
        );
    }

    /**
     * Validate that plan is not archived for setting as current.
     *
     * @param plan the allocation plan entity
     * @throws IllegalArgumentException if plan is archived
     */
    void assertNotArchivedForCurrent(AllocationPlan plan) {
        if (plan.getStatus() == PlanStatus.ARCHIVED) {
            throw new IllegalArgumentException("Cannot set archived plan as current. Restore it first.");
        }
    }

    /**
     * Validate that plan is not already archived.
     *
     * @param plan the allocation plan entity
     * @throws IllegalArgumentException if plan is already archived
     */
    void assertNotAlreadyArchived(AllocationPlan plan) {
        if (plan.getStatus() == PlanStatus.ARCHIVED) {
            throw new IllegalArgumentException("Plan is already archived");
        }
    }
}
