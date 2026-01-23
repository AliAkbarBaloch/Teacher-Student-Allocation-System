package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintCreateDto;
import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintResponseDto;
import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintUpdateDto;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.entity.ZoneConstraint;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.ZoneConstraintMapper;
import de.unipassau.allocationsystem.repository.InternshipTypeRepository;
import de.unipassau.allocationsystem.repository.ZoneConstraintRepository;
import de.unipassau.allocationsystem.utils.PaginationUtils;
import de.unipassau.allocationsystem.utils.SearchSpecificationUtils;
import de.unipassau.allocationsystem.utils.SortFieldUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.domain.PageImpl;

/**
 * Service for managing zone constraints.
 * Handles CRUD operations with validation, audit logging, and business rules enforcement.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ZoneConstraintService implements CrudService<ZoneConstraint, Long> {

    private final ZoneConstraintRepository zoneConstraintRepository;
    private final InternshipTypeRepository internshipTypeRepository;
    private final ZoneConstraintMapper zoneConstraintMapper;

    @Override
    public List<Map<String, String>> getSortFields() {
        return SortFieldUtils.getSortFields("id", "zoneNumber", "isAllowed", "createdAt", "lastModified");
    }

    /**
     * Returns the list of sortable field keys.
     *
     * @return list of field keys
     */
    public List<String> getSortFieldKeys() {
        return getSortFields().stream().map(f -> f.get("key")).toList();
    }

    private Specification<ZoneConstraint> buildSearchSpecification(String searchValue) {
        return SearchSpecificationUtils.buildMultiFieldLikeSpecification(
                new String[]{"zoneNumber", "description", "internshipType.internshipCode", "internshipType.fullName"},
                searchValue
        );
    }

    @Override
    public boolean existsById(Long id) {
        return zoneConstraintRepository.findById(id).isPresent();
    }

    private String duplicateMessage(Integer zoneNumber, Long internshipTypeId) {
        return "Zone constraint already exists for zone " + zoneNumber + " and internship type " + internshipTypeId;
    }

    private void assertCompositeUniqueOnCreate(Integer zoneNumber, Long internshipTypeId) {
        if (zoneConstraintRepository.existsByZoneNumberAndInternshipTypeId(zoneNumber, internshipTypeId)) {
            throw new DuplicateResourceException(duplicateMessage(zoneNumber, internshipTypeId));
        }
    }

    private void assertCompositeUniqueOnUpdate(Integer zoneNumber, Long internshipTypeId, Long existingId) {
        if (zoneConstraintRepository.existsByZoneNumberAndInternshipTypeIdAndIdNot(zoneNumber, internshipTypeId, existingId)) {
            throw new DuplicateResourceException(duplicateMessage(zoneNumber, internshipTypeId));
        }
    }

    /**
     * Loads an existing zone constraint or throws if it doesn't exist.
     */
    private ZoneConstraint requireConstraint(Long id) {
        return zoneConstraintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone constraint not found with id: " + id));
    }

    /**
     * Applies field updates from data to existing entity.
     * Uses null-check-and-set pattern for all updatable fields.
     */
    private void applyFieldUpdates(ZoneConstraint existing, ZoneConstraint data) {
        if (data.getZoneNumber() != null) {
            existing.setZoneNumber(data.getZoneNumber());
        }
        if (data.getInternshipType() != null) {
            existing.setInternshipType(data.getInternshipType());
        }
        if (data.getIsAllowed() != null) {
            existing.setIsAllowed(data.getIsAllowed());
        }
        if (data.getDescription() != null) {
            existing.setDescription(data.getDescription());
        }
    }

    private boolean compositeChanged(ZoneConstraint existing, ZoneConstraint incoming) {
        if (incoming.getZoneNumber() == null || incoming.getInternshipType() == null) {
            return false;
        }
        boolean zoneChanged = !incoming.getZoneNumber().equals(existing.getZoneNumber());
        boolean typeChanged = incoming.getInternshipType().getId() != null
                && existing.getInternshipType() != null
                && existing.getInternshipType().getId() != null
                && !incoming.getInternshipType().getId().equals(existing.getInternshipType().getId());
        return zoneChanged || typeChanged;
    }

    /**
     * Internal create implementation to avoid self-invocation of transactional methods.
     */
    private ZoneConstraint createEntityInternal(ZoneConstraint zoneConstraint) {
        assertCompositeUniqueOnCreate(zoneConstraint.getZoneNumber(), zoneConstraint.getInternshipType().getId());
        return zoneConstraintRepository.save(zoneConstraint);
    }

    /**
     * Internal update implementation to avoid self-invocation of transactional methods.
     */
    private ZoneConstraint updateEntityInternal(Long id, ZoneConstraint data) {
        ZoneConstraint existing = requireConstraint(id);

        if (compositeChanged(existing, data)) {
            assertCompositeUniqueOnUpdate(data.getZoneNumber(), data.getInternshipType().getId(), id);
        }

        applyFieldUpdates(existing, data);
        return zoneConstraintRepository.save(existing);
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.ZONE_CONSTRAINT,
            description = "Viewed list of zone constraints",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Map<String, Object> getPaginated(Map<String, String> queryParams, String searchValue) {
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        Pageable pageable = PageRequest.of(params.page() - 1, params.pageSize(), sort);

        Page<ZoneConstraint> page = zoneConstraintRepository.findAll(buildSearchSpecification(searchValue), pageable);
        List<ZoneConstraintResponseDto> dtoItems = page.getContent().stream()
                .map(zoneConstraintMapper::toResponseDto)
                .toList();
        PageImpl<ZoneConstraintResponseDto> dtoPage = new PageImpl<>(dtoItems, page.getPageable(), page.getTotalElements());
        return PaginationUtils.formatPaginationResponse(dtoPage);
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.ZONE_CONSTRAINT,
            description = "Viewed all zone constraints",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public List<ZoneConstraint> getAll() {
        return getAllZoneConstraints();
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.ZONE_CONSTRAINT,
            description = "Viewed zone constraint by id",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Optional<ZoneConstraint> getById(Long id) {
        return getZoneConstraintById(id);
    }

    private List<ZoneConstraint> getAllZoneConstraints() {
        return zoneConstraintRepository.findAll();
    }

    private Optional<ZoneConstraint> getZoneConstraintById(Long id) {
        return zoneConstraintRepository.findById(id);
    }

    @Audited(
            action = AuditAction.CREATE,
            entityName = AuditEntityNames.ZONE_CONSTRAINT,
            description = "Created new zone constraint",
            captureNewValue = true
    )
    @Override
    public ZoneConstraint create(ZoneConstraint zoneConstraint) {
        return createEntityInternal(zoneConstraint);
    }

    /**
     * Create a new zone constraint from DTO.
     */
    public ZoneConstraintResponseDto create(ZoneConstraintCreateDto createDto) {
        InternshipType internshipType = internshipTypeRepository.findById(createDto.getInternshipTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Internship type not found with id: " + createDto.getInternshipTypeId()));

        ZoneConstraint constraint = zoneConstraintMapper.toEntityCreate(createDto);
        constraint.setInternshipType(internshipType);

        ZoneConstraint savedConstraint = createEntityInternal(constraint);
        return zoneConstraintMapper.toResponseDto(savedConstraint);
    }

    @Audited(
            action = AuditAction.UPDATE,
            entityName = AuditEntityNames.ZONE_CONSTRAINT,
            description = "Updated zone constraint",
            captureNewValue = true
    )
    @Override
    public ZoneConstraint update(Long id, ZoneConstraint data) {
        return updateEntityInternal(id, data);
    }

    /**
     * Update zone constraint from DTO.
     */
    public ZoneConstraintResponseDto update(Long id, ZoneConstraintUpdateDto updateDto) {
        ZoneConstraint existing = requireConstraint(id);

        if (updateDto.getInternshipTypeId() != null) {
            InternshipType internshipType = internshipTypeRepository.findById(updateDto.getInternshipTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Internship type not found with id: " + updateDto.getInternshipTypeId()));
            existing.setInternshipType(internshipType);
        }

        zoneConstraintMapper.updateEntityFromDto(updateDto, existing);

        ZoneConstraint updated = updateEntityInternal(id, existing);
        return zoneConstraintMapper.toResponseDto(updated);
    }

    @Audited(
            action = AuditAction.DELETE,
            entityName = AuditEntityNames.ZONE_CONSTRAINT,
            description = "Deleted zone constraint",
            captureNewValue = false
    )
    @Override
    public void delete(Long id) {
        // Use existsById to align with unit tests that mock existence checks.
        if (!zoneConstraintRepository.existsById(id)) {
            throw new ResourceNotFoundException("Zone constraint not found with id: " + id);
        }
        zoneConstraintRepository.deleteById(id);
    }
}
