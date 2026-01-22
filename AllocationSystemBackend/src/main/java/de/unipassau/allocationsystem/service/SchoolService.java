package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.SchoolRepository;
import de.unipassau.allocationsystem.utils.PaginationUtils;
import de.unipassau.allocationsystem.utils.SortFieldUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        return getSortFields().stream().map(f -> f.get("key")).toList();
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

    @Override
    public boolean existsById(Long id) {
        return schoolRepository.existsById(id);
    }

    private School getExistingOrThrow(Long id) {
        return schoolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + id));
    }

    private void ensureUniqueName(String schoolName, Long currentId) {
        schoolRepository.findBySchoolName(schoolName).ifPresent(found -> {
            boolean differentRecord = currentId == null || found.getId() == null || !found.getId().equals(currentId);
            if (differentRecord) {
                throw new DuplicateResourceException("School with name '" + schoolName + "' already exists");
            }
        });
    }

    private void updateNameIfChanged(School existing, School data) {
        String incoming = data.getSchoolName();
        if (incoming == null || incoming.equals(existing.getSchoolName())) {
            return;
        }
        ensureUniqueName(incoming, existing.getId());
        existing.setSchoolName(incoming);
    }

    private void updateZoneIfProvided(School existing, School data) {
        if (data.getZoneNumber() != null) {
            existing.setZoneNumber(data.getZoneNumber());
        }
    }

    private void updateTypeIfProvided(School existing, School data) {
        if (data.getSchoolType() != null) {
            existing.setSchoolType(data.getSchoolType());
        }
    }

    private void updateActiveIfProvided(School existing, School data) {
        if (data.getIsActive() != null) {
            existing.setIsActive(data.getIsActive());
        }
    }

    private void updateAddressIfProvided(School existing, School data) {
        if (data.getAddress() != null) {
            existing.setAddress(data.getAddress());
        }
    }

    private void updateGeoIfProvided(School existing, School data) {
        if (data.getLatitude() != null) {
            existing.setLatitude(data.getLatitude());
        }
        if (data.getLongitude() != null) {
            existing.setLongitude(data.getLongitude());
        }
    }

    private void updateDistanceIfProvided(School existing, School data) {
        if (data.getDistanceFromCenter() != null) {
            existing.setDistanceFromCenter(data.getDistanceFromCenter());
        }
    }

    private void updateTransportIfProvided(School existing, School data) {
        if (data.getTransportAccessibility() != null) {
            existing.setTransportAccessibility(data.getTransportAccessibility());
        }
    }

    private void updateContactsIfProvided(School existing, School data) {
        if (data.getContactEmail() != null) {
            existing.setContactEmail(data.getContactEmail());
        }
        if (data.getContactPhone() != null) {
            existing.setContactPhone(data.getContactPhone());
        }
    }

    /**
     * Applies field updates from source to target school.
     * Only updates fields that are non-null in the source.
     */
    private void applyFieldUpdates(School existing, School data) {
        updateNameIfChanged(existing, data);
        updateZoneIfProvided(existing, data);
        updateTypeIfProvided(existing, data);
        updateActiveIfProvided(existing, data);
        updateAddressIfProvided(existing, data);
        updateGeoIfProvided(existing, data);
        updateDistanceIfProvided(existing, data);
        updateTransportIfProvided(existing, data);
        updateContactsIfProvided(existing, data);
    }

    private Pageable toPageable(Map<String, String> queryParams) {
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        return PageRequest.of(
                params.page() - 1,
                params.pageSize(),
                Sort.by(params.sortOrder(), params.sortBy())
        );
    }

    private Specification<School> buildFilterSpecification(Map<String, String> queryParams, String searchValue) {
        return (root, query, cb) -> cb.and(collectPredicates(queryParams, searchValue, root, cb)
                .toArray(new Predicate[0]));
    }

    private List<Predicate> collectPredicates(Map<String, String> queryParams, String searchValue,
                                              Root<School> root, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        addSearchPredicate(predicates, root, cb, searchValue);
        addSchoolTypePredicate(predicates, root, cb, queryParams.get("schoolType"));
        addZonePredicate(predicates, root, cb, queryParams.get("zoneNumber"));
        addActivePredicate(predicates, root, cb, queryParams.get("isActive"));

        return predicates;
    }

    private void addSearchPredicate(List<Predicate> predicates, Root<School> root, CriteriaBuilder cb,
                                    String searchValue) {
        if (searchValue == null || searchValue.trim().isEmpty()) {
            return;
        }
        String likePattern = "%" + searchValue.trim().toLowerCase() + "%";
        predicates.add(cb.like(cb.lower(root.get("schoolName")), likePattern));
    }

    private void addSchoolTypePredicate(List<Predicate> predicates, Root<School> root, CriteriaBuilder cb,
                                        String schoolTypeParam) {
        Optional<School.SchoolType> schoolType = parseSchoolType(schoolTypeParam);
        schoolType.ifPresent(t -> predicates.add(cb.equal(root.get("schoolType"), t)));
    }

    private Optional<School.SchoolType> parseSchoolType(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return Optional.empty();
        }
        String normalized = raw.trim().toUpperCase();
        for (School.SchoolType t : School.SchoolType.values()) {
            if (t.name().equals(normalized)) {
                return Optional.of(t);
            }
        }
        return Optional.empty();
    }

    private void addZonePredicate(List<Predicate> predicates, Root<School> root, CriteriaBuilder cb,
                                  String zoneNumberParam) {
        Optional<Integer> zone = parseInteger(zoneNumberParam);
        zone.ifPresent(z -> predicates.add(cb.equal(root.get("zoneNumber"), z)));
    }

    private Optional<Integer> parseInteger(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(raw.trim()));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    private void addActivePredicate(List<Predicate> predicates, Root<School> root, CriteriaBuilder cb,
                                    String isActiveParam) {
        if (isActiveParam == null || isActiveParam.trim().isEmpty()) {
            return;
        }
        boolean isActive = Boolean.parseBoolean(isActiveParam.trim());
        predicates.add(cb.equal(root.get("isActive"), isActive));
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
        Pageable pageable = toPageable(queryParams);
        Specification<School> spec = buildFilterSpecification(queryParams, searchValue);
        Page<School> page = schoolRepository.findAll(spec, pageable);
        return PaginationUtils.formatPaginationResponse(page);
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
    @Override
    public School create(School school) {
        ensureUniqueName(school.getSchoolName(), null);
        return schoolRepository.save(school);
    }

    @Audited(
            action = AuditAction.UPDATE,
            entityName = AuditEntityNames.SCHOOL,
            description = "Updated school",
            captureNewValue = true
    )
    @Override
    public School update(Long id, School data) {
        School existing = getExistingOrThrow(id);
        applyFieldUpdates(existing, data);
        return schoolRepository.save(existing);
    }

    /**
     * Updates the active status of a school.
     *
     * @param id       the school ID
     * @param isActive the new active status
     * @return updated school entity
     */
    @Audited(
            action = AuditAction.UPDATE,
            entityName = AuditEntityNames.SCHOOL,
            description = "Updated school status",
            captureNewValue = true
    )
    public School updateStatus(Long id, Boolean isActive) {
        // Delegate to update(...) to avoid duplicated "load-save" logic.
        School patch = new School();
        patch.setIsActive(isActive);
        return update(id, patch);
    }

    @Audited(
            action = AuditAction.DELETE,
            entityName = AuditEntityNames.SCHOOL,
            description = "Deleted school",
            captureNewValue = false
    )
    @Override
    public void delete(Long id) {
        getExistingOrThrow(id);
        schoolRepository.deleteById(id);
    }
}
