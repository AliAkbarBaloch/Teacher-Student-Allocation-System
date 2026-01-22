package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.entity.Permission;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.PermissionRepository;
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

    private Specification<Permission> buildSearchSpecification(String searchValue) {
        // Search across title and description
        return SearchSpecificationUtils.buildMultiFieldLikeSpecification(
                new String[]{"title", "description"}, searchValue
        );
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

    /**
     * Ensures that the given title is unique.
     * If currentId is non-null, allows the record with that id to have the same title.
     */
    private void assertTitleUniqueForId(String title, Long currentId) {
        permissionRepository.findByTitle(title).ifPresent(existing -> {
            Long existingId = existing.getId();
            boolean isDifferentRecord = (currentId == null) || (existingId != null && !existingId.equals(currentId));
            if (isDifferentRecord) {
                throw new DuplicateResourceException("Permission with title '" + title + "' already exists");
            }
        });
    }

    /**
     * Applies field updates from source to target permission.
     * Only updates fields that are non-null in the source.
     *
     * @param existing the target permission to update
     * @param data     the source data with new values
     */
    private void applyFieldUpdates(Permission existing, Permission data) {
        String newTitle = data.getTitle();
        if (newTitle != null && !newTitle.equals(existing.getTitle())) {
            assertTitleUniqueForId(newTitle, existing.getId());
            existing.setTitle(newTitle);
        }

        if (data.getDescription() != null) {
            existing.setDescription(data.getDescription());
        }
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
        Specification<Permission> spec = buildSearchSpecification(searchValue);
        return permissionRepository.findAll(spec, pageable);
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
        Pageable pageable = toPageable(queryParams);
        Page<Permission> page = findPage(searchValue, pageable);
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
        assertTitleUniqueForId(permission.getTitle(), null);
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
        // Ensures correct exception if missing, then deletes
        getExistingOrThrow(id);
        permissionRepository.deleteById(id);
    }
}
