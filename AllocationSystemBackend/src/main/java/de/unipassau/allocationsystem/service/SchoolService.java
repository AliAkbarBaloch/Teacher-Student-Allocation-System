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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SchoolService implements CrudService<School, Long> {

    private final SchoolRepository schoolRepository;

    @Override
    public List<Map<String, String>> getSortFields() {
        List<Map<String, String>> fields = new ArrayList<>();
        fields.add(Map.of("key", "id", "label", "ID"));
        fields.add(Map.of("key", "schoolName", "label", "School Name"));
        fields.add(Map.of("key", "createdAt", "label", "Creation Date"));
        fields.add(Map.of("key", "updatedAt", "label", "Last Updated"));
        return fields;
    }

    public List<String> getSortFieldKeys() {
        List<String> keys = new ArrayList<>();
        for (Map<String, String> field : getSortFields()) {
            keys.add(field.get("key"));
        }
        return keys;
    }

    private Specification<School> buildSearchSpecification(String searchValue) {
        if (searchValue == null || searchValue.trim().isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }
        String likePattern = "%" + searchValue.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("schoolName")), likePattern);
    }

    public boolean schoolNameExists(String schoolName) {
        return schoolRepository.findBySchoolName(schoolName).isPresent();
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

            // Search filter
            if (searchValue != null && !searchValue.trim().isEmpty()) {
                String likePattern = "%" + searchValue.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("schoolName")), likePattern));
            }

            // School type filter
            String schoolTypeParam = queryParams.get("schoolType");
            if (schoolTypeParam != null && !schoolTypeParam.trim().isEmpty()) {
                try {
                    School.SchoolType schoolType = School.SchoolType.valueOf(schoolTypeParam.toUpperCase());
                    predicates.add(cb.equal(root.get("schoolType"), schoolType));
                } catch (IllegalArgumentException e) {
                    // Invalid school type, ignore filter
                }
            }

            // Zone number filter
            String zoneNumberParam = queryParams.get("zoneNumber");
            if (zoneNumberParam != null && !zoneNumberParam.trim().isEmpty()) {
                try {
                    Integer zoneNumber = Integer.parseInt(zoneNumberParam);
                    predicates.add(cb.equal(root.get("zoneNumber"), zoneNumber));
                } catch (NumberFormatException e) {
                    // Invalid zone number, ignore filter
                }
            }

            // Active status filter
            String isActiveParam = queryParams.get("isActive");
            if (isActiveParam != null && !isActiveParam.trim().isEmpty()) {
                try {
                    Boolean isActive = Boolean.parseBoolean(isActiveParam);
                    predicates.add(cb.equal(root.get("isActive"), isActive));
                } catch (Exception e) {
                    // Invalid boolean, ignore filter
                }
            }

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
        if (schoolRepository.findBySchoolName(school.getSchoolName()).isPresent()) {
            throw new DuplicateResourceException("School with name '" + school.getSchoolName() + "' already exists");
        }
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

        if (data.getSchoolName() != null && !data.getSchoolName().equals(existing.getSchoolName())) {
            if (schoolRepository.findBySchoolName(data.getSchoolName()).isPresent()) {
                throw new DuplicateResourceException("School with name '" + data.getSchoolName() + "' already exists");
            }
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

        return schoolRepository.save(existing);
    }

    @Audited(
            action = AuditAction.UPDATE,
            entityName = AuditEntityNames.SCHOOL,
            description = "Updated school status",
            captureNewValue = true
    )
    @Transactional
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
        if (!schoolRepository.existsById(id)) {
            throw new ResourceNotFoundException("School not found with id: " + id);
        }
        schoolRepository.deleteById(id);
    }
}