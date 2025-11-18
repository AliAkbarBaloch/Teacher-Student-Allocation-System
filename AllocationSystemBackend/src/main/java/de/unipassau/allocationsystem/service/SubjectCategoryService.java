package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.entity.SubjectCategory;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.SubjectCategoryRepository;
import de.unipassau.allocationsystem.service.audit.AuditLogService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service layer for CRUD operations and pagination logic around {@link SubjectCategory}.
 * <p>
 * Besides basic repository calls, this class integrates audit logging so that
 * create/update/delete operations are tracked in the central audit log, mirroring
 * the behaviour implemented for users. All mutations emit descriptive log entries
 * and capture before/after snapshots where applicable.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubjectCategoryService implements CrudService<SubjectCategory, Long> {

    private final SubjectCategoryRepository subjectCategoryRepository;
    private final AuditLogService auditLogService;

    public List<Map<String, String>> getSortFields() {
        List<Map<String, String>> fields = new ArrayList<>();
        fields.add(Map.of("key", "id", "label", "ID"));
        fields.add(Map.of("key", "categoryTitle", "label", "Category Title"));
        fields.add(Map.of("key", "createdAt", "label", "Creation Date"));
        fields.add(Map.of("key", "updatedAt", "label", "Last Updated"));
        return fields;
    }

    /**
     * Checks whether a subject category with the provided title already exists.
     *
     * @param categoryTitle title to check
     * @return true when the repository already has an entity with this title
     */
    public boolean categoryTitleExists(String categoryTitle) {
        return subjectCategoryRepository.findByCategoryTitle(categoryTitle).isPresent();
    }

    @Override
    public boolean existsById(Long id) {
        return subjectCategoryRepository.existsById(id);
    }

    /**
     * Builds a case-insensitive specification that matches titles containing
     * the provided search fragment. If the search string is blank the specification
     * resolves to a conjunction (match all).
     *
     * @param searchValue optional string to filter by
     * @return specification applied to the repository query
     */
    private Specification<SubjectCategory> buildSearchSpecification(String searchValue) {
        if (searchValue == null || searchValue.trim().isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }
        String likePattern = "%" + searchValue.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("categoryTitle")), likePattern);
    }

    /**
     * Returns a paginated result set of subject categories applying sort and
     * optional search directives derived from query parameters.
     *
     * @param queryParams raw request query map containing page/pageSize/sort info
     * @param searchValue optional search string
     * @return pagination metadata along with the current page content
     */
    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.SUBJECT_CATEGORY,
            description = "Viewed list of subject categories",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Map<String, Object> getPaginated(Map<String, String> queryParams, String searchValue) {
        log.info("Fetching subject categories with params: {}", queryParams);
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
        log.info("Retrieving all subject categories");
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
        log.info("Retrieving subject category by id {}", id);
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
        log.info("Creating subject category with title {}", subjectCategory.getCategoryTitle());
        if (subjectCategoryRepository.findByCategoryTitle(subjectCategory.getCategoryTitle()).isPresent()) {
            throw new DuplicateResourceException("Subject category with title '" + subjectCategory.getCategoryTitle() + "' already exists");
        }
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
        log.info("Updating subject category {}", id);
        SubjectCategory existing = subjectCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject category not found with id: " + id));

        Map<String, Object> previousValue = new HashMap<>();
        Map<String, Object> newValue = new HashMap<>();

        if (data.getCategoryTitle() != null && !data.getCategoryTitle().equals(existing.getCategoryTitle())) {
            if (subjectCategoryRepository.findByCategoryTitle(data.getCategoryTitle()).isPresent()) {
                throw new DuplicateResourceException("Subject category with title '" + data.getCategoryTitle() + "' already exists");
            }
            previousValue.put("categoryTitle", existing.getCategoryTitle());
            existing.setCategoryTitle(data.getCategoryTitle());
            newValue.put("categoryTitle", data.getCategoryTitle());
        }

        SubjectCategory updated = subjectCategoryRepository.save(existing);

        if (!previousValue.isEmpty()) {
            auditLogService.logUpdate(AuditEntityNames.SUBJECT_CATEGORY, id.toString(), previousValue, newValue);
        }

        return updated;
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
        log.info("Deleting subject category {}", id);
        SubjectCategory existing = subjectCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject category not found with id: " + id));

        Map<String, Object> previousValue = Map.of(
            "id", existing.getId(),
            "categoryTitle", existing.getCategoryTitle()
        );

        subjectCategoryRepository.delete(existing);
        auditLogService.logDelete(AuditEntityNames.SUBJECT_CATEGORY, id.toString(), previousValue);
    }
}

