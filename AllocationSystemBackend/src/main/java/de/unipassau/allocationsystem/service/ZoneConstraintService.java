package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.entity.ZoneConstraint;
import de.unipassau.allocationsystem.repository.InternshipTypeRepository;
import de.unipassau.allocationsystem.repository.ZoneConstraintRepository;
import de.unipassau.allocationsystem.utils.PaginationUtils;
import de.unipassau.allocationsystem.utils.SortFieldUtils;
import de.unipassau.allocationsystem.utils.SearchSpecificationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintCreateDto;
import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintResponseDto;
import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintUpdateDto;
import de.unipassau.allocationsystem.mapper.ZoneConstraintMapper;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        return SortFieldUtils.getSortFields(
            "id", "zoneNumber", "isAllowed", "createdAt", "lastModified"
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

    private Specification<ZoneConstraint> buildSearchSpecification(String searchValue) {
        // Search across zoneNumber, description, internshipType.internshipCode, internshipType.fullName
        return SearchSpecificationUtils.buildMultiFieldLikeSpecification(
            new String[]{"zoneNumber", "description", "internshipType.internshipCode", "internshipType.fullName"}, searchValue
        );
    }

    @Override
    public boolean existsById(Long id) {
        return zoneConstraintRepository.findById(id).isPresent();
    }

    /**
     * Validates that a zone-internshipType combination is unique.
     * 
     * @param zoneNumber the zone number
     * @param internshipTypeId the internship type id
     * @throws DuplicateResourceException if combination already exists
     */
    private void validateCompositeUniqueness(Integer zoneNumber, Long internshipTypeId) {
        if (zoneConstraintRepository.existsByZoneNumberAndInternshipTypeId(zoneNumber, internshipTypeId)) {
            throw new DuplicateResourceException(
                "Zone constraint already exists for zone " + zoneNumber +
                " and internship type " + internshipTypeId
            );
        }
    }

    /**
     * Validates that a zone-internshipType combination can be updated to a new value.
     * Allows the same combination to remain unchanged, but ensures new combinations are unique.
     * 
     * @param zoneNumber the new zone number
     * @param internshipTypeId the new internship type id
     * @param existingId the id of the existing constraint
     * @throws DuplicateResourceException if new combination already exists (for different record)
     */
    private void validateCompositeUniquenessForUpdate(Integer zoneNumber, Long internshipTypeId, Long existingId) {
        if (zoneConstraintRepository.existsByZoneNumberAndInternshipTypeIdAndIdNot(
                zoneNumber, internshipTypeId, existingId)) {
            throw new DuplicateResourceException(
                "Zone constraint already exists for zone " + zoneNumber +
                " and internship type " + internshipTypeId
            );
        }
    }

    /**
     * Validates that a zone constraint exists by id.
     * 
     * @param id the id to validate
     * @return the existing ZoneConstraint
     * @throws ResourceNotFoundException if not found
     */
    private ZoneConstraint validateExistence(Long id) {
        return zoneConstraintRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Zone constraint not found with id: " + id
            ));
    }

    /**
     * Applies field updates from data to existing entity.
     * Uses null-check-and-set pattern for all updatable fields.
     * 
     * @param existing the entity to update
     * @param data the data containing new values
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

        Specification<ZoneConstraint> spec = buildSearchSpecification(searchValue);
        Page<ZoneConstraint> page = zoneConstraintRepository.findAll(spec, pageable);

        return PaginationUtils.formatPaginationResponse(page);
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
        return zoneConstraintRepository.findAll();
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
        return zoneConstraintRepository.findById(id);
    }

    @Audited(
            action = AuditAction.CREATE,
            entityName = AuditEntityNames.ZONE_CONSTRAINT,
            description = "Created new zone constraint",
            captureNewValue = true
    )
    @Transactional
    @Override
    public ZoneConstraint create(ZoneConstraint zoneConstraint) {
        validateCompositeUniqueness(zoneConstraint.getZoneNumber(), zoneConstraint.getInternshipType().getId());
        return zoneConstraintRepository.save(zoneConstraint);
    }

    /**
     * Create a new zone constraint from DTO.
     */
    @Transactional
    public ZoneConstraintResponseDto create(ZoneConstraintCreateDto createDto) {
        // Validate internship type exists
        InternshipType internshipType = internshipTypeRepository.findById(createDto.getInternshipTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Internship type not found with id: " + createDto.getInternshipTypeId()));

        // Create constraint entity
        ZoneConstraint constraint = zoneConstraintMapper.toEntityCreate(createDto);
        constraint.setInternshipType(internshipType);

        ZoneConstraint savedConstraint = create(constraint);
        return zoneConstraintMapper.toResponseDto(savedConstraint);
    }

    @Audited(
            action = AuditAction.UPDATE,
            entityName = AuditEntityNames.ZONE_CONSTRAINT,
            description = "Updated zone constraint",
            captureNewValue = true
    )
    @Transactional
    @Override
    public ZoneConstraint update(Long id, ZoneConstraint data) {
        ZoneConstraint existing = validateExistence(id);

        // Validate composite uniqueness if either field is being changed
        if (data.getZoneNumber() != null && data.getInternshipType() != null) {
            if (!data.getZoneNumber().equals(existing.getZoneNumber()) ||
                !data.getInternshipType().getId().equals(existing.getInternshipType().getId())) {
                validateCompositeUniquenessForUpdate(data.getZoneNumber(), data.getInternshipType().getId(), id);
            }
        }

        applyFieldUpdates(existing, data);
        return zoneConstraintRepository.save(existing);
    }

    /**
     * Update zone constraint from DTO.
     */
    @Transactional
    public ZoneConstraintResponseDto update(Long id, ZoneConstraintUpdateDto updateDto) {
        ZoneConstraint existing = zoneConstraintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone constraint not found with id: " + id));

        // Update internship type if provided
        if (updateDto.getInternshipTypeId() != null) {
            InternshipType internshipType = internshipTypeRepository.findById(updateDto.getInternshipTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Internship type not found with id: " + updateDto.getInternshipTypeId()));
            existing.setInternshipType(internshipType);
        }

        // Update other fields
        zoneConstraintMapper.updateEntityFromDto(updateDto, existing);

        ZoneConstraint updated = update(id, existing);
        return zoneConstraintMapper.toResponseDto(updated);
    }

    @Audited(
            action = AuditAction.DELETE,
            entityName = AuditEntityNames.ZONE_CONSTRAINT,
            description = "Deleted zone constraint",
            captureNewValue = false
    )
    @Transactional
    @Override
    public void delete(Long id) {
        validateExistence(id);
        zoneConstraintRepository.deleteById(id);
    }
}
