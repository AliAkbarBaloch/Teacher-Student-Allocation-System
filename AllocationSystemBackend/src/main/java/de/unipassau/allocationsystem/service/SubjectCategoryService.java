package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.entity.SubjectCategory;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.SubjectCategoryRepository;
import de.unipassau.allocationsystem.service.audit.AuditLogService;
import de.unipassau.allocationsystem.utils.PaginationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

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
public class SubjectCategoryService {

    private final SubjectCategoryRepository subjectCategoryRepository;
    private final AuditLogService auditLogService;

    /**
     * Checks whether a subject category with the provided title already exists.
     *
     * @param categoryTitle title to check
     * @return true when the repository already has an entity with this title
     */
    public boolean categoryTitleExists(String categoryTitle) {
        return subjectCategoryRepository.findByCategoryTitle(categoryTitle).isPresent();
    }

    /**
     * Returns the fields that callers may sort by along with human-readable labels.
     *
     * @return immutable list of key/label descriptors
     */
    public List<Map<String, String>> getSortFields() {
        List<Map<String, String>> fields = new ArrayList<>();
        fields.add(Map.of("key", "id", "label", "ID"));
        fields.add(Map.of("key", "categoryTitle", "label", "Category Title"));
        fields.add(Map.of("key", "createdAt", "label", "Creation Date"));
        fields.add(Map.of("key", "updatedAt", "label", "Last Updated"));
        return fields;
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
     * @param includeRelations unused toggle kept for API signature parity
     * @param searchValue optional search string
     * @return pagination metadata along with the current page content
     */
    @Transactional
    public Map<String, Object> getPaginated(Map<String, String> queryParams, boolean includeRelations, String searchValue) {
        log.info("Fetching subject categories with params: {}", queryParams);
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        Pageable pageable = PageRequest.of(params.page() - 1, params.pageSize(), sort);

        Specification<SubjectCategory> spec = buildSearchSpecification(searchValue);
        Page<SubjectCategory> page = subjectCategoryRepository.findAll(spec, pageable);

        return PaginationUtils.formatPaginationResponse(page);
    }

    /**
     * Retrieves every subject category without pagination. Intended for scenarios
     * like dropdown population where the dataset is relatively small.
     *
     * @return list of all subject categories
     */
    public List<SubjectCategory> getAll() {
        log.info("Retrieving all subject categories");
        return subjectCategoryRepository.findAll();
    }

    /**
     * Finds a subject category by identifier.
     *
     * @param id database id
     * @return optional containing the entity when present
     */
    public Optional<SubjectCategory> getById(Long id) {
        log.info("Retrieving subject category by id {}", id);
        return subjectCategoryRepository.findById(id);
    }

    /**
     * Persists a new subject category, enforcing unique titles and emitting an
     * audit event via the {@link Audited} annotation.
     *
     * @param subjectCategory payload to persist
     * @return stored entity including generated identifiers
     */
    @Transactional
    @Audited(
        action = AuditAction.CREATE,
        entityName = "SubjectCategory",
        description = "Created new subject category",
        captureNewValue = true
    )
    public SubjectCategory create(SubjectCategory subjectCategory) {
        log.info("Creating subject category with title {}", subjectCategory.getCategoryTitle());
        if (subjectCategoryRepository.findByCategoryTitle(subjectCategory.getCategoryTitle()).isPresent()) {
            throw new DuplicateResourceException("Subject category with title '" + subjectCategory.getCategoryTitle() + "' already exists");
        }
        return subjectCategoryRepository.save(subjectCategory);
    }

    /**
     * Applies mutable fields from the input entity to the stored category while
     * guarding against duplicate titles. Whenever a change occurs an audit log
     * entry is recorded with before/after snapshots.
     *
     * @param id   identifier of the category to update
     * @param data partial entity containing the new values
     * @return the updated entity
     */
    @Transactional
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
            auditLogService.logUpdate("SubjectCategory", id.toString(), previousValue, newValue);
        }

        return updated;
    }

    /**
     * Removes the subject category identified by {@code id} and records a deletion
     * audit log that captures the key fields of the removed entity.
     *
     * @param id identifier of the category to remove
     */
    @Transactional
    public void delete(Long id) {
        log.info("Deleting subject category {}", id);
        SubjectCategory existing = subjectCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject category not found with id: " + id));

        Map<String, Object> previousValue = Map.of(
            "id", existing.getId(),
            "categoryTitle", existing.getCategoryTitle()
        );

        subjectCategoryRepository.delete(existing);
        auditLogService.logDelete("SubjectCategory", id.toString(), previousValue);
    }
}

