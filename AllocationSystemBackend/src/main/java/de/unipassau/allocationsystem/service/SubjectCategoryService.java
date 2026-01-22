package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.entity.SubjectCategory;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.SubjectCategoryRepository;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import de.unipassau.allocationsystem.utils.SearchSpecificationUtils;
import de.unipassau.allocationsystem.utils.SortFieldUtils;

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
     * Validates that the category title is unique, throws exception if duplicate found.
     * 
     * @param categoryTitle the category title to validate
     * @throws DuplicateResourceException if title already exists
     */
    private void validateCategoryTitleUniqueness(String categoryTitle) {
        if (subjectCategoryRepository.findByCategoryTitle(categoryTitle).isPresent()) {
            throw new DuplicateResourceException("Subject category with title '" + categoryTitle + "' already exists");
        }
    }

    /**
     * Validates that a new category title doesn't conflict with existing records (for updates).
     * Allows the same title if it's the current category being updated.
     * 
     * @param newTitle the new category title
     * @param oldTitle the old category title
     * @throws DuplicateResourceException if new title conflicts with another category's title
     */
    private void validateCategoryTitleForUpdate(String newTitle, String oldTitle) {
        if (!newTitle.equals(oldTitle) && subjectCategoryRepository.findByCategoryTitle(newTitle).isPresent()) {
            throw new DuplicateResourceException("Subject category with title '" + newTitle + "' already exists");
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

    /**
     * Applies field updates from source to target category.
     * Only updates fields that are non-null in the source.
     * 
     * @param existing the target category to update
     * @param data the source data with new values
     */
    private void applyFieldUpdates(SubjectCategory existing, SubjectCategory data) {
        if (data.getCategoryTitle() != null) {
            validateCategoryTitleForUpdate(data.getCategoryTitle(), existing.getCategoryTitle());
            existing.setCategoryTitle(data.getCategoryTitle());
        }
    }

    @Override
    public boolean existsById(Long id) {
        return subjectCategoryRepository.findById(id).isPresent();
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
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        Pageable pageable = PageRequest.of(params.page() - 1, params.pageSize(), sort);

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
    @Transactional
    @Override
    public SubjectCategory create(SubjectCategory subjectCategory) {
        validateCategoryTitleUniqueness(subjectCategory.getCategoryTitle());
        return subjectCategoryRepository.save(subjectCategory);
    }

    @Audited(
            action = AuditAction.UPDATE,
            entityName = AuditEntityNames.SUBJECT_CATEGORY,
            description = "Updated subject category",
            captureNewValue = true
    )
    @Transactional
    @Override
    public SubjectCategory update(Long id, SubjectCategory data) {
        SubjectCategory existing = subjectCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject category not found with id: " + id));

        applyFieldUpdates(existing, data);
        return subjectCategoryRepository.save(existing);
    }

    @Audited(
            action = AuditAction.DELETE,
            entityName = AuditEntityNames.SUBJECT_CATEGORY,
            description = "Deleted subject category",
            captureNewValue = false
    )
    @Transactional
    @Override
    public void delete(Long id) {
        validateExistence(id);
        subjectCategoryRepository.deleteById(id);
    }
}