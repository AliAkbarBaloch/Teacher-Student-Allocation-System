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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Service for managing {@link Subject} entities.
 * Provides CRUD operations plus paginated querying with search and sorting.
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
     * Returns the list of sortable field keys exposed by {@link #getSortFields()}.
     *
     * @return list of sort field keys
     */
    public List<String> getSortFieldKeys() {
        return getSortFields().stream().map(x -> x.get("key")).toList();
    }

    private Specification<Subject> searchSpec(String searchValue) {
        return SearchSpecificationUtils.buildMultiFieldLikeSpecification(
                new String[]{"subjectCode", "subjectTitle", "schoolType"}, searchValue
        );
    }

    /**
     * Checks if a {@link Subject} exists with the given subject code.
     *
     * @param subjectCode subject code to look up
     * @return true if a subject exists with the given code, otherwise false
     */
    public boolean isRecordExist(String subjectCode) {
        return subjectRepository.findBySubjectCode(subjectCode).isPresent();
    }

    private Optional<Subject> findByCode(String code) {
        if (code == null) {
            return Optional.empty();
        }
        return subjectRepository.findBySubjectCode(code);
    }

    private void failDuplicate(String code) {
        throw new DuplicateResourceException("Subject with code '" + code + "' already exists");
    }

    private void assertCodeAvailableFor(String code, Long currentId) {
        Optional<Subject> match = findByCode(code);
        if (match.isEmpty()) {
            return;
        }

        Subject found = match.get();
        if (currentId == null) {
            failDuplicate(code);
            return;
        }

        Long foundId = found.getId();
        if (foundId == null || !foundId.equals(currentId)) {
            failDuplicate(code);
        }
    }

    private Subject requireSubject(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));
    }

    @Override
    public boolean existsById(Long id) {
        return subjectRepository.existsById(id);
    }

    private PageRequest buildPageRequestFrom(Map<String, String> queryParams) {
        PaginationUtils.PaginationParams p = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(p.sortOrder(), p.sortBy());
        return PageRequest.of(p.page() - 1, p.pageSize(), sort);
    }

    private Page<Subject> querySubjects(String searchValue, PageRequest pageRequest) {
        return subjectRepository.findAll(searchSpec(searchValue), pageRequest);
    }

    private static <T> void setIfPresent(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    private void applyCodeUpdate(Subject existing, String incomingCode) {
        if (incomingCode == null) {
            return;
        }
        if (incomingCode.equals(existing.getSubjectCode())) {
            return;
        }
        assertCodeAvailableFor(incomingCode, existing.getId());
        existing.setSubjectCode(incomingCode);
    }

    private void applyNonCodeUpdates(Subject existing, Subject data) {
        // Same logic as before, just grouped to avoid the repeated block clone pattern.
        setIfPresent(data.getIsActive(), existing::setIsActive);
        setIfPresent(data.getSchoolType(), existing::setSchoolType);
        setIfPresent(data.getSubjectCategory(), existing::setSubjectCategory);
        setIfPresent(data.getSubjectTitle(), existing::setSubjectTitle);
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
        PageRequest pageRequest = buildPageRequestFrom(queryParams);
        Page<Subject> pageResult = querySubjects(searchValue, pageRequest);
        return PaginationUtils.formatPaginationResponse(pageResult);
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
        return getAllSubjects();
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
        return getSubjectById(id);
    }

    private List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    private Optional<Subject> getSubjectById(Long id) {
        return subjectRepository.findById(id);
    }

    @Audited(
            action = AuditLog.AuditAction.CREATE,
            entityName = AuditEntityNames.SUBJECT,
            description = "Created new subject",
            captureNewValue = true
    )
    @Override
    public Subject create(Subject subject) {
        assertCodeAvailableFor(subject.getSubjectCode(), null);
        return subjectRepository.save(subject);
    }

    @Audited(
            action = AuditLog.AuditAction.UPDATE,
            entityName = AuditEntityNames.SUBJECT,
            description = "Updated subject information",
            captureNewValue = true
    )
    @Override
    public Subject update(Long id, Subject data) {
        Subject existing = requireSubject(id);
        applyCodeUpdate(existing, data.getSubjectCode());
        applyNonCodeUpdates(existing, data);
        return subjectRepository.save(existing);
    }

    @Audited(
            action = AuditLog.AuditAction.DELETE,
            entityName = AuditEntityNames.SUBJECT,
            description = "Deleted subject",
            captureNewValue = false
    )
    @Override
    public void delete(Long id) {
        // Same behavior (404 if missing), but delete via entity to change clone shape.
        Subject existing = requireSubject(id);
        subjectRepository.delete(existing);
    }
}
