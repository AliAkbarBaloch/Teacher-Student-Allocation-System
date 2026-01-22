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
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing {@link SubjectCategory} entities.
 * Provides CRUD operations and paginated search/sort for subject categories.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubjectCategoryService implements CrudService<SubjectCategory, Long> {

    private final SubjectCategoryRepository subjectCategoryRepository;

    @Override
    public List<Map<String, String>> getSortFields() {
        return SortFieldUtils.getSortFields("id", "categoryTitle", "createdAt", "updatedAt");
    }

    /**
     * Returns the list of sortable field keys exposed by {@link #getSortFields()}.
     *
     * @return list of sort field keys
     */
    public List<String> getSortFieldKeys() {
        return getSortFields().stream().map(m -> m.get("key")).toList();
    }

    /**
     * Checks if a subject category exists with the given title.
     *
     * @param categoryTitle category title to check
     * @return true if a category with the given title exists, otherwise false
     */
    public boolean categoryTitleExists(String categoryTitle) {
        return subjectCategoryRepository.findByCategoryTitle(categoryTitle).isPresent();
    }

    private void assertUniqueTitleForCreate(String title) {
        if (title != null && subjectCategoryRepository.findByCategoryTitle(title).isPresent()) {
            throw new DuplicateResourceException("Subject category with title '" + title + "' already exists");
        }
    }

    private void assertUniqueTitleForUpdate(String incomingTitle, SubjectCategory existing) {
        if (incomingTitle == null) {
            return;
        }
        if (incomingTitle.equals(existing.getCategoryTitle())) {
            return;
        }
        Optional<SubjectCategory> match = subjectCategoryRepository.findByCategoryTitle(incomingTitle);
        if (match.isPresent() && match.get().getId() != null && !match.get().getId().equals(existing.getId())) {
            throw new DuplicateResourceException("Subject category with title '" + incomingTitle + "' already exists");
        }
    }

    private SubjectCategory mustLoad(Long id) {
        return subjectCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject category not found with id: " + id));
    }

    private Specification<SubjectCategory> searchSpec(String searchValue) {
        return SearchSpecificationUtils.buildMultiFieldLikeSpecification(
                new String[]{"categoryTitle"}, searchValue
        );
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
        PaginationUtils.PaginationParams p = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(p.sortOrder(), p.sortBy());
        PageRequest pr = PageRequest.of(p.page() - 1, p.pageSize(), sort);

        Page<SubjectCategory> page = subjectCategoryRepository.findAll(searchSpec(searchValue), pr);
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
        assertUniqueTitleForCreate(subjectCategory.getCategoryTitle());
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
        SubjectCategory existing = mustLoad(id);

        String incomingTitle = data.getCategoryTitle();
        if (incomingTitle != null) {
            assertUniqueTitleForUpdate(incomingTitle, existing);
            existing.setCategoryTitle(incomingTitle);
        }

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
        // keep original semantics: 404 if missing, then delete
        if (!subjectCategoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Subject category not found with id: " + id);
        }
        subjectCategoryRepository.deleteById(id);
    }
}
