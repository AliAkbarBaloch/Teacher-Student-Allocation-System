package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.entity.SubjectCategory;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.SubjectCategoryRepository;
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

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
/**
 * Service for managing subject categories.
 * Handles CRUD operations for subject category entities.
 */
public class SubjectCategoryService implements CrudService<SubjectCategory, Long> {

    private final SubjectCategoryRepository subjectCategoryRepository;

    @Override
    public List<Map<String, String>> getSortFields() {
        return SortFieldUtils.getSortFields("id", "categoryTitle", "createdAt", "updatedAt");
    }

    /**
     * Returns the list of sortable field keys.
     *
     * @return list of field keys
     */
    public List<String> getSortFieldKeys() {
        return getSortFields().stream().map(f -> f.get("key")).toList();
    }

    private Specification<SubjectCategory> buildSearchSpecification(String searchValue) {
        // Search across categoryTitle (extend fields if needed)
        return SearchSpecificationUtils.buildMultiFieldLikeSpecification(
                new String[]{"categoryTitle"}, searchValue
        );
    }

    /**
     * Checks if a category with the given title exists.
     *
     * @param categoryTitle the category title to check
     * @return true if category title exists, false otherwise
     */
    public boolean categoryTitleExists(String categoryTitle) {
        return subjectCategoryRepository.findByCategoryTitle(categoryTitle).isPresent();
    }

    /**
     * Ensures title uniqueness.
     * - For create: currentId == null, any match is a duplicate.
     * - For update: allows the same record to keep its title.
     */
    private void ensureTitleUnique(String title, Long currentId) {
        if (title == null) {
            return;
        }

        Optional<SubjectCategory> match = subjectCategoryRepository.findByCategoryTitle(title);
        if (match.isEmpty()) {
            return;
        }

        SubjectCategory found = match.get();
        boolean sameRecord = currentId != null && found.getId() != null && found.getId().equals(currentId);
        if (!sameRecord) {
            throw new DuplicateResourceException("Subject category with title '" + title + "' already exists");
        }
    }

    /**
     * Validates that a category exists with the given ID.
     *
     * @param id the category ID
     * @throws ResourceNotFoundException if not found
     */
    private void validateExistence(Long id) {
        if (!subjectCategoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Subject category not found with id: " + id);
        }
    }

    private SubjectCategory loadForUpdate(Long id) {
        return subjectCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject category not found with id: " + id));
    }

    /**
     * Applies field updates from source to target category.
     * Only updates fields that are non-null in the source.
     *
     * @param existing the target category to update
     * @param data     the source data with new values
     */
    private void applyFieldUpdates(SubjectCategory existing, SubjectCategory data) {
        String incomingTitle = data.getCategoryTitle();
        if (incomingTitle == null) {
            return;
        }

        // Same logic as before: only enforce uniqueness when it would change the title
        if (!incomingTitle.equals(existing.getCategoryTitle())) {
            ensureTitleUnique(incomingTitle, existing.getId());
        }

        existing.setCategoryTitle(incomingTitle);
    }

    @Override
    public boolean existsById(Long id) {
        // keep as in your original version: repository existence
        return subjectCategoryRepository.findById(id).isPresent();
    }

    private Pageable createPageRequest(Map<String, String> queryParams) {
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        return PageRequest.of(params.page() - 1, params.pageSize(), sort);
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.SUBJECT_CATEGORY,
            description = "Viewed list of subject categories",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Map<String, Object> getPaginated(Map<String, String> queryParams, String searchValue) {
        Pageable pageable = createPageRequest(queryParams);
        Specification<SubjectCategory> spec = buildSearchSpecification(searchValue);
        Page<SubjectCategory> page = subjectCategoryRepository.findAll(spec, pageable);
        return PaginationUtils.formatPaginationResponse(page);
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.SUBJECT_CATEGORY,
            description = "Viewed all subject categories",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public List<SubjectCategory> getAll() {
        return subjectCategoryRepository.findAll();
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.SUBJECT_CATEGORY,
            description = "Viewed subject category by id",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Optional<SubjectCategory> getById(Long id) {
        return subjectCategoryRepository.findById(id);
    }

    @Audited(
            action = AuditAction.CREATE,
            entityName = AuditEntityNames.SUBJECT_CATEGORY,
            description = "Created new subject category",
            captureNewValue = true
    )
    @Override
    public SubjectCategory create(SubjectCategory subjectCategory) {
        // Same behavior as validateCategoryTitleUniqueness(title)
        ensureTitleUnique(subjectCategory.getCategoryTitle(), null);
        return subjectCategoryRepository.save(subjectCategory);
    }

    @Audited(
            action = AuditAction.UPDATE,
            entityName = AuditEntityNames.SUBJECT_CATEGORY,
            description = "Updated subject category",
            captureNewValue = true
    )
    @Override
    public SubjectCategory update(Long id, SubjectCategory data) {
        SubjectCategory existing = loadForUpdate(id);
        applyFieldUpdates(existing, data);
        return subjectCategoryRepository.save(existing);
    }

    @Audited(
            action = AuditAction.DELETE,
            entityName = AuditEntityNames.SUBJECT_CATEGORY,
            description = "Deleted subject category",
            captureNewValue = false
    )
    @Override
    public void delete(Long id) {
        // Preserve original behavior: check existence via existsById, then delete.
        validateExistence(id);
        subjectCategoryRepository.deleteById(id);
    }
}
