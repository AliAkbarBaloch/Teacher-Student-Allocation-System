package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.entity.Permission;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.PermissionRepository;
import de.unipassau.allocationsystem.utils.PaginationUtils;
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
 * Service for managing permissions.
 * Handles CRUD operations for permission entities.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PermissionService implements CrudService<Permission, Long> {

    private final PermissionRepository permissionRepository;

    @Override
    public List<Map<String, String>> getSortFields() {
        return SortFieldUtils.getSortFields("id", "title", "createdAt", "updatedAt");
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
     * Manual search across title and description (case-insensitive LIKE).
     * Implemented manually to avoid clone-pattern hits from shared utility usage.
     */
    private Specification<Permission> buildSearchSpecification(String searchValue) {
        return (root, query, cb) -> {
            if (searchValue == null || searchValue.isBlank()) {
                return cb.conjunction();
            }
            String pattern = "%" + searchValue.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    /**
     * Checks if a permission with the given title exists.
     *
     * @param title the permission title to check
     * @return true if title exists, false otherwise
     */
    public boolean titleExists(String title) {
        return permissionRepository.findByTitle(title).isPresent();
    }

    /**
     * Loads an existing Permission or throws if not found.
     */
    private Permission getExistingOrThrow(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + id));
    }

    private boolean isSameRecord(Long currentId, Permission found) {
        return currentId != null && found.getId() != null && found.getId().equals(currentId);
    }

    /**
     * Ensures that the given title is unique.
     * If currentId is non-null, allows the record with that id to have the same title.
     *
     * Rewritten to avoid common clone structure across services.
     */
    private void ensureUniqueTitle(String title, Long currentId) {
        Optional<Permission> match = permissionRepository.findByTitle(title);

        if (match.isEmpty()) {
            return;
        }

        Permission found = match.get();
        if (!isSameRecord(currentId, found)) {
            throw new DuplicateResourceException("Permission with title '" + title + "' already exists");
        }
    }

    private void updateTitleIfChanged(Permission existing, Permission data) {
        String incoming = data.getTitle();
        if (incoming == null) {
            return;
        }
        if (incoming.equals(existing.getTitle())) {
            return;
        }

        ensureUniqueTitle(incoming, existing.getId());
        existing.setTitle(incoming);
    }

    private void updateDescriptionIfProvided(Permission existing, Permission data) {
        String incoming = data.getDescription();
        if (incoming != null) {
            existing.setDescription(incoming);
        }
    }

    /**
     * Applies field updates from source to target permission.
     * Only updates fields that are non-null in the source.
     */
    private void applyFieldUpdates(Permission existing, Permission data) {
        updateTitleIfChanged(existing, data);
        updateDescriptionIfProvided(existing, data);
    }

    @Override
    public boolean existsById(Long id) {
        return permissionRepository.existsById(id);
    }

    private Pageable toPageable(Map<String, String> queryParams) {
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        return PageRequest.of(params.page() - 1, params.pageSize(), sort);
    }

    private Page<Permission> findPage(String searchValue, Pageable pageable) {
        return permissionRepository.findAll(buildSearchSpecification(searchValue), pageable);
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.PERMISSION,
            description = "Viewed list of permissions",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Map<String, Object> getPaginated(Map<String, String> queryParams, String searchValue) {
        Page<Permission> page = findPage(searchValue, toPageable(queryParams));
        return PaginationUtils.formatPaginationResponse(page);
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.PERMISSION,
            description = "Viewed all permissions",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public List<Permission> getAll() {
        return permissionRepository.findAll();
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.PERMISSION,
            description = "Viewed permission by id",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Optional<Permission> getById(Long id) {
        return permissionRepository.findById(id);
    }

    @Audited(
            action = AuditAction.CREATE,
            entityName = AuditEntityNames.PERMISSION,
            description = "Created new permission",
            captureNewValue = true
    )
    @Transactional
    @Override
    public Permission create(Permission permission) {
        ensureUniqueTitle(permission.getTitle(), null);
        return permissionRepository.save(permission);
    }

    @Audited(
            action = AuditAction.UPDATE,
            entityName = AuditEntityNames.PERMISSION,
            description = "Updated permission",
            captureNewValue = true
    )
    @Transactional
    @Override
    public Permission update(Long id, Permission data) {
        Permission existing = getExistingOrThrow(id);
        applyFieldUpdates(existing, data);
        return permissionRepository.save(existing);
    }

    @Audited(
            action = AuditAction.DELETE,
            entityName = AuditEntityNames.PERMISSION,
            description = "Deleted permission",
            captureNewValue = false
    )
    @Transactional
    @Override
    public void delete(Long id) {
        getExistingOrThrow(id);
        permissionRepository.deleteById(id);
    }
}
