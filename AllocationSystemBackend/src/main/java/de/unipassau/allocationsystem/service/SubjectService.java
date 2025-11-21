package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.entity.AuditLog;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.SubjectRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service layer for CRUD operations and pagination logic around {@link Subject}.
 * <p>
 * This service integrates audit logging so that create/update/delete operations
 * are tracked in the central audit log. All mutations emit descriptive log entries
 * and capture before/after snapshots where applicable.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubjectService implements CrudService<Subject, Long> {

    private final SubjectRepository subjectRepository;

    @Override
    public List<Map<String, String>> getSortFields() {
        List<Map<String, String>> fields = new ArrayList<>();
        fields.add(Map.of("key", "id", "label", "ID"));
        fields.add(Map.of("key", "subjectCode", "label", "Subject Code"));
        fields.add(Map.of("key", "subjectTitle", "label", "Subject Title"));
        fields.add(Map.of("key", "schoolType", "label", "School Type"));
        fields.add(Map.of("key", "isActive", "label", "Active Status"));
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

    /**
     * Builds a case-insensitive specification that matches subject code, title,
     * or school type containing the provided search fragment. If the search string
     * is blank the specification resolves to a conjunction (match all).
     *
     * @param searchValue optional string to filter by
     * @return specification applied to the repository query
     */
    private Specification<Subject> buildSearchSpecification(String searchValue) {
        if (searchValue == null || searchValue.trim().isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }
        String likePattern = "%" + searchValue.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("subjectCode")), likePattern),
                cb.like(cb.lower(root.get("subjectTitle")), likePattern),
                cb.like(cb.lower(root.get("schoolType")), likePattern)
        );
    }

    /**
     * Checks whether a subject with the provided code already exists.
     *
     * @param subjectCode code to check
     * @return true when the repository already has an entity with this code
     */
    public boolean isRecordExist(String subjectCode) {
        return subjectRepository.findBySubjectCode(subjectCode).isPresent();
    }

    @Override
    public boolean existsById(Long id) {
        return subjectRepository.existsById(id);
    }

    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.SUBJECT,
            description = "Viewed list of subjects",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Map<String, Object> getPaginated(Map<String, String> queryParams, String searchValue) {
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        Pageable pageable = PageRequest.of(params.page() - 1, params.pageSize(), sort);

        Specification<Subject> spec = buildSearchSpecification(searchValue);
        Page<Subject> page = subjectRepository.findAll(spec, pageable);

        return PaginationUtils.formatPaginationResponse(page);
    }

    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.SUBJECT,
            description = "Viewed all subjects",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public List<Subject> getAll() {
        return subjectRepository.findAll();
    }

    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.SUBJECT,
            description = "Viewed subject by id",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Optional<Subject> getById(Long id) {
        return subjectRepository.findById(id);
    }

    /**
     * Persists a new subject, enforcing unique codes and emitting an
     * audit event via the {@link Audited} annotation.
     *
     * @param subject payload to persist
     * @return stored entity including generated identifiers
     * @throws DuplicateResourceException if subject code already exists
     */
    @Audited(
            action = AuditLog.AuditAction.CREATE,
            entityName = AuditEntityNames.SUBJECT,
            description = "Created new subject",
            captureNewValue = true
    )
    @Transactional
    @Override
    public Subject create(Subject subject) {
        if (subjectRepository.findBySubjectCode(subject.getSubjectCode()).isPresent()) {
            throw new DuplicateResourceException("Subject with code '" + subject.getSubjectCode() + "' already exists");
        }
        return subjectRepository.save(subject);
    }

    /**
     * Applies mutable fields from the input entity to the stored subject while
     * guarding against duplicate codes. Whenever a change occurs an audit log
     * entry is recorded with before/after snapshots.
     *
     * @param id   identifier of the subject to update
     * @param data partial entity containing the new values
     * @return the updated entity
     * @throws ResourceNotFoundException if subject not found
     * @throws DuplicateResourceException if update violates uniqueness
     */
    @Audited(
            action = AuditLog.AuditAction.UPDATE,
            entityName = AuditEntityNames.SUBJECT,
            description = "Updated subject information",
            captureNewValue = true
    )
    @Transactional
    @Override
    public Subject update(Long id, Subject data) {
        Subject existing = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));

        if (data.getSubjectCode() != null && !data.getSubjectCode().equals(existing.getSubjectCode())) {
            if (subjectRepository.findBySubjectCode(data.getSubjectCode()).isPresent()) {
                throw new DuplicateResourceException("Subject with code '" + data.getSubjectCode() + "' already exists");
            }
            existing.setSubjectCode(data.getSubjectCode());
        }
        if (data.getSubjectTitle() != null) {
            existing.setSubjectTitle(data.getSubjectTitle());
        }
        if (data.getSubjectCategory() != null) {
            existing.setSubjectCategory(data.getSubjectCategory());
        }
        if (data.getSchoolType() != null) {
            existing.setSchoolType(data.getSchoolType());
        }
        if (data.getIsActive() != null) {
            existing.setIsActive(data.getIsActive());
        }

        return subjectRepository.save(existing);
    }

    /**
     * Removes the subject identified by {@code id} and records a deletion
     * audit log that captures the key fields of the removed entity.
     *
     * @param id identifier of the subject to remove
     * @throws ResourceNotFoundException if subject not found
     */
    @Audited(
            action = AuditLog.AuditAction.DELETE,
            entityName = AuditEntityNames.SUBJECT,
            description = "Deleted subject",
            captureNewValue = false
    )
    @Transactional
    @Override
    public void delete(Long id) {
        if (!subjectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Subject not found with id: " + id);
        }
        subjectRepository.deleteById(id);
    }
}

