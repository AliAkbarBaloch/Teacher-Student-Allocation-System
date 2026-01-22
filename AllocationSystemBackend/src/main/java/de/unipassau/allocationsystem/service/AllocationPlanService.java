package de.unipassau.allocationsystem.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.unipassau.allocationsystem.allocation.TeacherAllocationService;
import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.constant.PlanChangeTypes;
import de.unipassau.allocationsystem.dto.allocationplan.AllocationPlanCreateDto;
import de.unipassau.allocationsystem.dto.allocationplan.AllocationPlanResponseDto;
import de.unipassau.allocationsystem.dto.allocationplan.AllocationPlanUpdateDto;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.AllocationPlan;
import de.unipassau.allocationsystem.entity.AllocationPlan.PlanStatus;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.AllocationPlanMapper;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.AllocationPlanRepository;
import de.unipassau.allocationsystem.utils.PaginationUtils;
import de.unipassau.allocationsystem.utils.SortFieldUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing allocation plans with versioning and status workflow.
 * Includes validation, audit logging, and business rules enforcement.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AllocationPlanService {

    private final AllocationPlanRepository allocationPlanRepository;
    private final AcademicYearRepository academicYearRepository;
    private final AllocationPlanMapper allocationPlanMapper;
    private final PlanChangeLogService planChangeLogService;
    private final TeacherAllocationService teacherAllocationService;

    private final AllocationPlanWriteSupport writeSupport;

    /**
     * Get available sort fields for allocation plans.
     *
     * @return list of sortable fields
     */
    public List<Map<String, String>> getSortFields() {
        return SortFieldUtils.getSortFields(
                "id", "planName", "planVersion", "status", "isCurrent", "createdAt", "updatedAt"
        );
    }

    /**
     * Returns the list of sortable field keys.
     *
     * @return list of field keys
     */
    public List<String> getSortFieldKeys() {
        return getSortFields().stream().map(f -> f.get("key")).toList();
    }

    /**
     * Retrieves all allocation plans without pagination.
     *
     * @return list of all allocation plans
     */
    @Transactional(readOnly = true)
    public List<AllocationPlan> getAll() {
        return allocationPlanRepository.findAll();
    }

    /**
     * Get all allocation plans with filtering and pagination.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAllPlans(
            Long yearId,
            PlanStatus status,
            Boolean isCurrent,
            Map<String, String> queryParams) {

        log.info("Fetching allocation plans - yearId: {}, status: {}, isCurrent: {}",
                yearId, status, isCurrent);

        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);

        Specification<AllocationPlan> spec = Specification.allOf();

        if (yearId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("academicYear").get("id"), yearId));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (isCurrent != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isCurrent"), isCurrent));
        }

        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        Pageable pageable = PageRequest.of(params.page() - 1, params.pageSize(), sort);

        Page<AllocationPlan> page = allocationPlanRepository.findAll(spec, pageable);
        log.info("Found {} allocation plans (page {} of {})",
                page.getNumberOfElements(), page.getNumber() + 1, page.getTotalPages());

        Page<AllocationPlanResponseDto> dtoPage = page.map(allocationPlanMapper::toResponseDto);
        return PaginationUtils.formatPaginationResponse(dtoPage);
    }

    /**
     * Get a specific allocation plan by ID.
     */
    @Transactional(readOnly = true)
    public AllocationPlanResponseDto getPlanById(Long id) {
        log.info("Fetching allocation plan with ID: {}", id);

        AllocationPlan plan = allocationPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation plan not found with ID: " + id));

        return allocationPlanMapper.toResponseDto(plan);
    }

    /**
     * Create a new allocation plan.
     */
    @Audited(
            action = AuditAction.CREATE,
            entityName = AuditEntityNames.ALLOCATION_PLAN,
            description = "Created new allocation plan",
            captureNewValue = true
    )
    @Transactional
    public AllocationPlanResponseDto createPlan(AllocationPlanCreateDto createDto) {
        AcademicYear year = writeSupport.requireAcademicYear(createDto.getYearId());
        writeSupport.requireUniqueVersion(createDto.getYearId(), createDto.getPlanVersion());

        if (Boolean.TRUE.equals(createDto.getIsCurrent())) {
            allocationPlanRepository.unsetCurrentForYear(createDto.getYearId());
        }

        AllocationPlan saved = writeSupport.createAndSavePlan(createDto, year);

        writeSupport.logCreateChange(saved);
        return allocationPlanMapper.toResponseDto(saved);
    }

    /**
     * Run the allocation algorithm for a specific allocation plan.
     *
     * @param planId ID of the allocation plan
     * @return ID of the newly created allocation plan
     */
    @Audited(
            action = AuditAction.UPDATE,
            entityName = AuditEntityNames.ALLOCATION_PLAN,
            description = "Run allocation algorithm for plan"
    )
    public Long runAllocationForPlan(Long planId) {
        log.info("Running allocation algorithm for plan ID: {}", planId);

        AllocationPlan plan = allocationPlanRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Allocation plan not found with id: " + planId));

        AcademicYear academicYear = writeSupport.requirePlanAcademicYear(planId, plan);

        AllocationPlan newPlan = teacherAllocationService.performAllocation(academicYear.getId());
        log.info("Allocation algorithm completed successfully - new plan created with ID: {}", newPlan.getId());
        return newPlan.getId();
    }

    /**
     * Update an existing allocation plan.
     */
    @Audited(
            action = AuditAction.UPDATE,
            entityName = AuditEntityNames.ALLOCATION_PLAN,
            description = "Updated allocation plan",
            captureNewValue = true
    )
    @Transactional
    public AllocationPlanResponseDto updatePlan(Long id, AllocationPlanUpdateDto updateDto) {
        AllocationPlan plan = writeSupport.requirePlan(id);

        writeSupport.assertUpdatable(plan);
        writeSupport.applyCurrentFlagIfNeeded(id, plan, updateDto);

        var oldDto = allocationPlanMapper.toResponseDto(plan);

        allocationPlanMapper.updateEntityFromDto(updateDto, plan);
        AllocationPlan updated = allocationPlanRepository.save(plan);

        writeSupport.logUpdateChange(updated, oldDto);
        return allocationPlanMapper.toResponseDto(updated);
    }

    /**
     * Set a specific plan as the current plan for its academic year.
     * This will unset is_current on all other plans for the same year.
     */
    @Audited(
            action = AuditAction.UPDATE,
            entityName = AuditEntityNames.ALLOCATION_PLAN,
            description = "Set allocation plan as current",
            captureNewValue = true
    )
    @Transactional
    public AllocationPlanResponseDto setCurrentPlan(Long id) {
        AllocationPlan plan = writeSupport.requirePlan(id);
        writeSupport.assertNotArchivedForCurrent(plan);

        allocationPlanRepository.unsetCurrentForYearExcept(plan.getAcademicYear().getId(), id);

        plan.setIsCurrent(true);
        AllocationPlan updated = allocationPlanRepository.save(plan);

        planChangeLogService.logPlanChange(
                updated.getId(),
                PlanChangeTypes.STATUS_CHANGE,
                AuditEntityNames.ALLOCATION_PLAN,
                updated.getId(),
                "isCurrent=false",
                "isCurrent=true",
                "Set as current plan"
        );

        return allocationPlanMapper.toResponseDto(updated);
    }

    /**
     * Archive an allocation plan (soft delete).
     * Sets status to ARCHIVED and removes is_current flag.
     */
    @Audited(
            action = AuditAction.UPDATE,
            entityName = AuditEntityNames.ALLOCATION_PLAN,
            description = "Archived allocation plan",
            captureNewValue = true
    )
    @Transactional
    public AllocationPlanResponseDto archivePlan(Long id) {
        AllocationPlan plan = writeSupport.requirePlan(id);

        writeSupport.assertNotAlreadyArchived(plan);

        if (Boolean.TRUE.equals(plan.getIsCurrent())) {
            plan.setIsCurrent(false);
        }

        PlanStatus previous = plan.getStatus();
        plan.setStatus(PlanStatus.ARCHIVED);
        AllocationPlan updated = allocationPlanRepository.save(plan);

        planChangeLogService.logPlanChange(
                updated.getId(),
                PlanChangeTypes.STATUS_CHANGE,
                AuditEntityNames.ALLOCATION_PLAN,
                updated.getId(),
                "status=" + previous,
                "status=" + AllocationPlan.PlanStatus.ARCHIVED,
                "Archived allocation plan"
        );

        return allocationPlanMapper.toResponseDto(updated);
    }

    /**
     * Get the current allocation plan for a specific academic year.
     */
    @Transactional(readOnly = true)
    public AllocationPlanResponseDto getCurrentPlanForYear(Long yearId) {
        AllocationPlan plan = allocationPlanRepository.findByAcademicYearIdAndIsCurrentTrue(yearId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No current allocation plan found for academic year ID: " + yearId));

        return allocationPlanMapper.toResponseDto(plan);
    }
}
