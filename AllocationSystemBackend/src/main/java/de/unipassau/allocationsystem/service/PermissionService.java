package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.entity.Permission;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.PermissionRepository;
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
 * Service for managing permissions.
 * Handles CRUD operations for permission entities.
 */
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
     * Validates that the permission title is unique, throws exception if duplicate found.
     * 
     * @param title the permission title to validate
     * @throws DuplicateResourceException if title already exists
     */
    private void validatePermissionTitleUniqueness(String title) {
        if (permissionRepository.findByTitle(title).isPresent()) {
            throw new DuplicateResourceException("Permission with title '" + title + "' already exists");
        }
    }

    /**
     * Validates that a new permission title doesn't conflict with existing records (for updates).
     * Allows the same title if it's the current permission being updated.
     * 
     * @param newTitle the new permission title
     * @param oldTitle the old permission title
     * @throws DuplicateResourceException if new title conflicts with another permission's title
     */
    private void validatePermissionTitleForUpdate(String newTitle, String oldTitle) {
        if (!newTitle.equals(oldTitle) && permissionRepository.findByTitle(newTitle).isPresent()) {
            throw new DuplicateResourceException("Permission with title '" + newTitle + "' already exists");
        }
    }

    /**
     * Validates that a permission exists with the given ID.
     * 
     * @param id the permission ID
     * @throws ResourceNotFoundException if not found
     */
    private void validateExistence(Long id) {
        if (!permissionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Permission not found with id: " + id);
        }
    }

    /**
     * Applies field updates from source to target permission.
     * Only updates fields that are non-null in the source.
     * 
     * @param existing the target permission to update
     * @param data the source data with new values
     */
    private void applyFieldUpdates(Permission existing, Permission data) {
        if (data.getTitle() != null) {
            validatePermissionTitleForUpdate(data.getTitle(), existing.getTitle());
            existing.setTitle(data.getTitle());
        }
        if (data.getDescription() != null) {
            existing.setDescription(data.getDescription());
        }
    }

    @Override
    public boolean existsById(Long id) {
        return permissionRepository.findById(id).isPresent();
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
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        Pageable pageable = PageRequest.of(params.page() - 1, params.pageSize(), sort);

        Specification<Permission> spec = buildSearchSpecification(searchValue);
        Page<Permission> page = permissionRepository.findAll(spec, pageable);

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
        validatePermissionTitleUniqueness(permission.getTitle());
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
        Permission existing = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + id));

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
        validateExistence(id);
        permissionRepository.deleteById(id);
    }
}
