package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.entity.AuditLog;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.SubjectRepository;
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
        return SortFieldUtils.getSortFields(
                "id", "subjectCode", "subjectTitle", "schoolType", "isActive", "createdAt", "updatedAt"
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
     * Builds a case-insensitive specification that matches subject code, title,
     * or school type containing the provided search fragment. If the search string
     * is blank the specification resolves to a conjunction (match all).
     *
     * @param searchValue optional string to filter by
     * @return specification applied to the repository query
     */
    private Specification<Subject> buildSearchSpecification(String searchValue) {
        // Search across subjectCode, subjectTitle, and schoolType
        return SearchSpecificationUtils.buildMultiFieldLikeSpecification(
                new String[]{"subjectCode", "subjectTitle", "schoolType"}, searchValue
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

    /**
     * Validates that the subject code is unique, throws exception if duplicate found.
     *
     * @param subjectCode the subject code to validate
     * @throws DuplicateResourceException if subject code already exists
     */
    private void validateSubjectCodeUniqueness(String subjectCode) {
        if (subjectRepository.findBySubjectCode(subjectCode).isPresent()) {
            throw new DuplicateResourceException("Subject with code '" + subjectCode + "' already exists");
        }
    }

    /**
     * Validates that a new subject code doesn't conflict with existing records (for updates).
     * Allows the same code if it's the current subject being updated.
     *
     * @param newCode the new subject code
     * @param oldCode the old subject code
     * @throws DuplicateResourceException if new code conflicts with another subject's code
     */
    private void validateSubjectCodeForUpdate(String newCode, String oldCode) {
        if (!newCode.equals(oldCode) && subjectRepository.findBySubjectCode(newCode).isPresent()) {
            throw new DuplicateResourceException("Subject with code '" + newCode + "' already exists");
        }
    }

    private Subject getExistingOrThrow(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));
    }

    /**
     * Applies field updates from source to target subject.
     * Only updates fields that are non-null in the source.
     *
     * @param existing the target subject to update
     * @param data     the source data with new values
     */
    private void applyFieldUpdates(Subject existing, Subject data) {
        String incomingCode = data.getSubjectCode();
        if (incomingCode != null) {
            validateSubjectCodeForUpdate(incomingCode, existing.getSubjectCode());
            existing.setSubjectCode(incomingCode);
        }

        String incomingTitle = data.getSubjectTitle();
        if (incomingTitle != null) {
            existing.setSubjectTitle(incomingTitle);
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
    }

    @Override
    public boolean existsById(Long id) {
        return subjectRepository.existsById(id);
    }

    private Pageable toPageable(Map<String, String> queryParams) {
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        return PageRequest.of(params.page() - 1, params.pageSize(), sort);
    }

    private Page<Subject> findPage(String searchValue, Pageable pageable) {
        Specification<Subject> spec = buildSearchSpecification(searchValue);
        return subjectRepository.findAll(spec, pageable);
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
        Page<Subject> page = findPage(searchValue, toPageable(queryParams));
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
    @Override
    public Subject create(Subject subject) {
        validateSubjectCodeUniqueness(subject.getSubjectCode());
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
     * @throws ResourceNotFoundException  if subject not found
     * @throws DuplicateResourceException if update violates uniqueness
     */
    @Audited(
            action = AuditLog.AuditAction.UPDATE,
            entityName = AuditEntityNames.SUBJECT,
            description = "Updated subject information",
            captureNewValue = true
    )
    @Override
    public Subject update(Long id, Subject data) {
        Subject existing = getExistingOrThrow(id);
        applyFieldUpdates(existing, data);
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
    @Override
    public void delete(Long id) {
        // Preserve behavior: throw 404 when missing, then delete.
        getExistingOrThrow(id);
        subjectRepository.deleteById(id);
    }
}
