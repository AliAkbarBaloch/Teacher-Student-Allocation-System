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
        if (permissionRepository.findByTitle(permission.getTitle()).isPresent()) {
            throw new DuplicateResourceException("Permission with title '" + permission.getTitle() + "' already exists");
        }
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

        if (data.getTitle() != null && !data.getTitle().equals(existing.getTitle())) {
            if (permissionRepository.findByTitle(data.getTitle()).isPresent()) {
                throw new DuplicateResourceException("Permission with title '" + data.getTitle() + "' already exists");
            }
            existing.setTitle(data.getTitle());
        }

        if (data.getDescription() != null) {
            existing.setDescription(data.getDescription());
        }

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
        if (!permissionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Permission not found with id: " + id);
        }
        permissionRepository.deleteById(id);
    }
    
}
