package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.SchoolRepository;
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

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.CriteriaBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import de.unipassau.allocationsystem.utils.SortFieldUtils;
// ...existing code...

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
/**
 * Service for managing school entities.
 * Handles CRUD operations and status management for schools.
 */
public class SchoolService implements CrudService<School, Long> {

    private final SchoolRepository schoolRepository;

    @Override
    public List<Map<String, String>> getSortFields() {
        return SortFieldUtils.getSortFields("id", "schoolName", "createdAt", "updatedAt");
    }

    /**
     * Returns the list of sortable field keys.
     * 
     * @return list of field keys
     */
    public List<String> getSortFieldKeys() {
        List<String> keys = new ArrayList<>();
        for (Map<String, String> field : getSortFields()) {
            keys.add(field.get("key"));
        }
        return keys;
    }


    // Helper methods for building filter predicates
    private Predicate buildSchoolNamePredicate(String searchValue, Root<School> root, CriteriaBuilder cb) {
        if (searchValue != null && !searchValue.trim().isEmpty()) {
            String likePattern = "%" + searchValue.trim().toLowerCase() + "%";
            return cb.like(cb.lower(root.get("schoolName")), likePattern);
        }
        return null;
    }

    private Predicate buildSchoolTypePredicate(String schoolTypeParam, Root<School> root, CriteriaBuilder cb) {
        if (schoolTypeParam != null && !schoolTypeParam.trim().isEmpty()) {
            try {
                School.SchoolType schoolType = School.SchoolType.valueOf(schoolTypeParam.toUpperCase());
                return cb.equal(root.get("schoolType"), schoolType);
            } catch (IllegalArgumentException e) {
                // Invalid school type, ignore filter
            }
        }
        return null;
    }

    private Predicate buildZoneNumberPredicate(String zoneNumberParam, Root<School> root, CriteriaBuilder cb) {
        if (zoneNumberParam != null && !zoneNumberParam.trim().isEmpty()) {
            try {
                Integer zoneNumber = Integer.parseInt(zoneNumberParam);
                return cb.equal(root.get("zoneNumber"), zoneNumber);
            } catch (NumberFormatException e) {
                // Invalid zone number, ignore filter
            }
        }
        return null;
    }

    private Predicate buildIsActivePredicate(String isActiveParam, Root<School> root, CriteriaBuilder cb) {
        if (isActiveParam != null && !isActiveParam.trim().isEmpty()) {
            try {
                Boolean isActive = Boolean.parseBoolean(isActiveParam);
                return cb.equal(root.get("isActive"), isActive);
            } catch (Exception e) {
                // Invalid boolean, ignore filter
            }
        }
        return null;
    }

    /**
     * Checks if a school with the given name exists.
     * 
     * @param schoolName the school name to check
     * @return true if school name exists, false otherwise
     */
    public boolean schoolNameExists(String schoolName) {
        return schoolRepository.findBySchoolName(schoolName).isPresent();
    }

    /**
     * Validates that the school name is unique, throws exception if duplicate found.
     * 
     * @param schoolName the school name to validate
     * @throws DuplicateResourceException if school name already exists
     */
    private void validateSchoolNameUniqueness(String schoolName) {
        if (schoolRepository.findBySchoolName(schoolName).isPresent()) {
            throw new DuplicateResourceException("School with name '" + schoolName + "' already exists");
        }
    }

    /**
     * Validates that a new school name doesn't conflict with existing records (for updates).
     * Allows the same name if it's the current school being updated.
     * 
     * @param newName the new school name
     * @param oldName the old school name
     * @throws DuplicateResourceException if new name conflicts with another school's name
     */
    private void validateSchoolNameForUpdate(String newName, String oldName) {
        if (!newName.equals(oldName) && schoolRepository.findBySchoolName(newName).isPresent()) {
            throw new DuplicateResourceException("School with name '" + newName + "' already exists");
        }
    }

    /**
     * Validates that a school exists with the given ID.
     * 
     * @param id the school ID
     * @throws ResourceNotFoundException if not found
     */
    private void validateExistence(Long id) {
        if (!schoolRepository.existsById(id)) {
            throw new ResourceNotFoundException("School not found with id: " + id);
        }
    }

