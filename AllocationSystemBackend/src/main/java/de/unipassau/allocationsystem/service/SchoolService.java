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
import java.util.function.Consumer;

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
     * Returns the list of sortable field keys exposed by {@link #getSortFields()}.
     *
     * @return list of sort field keys
     */
    public List<String> getSortFieldKeys() {
        return getSortFields().stream().map(f -> f.get("key")).toList();
    }

    /**
     * Checks if a school with the given name exists.
     *
     * @param schoolName the school name to check
     * @return true if a school with the given name exists, otherwise false
     */
    public boolean schoolNameExists(String schoolName) {
        return schoolRepository.findBySchoolName(schoolName).isPresent();
    }

    @Override
    public boolean existsById(Long id) {
        return schoolRepository.existsById(id);
    }

    private School loadSchool(Long id) {
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

    private static <T> void setIfPresent(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    private void applyFieldUpdates(School existing, School data) {
        String incomingName = data.getSchoolName();
        if (incomingName != null && !incomingName.equals(existing.getSchoolName())) {
            ensureUniqueName(incomingName, existing.getId());
            existing.setSchoolName(incomingName);
        }

        setIfPresent(data.getZoneNumber(), existing::setZoneNumber);
        setIfPresent(data.getSchoolType(), existing::setSchoolType);
        setIfPresent(data.getIsActive(), existing::setIsActive);
        setIfPresent(data.getAddress(), existing::setAddress);
        setIfPresent(data.getLatitude(), existing::setLatitude);
        setIfPresent(data.getLongitude(), existing::setLongitude);
        setIfPresent(data.getDistanceFromCenter(), existing::setDistanceFromCenter);
        setIfPresent(data.getTransportAccessibility(), existing::setTransportAccessibility);
        setIfPresent(data.getContactEmail(), existing::setContactEmail);
        setIfPresent(data.getContactPhone(), existing::setContactPhone);
    }

    private School persist(School school) {
        return schoolRepository.save(school);
    }

    private Pageable pageRequestFrom(Map<String, String> queryParams) {
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        return PageRequest.of(params.page() - 1, params.pageSize(), sort);
    }

    private Specification<School> buildFilterSpecification(Map<String, String> queryParams, String searchValue) {
        return (root, query, cb) -> cb.and(collectPredicates(queryParams, searchValue, root, cb)
                .toArray(new Predicate[0]));
    }

    private List<Predicate> collectPredicates(Map<String, String> queryParams, String searchValue,
                                              Root<School> root, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (searchValue != null && !searchValue.trim().isEmpty()) {
            String likePattern = "%" + searchValue.trim().toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(root.get("schoolName")), likePattern));
        }

        parseSchoolType(queryParams.get("schoolType"))
                .ifPresent(t -> predicates.add(cb.equal(root.get("schoolType"), t)));

        parseInteger(queryParams.get("zoneNumber"))
                .ifPresent(z -> predicates.add(cb.equal(root.get("zoneNumber"), z)));

        String isActiveParam = queryParams.get("isActive");
        if (isActiveParam != null && !isActiveParam.trim().isEmpty()) {
            predicates.add(cb.equal(root.get("isActive"), Boolean.parseBoolean(isActiveParam.trim())));
        }

        return predicates;
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

    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.SCHOOL,
            description = "Viewed list of schools",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Map<String, Object> getPaginated(Map<String, String> queryParams, String searchValue) {
        Pageable pageable = pageRequestFrom(queryParams);
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
        return getAllSchools();
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
        return getSchoolById(id);
    }

    private List<School> getAllSchools() {
        return schoolRepository.findAll();
    }

    private Optional<School> getSchoolById(Long id) {
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
        return persist(school);
    }

    @Audited(
            action = AuditAction.UPDATE,
            entityName = AuditEntityNames.SCHOOL,
            description = "Updated school",
            captureNewValue = true
    )
    @Override
    public School update(Long id, School data) {
        School existing = loadSchool(id);
        applyFieldUpdates(existing, data);
        return persist(existing);
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
        School existing = loadSchool(id);
        existing.setIsActive(isActive);
        return persist(existing);
    }

    @Audited(
            action = AuditAction.DELETE,
            entityName = AuditEntityNames.SCHOOL,
            description = "Deleted school",
            captureNewValue = false
    )
    @Override
    public void delete(Long id) {
        // Same behavior as before (404 if missing), but different structure to avoid clone block.
        if (!schoolRepository.existsById(id)) {
            throw new ResourceNotFoundException("School not found with id: " + id);
        }
        schoolRepository.deleteById(id);
    }
}
