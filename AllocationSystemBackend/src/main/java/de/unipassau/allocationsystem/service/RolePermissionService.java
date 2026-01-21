package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.entity.RolePermission;
import de.unipassau.allocationsystem.repository.RolePermissionRepository;
import de.unipassau.allocationsystem.utils.PaginationUtils;
import de.unipassau.allocationsystem.utils.SortFieldUtils;
import de.unipassau.allocationsystem.utils.SearchSpecificationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
/**
 * Service for managing role-permission associations.
 * Handles CRUD operations for role-permission mappings.
 */
public class RolePermissionService implements CrudService<RolePermission, Long> {

    private final RolePermissionRepository rolePermissionRepository;

    /**
     * Returns the sortable fields metadata.
     * 
     * @return list of sort field metadata
     */
    public List<Map<String, String>> getSortFields() {
        return SortFieldUtils.getSortFields(
            "id", "role.title", "permission.title", "accessLevel", "createdAt", "updatedAt"
        );
    }

    /**
     * Returns the list of sortable field keys.
     * 
     * @return list of field keys
     */
    public List<String> getSortFieldKeys() {
        return getSortFields().stream().map(f -> f.get("key")).toList();
    }

    private Specification<RolePermission> buildSearchSpecification(String searchValue) {
        // Search across role.title, permission.title, accessLevel
        return SearchSpecificationUtils.buildMultiFieldLikeSpecification(
            new String[]{"role.title", "permission.title", "accessLevel"}, searchValue
        );
    }

    @Override
    public boolean existsById(Long id) {
        return rolePermissionRepository.existsById(id);
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.ROLE_PERMISSION,
            description = "Viewed list of role permissions",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Map<String, Object> getPaginated(Map<String, String> queryParams, String searchValue) {
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        Pageable pageable = PageRequest.of(params.page() - 1, params.pageSize(), sort);

        Specification<RolePermission> spec = buildSearchSpecification(searchValue);
        Page<RolePermission> page = rolePermissionRepository.findAll(spec, pageable);

        return PaginationUtils.formatPaginationResponse(page);
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.ROLE_PERMISSION,
            description = "Viewed all role permissions",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    /**
     * Retrieves all role-permission associations.
     * 
     * @return list of all role permissions
     */
    public List<RolePermission> getAll() {
        return rolePermissionRepository.findAll();
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.ROLE_PERMISSION,
            description = "Viewed role permission by id",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Optional<RolePermission> getById(Long id) {
        return rolePermissionRepository.findById(id);
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.ROLE_PERMISSION,
            description = "Viewed role permission by roleId",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    /**
     * Retrieves all permissions associated with a specific role.
     * 
     * @param roleId the role ID
     * @return list of role permissions for the role
     */
    public List<RolePermission> getByRoleId(Long roleId) {
        return rolePermissionRepository.findByRoleId(roleId);
    }

    @Audited(
        action = AuditAction.VIEW,
        entityName = AuditEntityNames.ROLE_PERMISSION,
        description = "Viewed role permissions by permissionId",
        captureNewValue = false
    )
    @Transactional(readOnly = true)
    /**
     * Retrieves all roles associated with a specific permission.
     * 
     * @param permissionId the permission ID
     * @return list of role permissions for the permission
     */
    public List<RolePermission> getByPermissionId(Long permissionId) {
        return rolePermissionRepository.findByPermissionId(permissionId);
    }

    @Audited(
            action = AuditAction.CREATE,
            entityName = AuditEntityNames.ROLE_PERMISSION,
            description = "Created new role permission",
            captureNewValue = true
    )
    @Transactional
    @Override
    public RolePermission create(RolePermission rolePermission) {
        if (rolePermissionRepository.findByRoleIdAndPermissionId(
                rolePermission.getRole().getId(),
                rolePermission.getPermission().getId()).isPresent()) {
            throw new DuplicateResourceException("Role permission mapping already exists");
        }
        return rolePermissionRepository.save(rolePermission);
    }

    @Audited(
            action = AuditAction.UPDATE,
            entityName = AuditEntityNames.ROLE_PERMISSION,
            description = "Updated role permission",
            captureNewValue = true
    )
    @Transactional
    @Override
    public RolePermission update(Long id, RolePermission data) {
        RolePermission existing = rolePermissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role permission not found with id: " + id));

        if (data.getRole() != null && data.getPermission() != null) {
            if (!existing.getRole().getId().equals(data.getRole().getId()) ||
                    !existing.getPermission().getId().equals(data.getPermission().getId())) {

                Optional<RolePermission> duplicate = rolePermissionRepository.findByRoleIdAndPermissionId(
                        data.getRole().getId(),
                        data.getPermission().getId());

                if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
                    throw new DuplicateResourceException("Role permission mapping already exists");
                }
                existing.setRole(data.getRole());
                existing.setPermission(data.getPermission());
            }
        }

        if (data.getAccessLevel() != null) {
            existing.setAccessLevel(data.getAccessLevel());
        }

        return rolePermissionRepository.save(existing);
    }

    @Audited(
            action = AuditAction.DELETE,
            entityName = AuditEntityNames.ROLE_PERMISSION,
            description = "Deleted role permission",
            captureNewValue = false
    )
    @Transactional
    @Override
    public void delete(Long id) {
        if (!rolePermissionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Role permission not found with id: " + id);
        }
        rolePermissionRepository.deleteById(id);
    }
}