    /**
     * Applies field updates from source to target school.
     * Only updates fields that are non-null in the source.
     * 
     * @param existing the target school to update
     * @param data the source data with new values
     */
    private void applyFieldUpdates(School existing, School data) {
        if (data.getSchoolName() != null) {
            validateSchoolNameForUpdate(data.getSchoolName(), existing.getSchoolName());
            existing.setSchoolName(data.getSchoolName());
        }
        if (data.getZoneNumber() != null) {
            existing.setZoneNumber(data.getZoneNumber());
        }
        if (data.getSchoolType() != null) {
            existing.setSchoolType(data.getSchoolType());
        }
        if (data.getIsActive() != null) {
            existing.setIsActive(data.getIsActive());
        }
        if (data.getAddress() != null) {
            existing.setAddress(data.getAddress());
        }
        if (data.getLatitude() != null) {
            existing.setLatitude(data.getLatitude());
        }
        if (data.getLongitude() != null) {
            existing.setLongitude(data.getLongitude());
        }
        if (data.getDistanceFromCenter() != null) {
            existing.setDistanceFromCenter(data.getDistanceFromCenter());
        }
        if (data.getTransportAccessibility() != null) {
            existing.setTransportAccessibility(data.getTransportAccessibility());
        }
        if (data.getContactEmail() != null) {
            existing.setContactEmail(data.getContactEmail());
        }
        if (data.getContactPhone() != null) {
            existing.setContactPhone(data.getContactPhone());
        }
    }

    @Override
    public boolean existsById(Long id) {
        return schoolRepository.findById(id).isPresent();
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.SCHOOL,
            description = "Viewed list of schools",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Map<String, Object> getPaginated(Map<String, String> queryParams, String searchValue) {
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        Pageable pageable = PageRequest.of(params.page() - 1, params.pageSize(), sort);

        Specification<School> spec = buildFilterSpecification(queryParams, searchValue);
        Page<School> page = schoolRepository.findAll(spec, pageable);

        return PaginationUtils.formatPaginationResponse(page);
    }

    private Specification<School> buildFilterSpecification(Map<String, String> queryParams, String searchValue) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Predicate schoolNamePredicate = buildSchoolNamePredicate(searchValue, root, cb);
            if (schoolNamePredicate != null) predicates.add(schoolNamePredicate);

            Predicate schoolTypePredicate = buildSchoolTypePredicate(queryParams.get("schoolType"), root, cb);
            if (schoolTypePredicate != null) predicates.add(schoolTypePredicate);

            Predicate zoneNumberPredicate = buildZoneNumberPredicate(queryParams.get("zoneNumber"), root, cb);
            if (zoneNumberPredicate != null) predicates.add(zoneNumberPredicate);

            Predicate isActivePredicate = buildIsActivePredicate(queryParams.get("isActive"), root, cb);
            if (isActivePredicate != null) predicates.add(isActivePredicate);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.SCHOOL,
            description = "Viewed all schools",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public List<School> getAll() {
        return schoolRepository.findAll();
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.SCHOOL,
            description = "Viewed school by id",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Optional<School> getById(Long id) {
        return schoolRepository.findById(id);
    }

    @Audited(
            action = AuditAction.CREATE,
            entityName = AuditEntityNames.SCHOOL,
            description = "Created new school",
            captureNewValue = true
    )
    @Transactional
    @Override
    public School create(School school) {
        validateSchoolNameUniqueness(school.getSchoolName());
        return schoolRepository.save(school);
    }

    @Audited(
            action = AuditAction.UPDATE,
            entityName = AuditEntityNames.SCHOOL,
            description = "Updated school",
            captureNewValue = true
    )
    @Transactional
    @Override
    public School update(Long id, School data) {
        School existing = schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + id));

        applyFieldUpdates(existing, data);
        return schoolRepository.save(existing);
    }

    @Audited(
            action = AuditAction.UPDATE,
            entityName = AuditEntityNames.SCHOOL,
            description = "Updated school status",
            captureNewValue = true
    )
    @Transactional
    /**
     * Updates the active status of a school.
     * 
     * @param id the school ID
     * @param isActive the new active status
     * @return updated school entity
     */
    public School updateStatus(Long id, Boolean isActive) {
        School existing = schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + id));
        existing.setIsActive(isActive);
        return schoolRepository.save(existing);
    }

    @Audited(
            action = AuditAction.DELETE,
            entityName = AuditEntityNames.SCHOOL,
            description = "Deleted school",
            captureNewValue = false
    )
    @Transactional
    @Override
    public void delete(Long id) {
        validateExistence(id);
        schoolRepository.deleteById(id);
    }
}