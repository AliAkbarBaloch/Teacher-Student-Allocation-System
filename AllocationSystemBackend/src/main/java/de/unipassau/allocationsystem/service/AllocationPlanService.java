package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.dto.AllocationPlanCreateDto;
import de.unipassau.allocationsystem.dto.AllocationPlanResponseDto;
import de.unipassau.allocationsystem.dto.AllocationPlanUpdateDto;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.AllocationPlan;
import de.unipassau.allocationsystem.entity.AllocationPlan.PlanStatus;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.AllocationPlanMapper;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.AllocationPlanRepository;
import de.unipassau.allocationsystem.repository.UserRepository;
import de.unipassau.allocationsystem.utils.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

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
    private final UserRepository userRepository;
    private final AllocationPlanMapper allocationPlanMapper;

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

        if (yearId == null) {
            throw new IllegalArgumentException("yearId parameter is required");
        }

        // Validate pagination parameters
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);

        // Build specification for filtering
        Specification<AllocationPlan> spec = (root, query, cb) -> 
            cb.equal(root.get("academicYear").get("id"), yearId);

        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        if (isCurrent != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isCurrent"), isCurrent));
        }

        // Create pageable with sorting
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        Pageable pageable = PageRequest.of(params.page() - 1, params.pageSize(), sort);

        // Fetch paginated results
        Page<AllocationPlan> page = allocationPlanRepository.findAll(spec, pageable);
        log.info("Found {} allocation plans (page {} of {})", 
                page.getNumberOfElements(), page.getNumber() + 1, page.getTotalPages());

        // Convert to DTOs
        Page<AllocationPlanResponseDto> dtoPage = page.map(allocationPlanMapper::toResponseDto);

        // Return paginated response
        return PaginationUtils.formatPaginationResponse(dtoPage);
    }

    /**
     * Get a specific allocation plan by ID.
     */
    @Transactional(readOnly = true)
    public AllocationPlanResponseDto getPlanById(Long id) {
        log.info("Fetching allocation plan with ID: {}", id);

        AllocationPlan plan = allocationPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Allocation plan not found with ID: " + id));

        return allocationPlanMapper.toResponseDto(plan);
    }

    /**
     * Create a new allocation plan.
     */
    @Audited(
        action = AuditAction.CREATE,
        entityName = "ALLOCATION_PLAN",
        description = "Created new allocation plan",
        captureNewValue = true
    )
    @Transactional
    public AllocationPlanResponseDto createPlan(AllocationPlanCreateDto createDto) {
        log.info("Creating allocation plan: {} v{} for year ID: {}", 
                createDto.getPlanName(), createDto.getPlanVersion(), createDto.getYearId());

        // Validate academic year exists
        AcademicYear academicYear = academicYearRepository.findById(createDto.getYearId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Academic year not found with ID: " + createDto.getYearId()));

        // Validate creator user exists and is active
        User createdByUser = userRepository.findById(createDto.getCreatedByUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + createDto.getCreatedByUserId()));

        if (!createdByUser.isEnabled()) {
            throw new IllegalArgumentException(
                    "Cannot create allocation plan with inactive user: " + createdByUser.getEmail());
        }

        // Check uniqueness of (yearId, planVersion)
        if (allocationPlanRepository.existsByAcademicYearIdAndPlanVersion(
                createDto.getYearId(), createDto.getPlanVersion())) {
            throw new DuplicateResourceException(
                    "Allocation plan with version '" + createDto.getPlanVersion() + 
                    "' already exists for this academic year");
        }

        // If isCurrent = true, unset current for other plans of the same year
        if (Boolean.TRUE.equals(createDto.getIsCurrent())) {
            allocationPlanRepository.unsetCurrentForYear(createDto.getYearId());
            log.info("Unset is_current for other plans of year ID: {}", createDto.getYearId());
        }

        // Create the allocation plan
        AllocationPlan plan = new AllocationPlan();
        plan.setAcademicYear(academicYear);
        plan.setPlanName(createDto.getPlanName());
        plan.setPlanVersion(createDto.getPlanVersion());
        plan.setStatus(createDto.getStatus());
        plan.setCreatedByUser(createdByUser);
        plan.setIsCurrent(createDto.getIsCurrent() != null ? createDto.getIsCurrent() : false);
        plan.setNotes(createDto.getNotes());

        AllocationPlan saved = allocationPlanRepository.save(plan);
        log.info("Allocation plan created successfully with ID: {}", saved.getId());

        return allocationPlanMapper.toResponseDto(saved);
    }

    /**
     * Update an existing allocation plan.
     */
    @Audited(
        action = AuditAction.UPDATE,
        entityName = "ALLOCATION_PLAN",
        description = "Updated allocation plan",
        captureNewValue = true
    )
    @Transactional
    public AllocationPlanResponseDto updatePlan(Long id, AllocationPlanUpdateDto updateDto) {
        log.info("Updating allocation plan with ID: {}", id);

        AllocationPlan plan = allocationPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Allocation plan not found with ID: " + id));

        // Check if plan is locked (APPROVED or ARCHIVED)
        // According to requirements, admin can still edit approved plans
        if (plan.getStatus() == PlanStatus.ARCHIVED) {
            throw new IllegalArgumentException(
                    "Cannot update archived allocation plan. Create a new version instead.");
        }

        // If changing status to APPROVED, validate business rules
        if (updateDto.getStatus() != null && updateDto.getStatus() == PlanStatus.APPROVED) {
            if (plan.getStatus() != PlanStatus.IN_REVIEW && plan.getStatus() != PlanStatus.DRAFT) {
                log.warn("Changing plan status directly to APPROVED from {}", plan.getStatus());
            }
        }

        // If setting isCurrent = true, unset current for other plans of the same year
        if (Boolean.TRUE.equals(updateDto.getIsCurrent()) && !plan.getIsCurrent()) {
            allocationPlanRepository.unsetCurrentForYearExcept(
                    plan.getAcademicYear().getId(), id);
            log.info("Unset is_current for other plans of year ID: {} except plan ID: {}", 
                    plan.getAcademicYear().getId(), id);
        }

        // Update the plan
        allocationPlanMapper.updateEntityFromDto(plan, updateDto);
        AllocationPlan updated = allocationPlanRepository.save(plan);

        log.info("Allocation plan updated successfully with ID: {}", updated.getId());
        return allocationPlanMapper.toResponseDto(updated);
    }

    /**
     * Set a specific plan as the current plan for its academic year.
     * This will unset is_current on all other plans for the same year.
     */
    @Audited(
        action = AuditAction.UPDATE,
        entityName = "ALLOCATION_PLAN",
        description = "Set allocation plan as current",
        captureNewValue = true
    )
    @Transactional
    public AllocationPlanResponseDto setCurrentPlan(Long id) {
        log.info("Setting allocation plan ID: {} as current", id);

        AllocationPlan plan = allocationPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Allocation plan not found with ID: " + id));

        // Validate that plan is in appropriate status to be set as current
        if (plan.getStatus() == PlanStatus.ARCHIVED) {
            throw new IllegalArgumentException(
                    "Cannot set archived plan as current. Restore it first.");
        }

        // Unset current for all other plans of the same year
        allocationPlanRepository.unsetCurrentForYearExcept(
                plan.getAcademicYear().getId(), id);

        // Set this plan as current
        plan.setIsCurrent(true);
        AllocationPlan updated = allocationPlanRepository.save(plan);

        log.info("Allocation plan ID: {} set as current for year ID: {}", 
                id, plan.getAcademicYear().getId());

        return allocationPlanMapper.toResponseDto(updated);
    }

    /**
     * Archive an allocation plan (soft delete).
     * Sets status to ARCHIVED and removes is_current flag.
     */
    @Audited(
        action = AuditAction.UPDATE,
        entityName = "ALLOCATION_PLAN",
        description = "Archived allocation plan",
        captureNewValue = true
    )
    @Transactional
    public AllocationPlanResponseDto archivePlan(Long id) {
        log.info("Archiving allocation plan with ID: {}", id);

        AllocationPlan plan = allocationPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Allocation plan not found with ID: " + id));

        if (plan.getStatus() == PlanStatus.ARCHIVED) {
            throw new IllegalArgumentException("Plan is already archived");
        }

        // If this was the current plan, unset it
        if (plan.getIsCurrent()) {
            plan.setIsCurrent(false);
            log.info("Removed is_current flag from archived plan");
        }

        plan.setStatus(PlanStatus.ARCHIVED);
        AllocationPlan updated = allocationPlanRepository.save(plan);

        log.info("Allocation plan archived successfully with ID: {}", id);
        return allocationPlanMapper.toResponseDto(updated);
    }

    /**
     * Get the current allocation plan for a specific academic year.
     */
    @Transactional(readOnly = true)
    public AllocationPlanResponseDto getCurrentPlanForYear(Long yearId) {
        log.info("Fetching current allocation plan for year ID: {}", yearId);

        AllocationPlan plan = allocationPlanRepository.findByAcademicYearIdAndIsCurrentTrue(yearId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No current allocation plan found for academic year ID: " + yearId));

        return allocationPlanMapper.toResponseDto(plan);
    }
}
