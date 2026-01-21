package de.unipassau.allocationsystem.repository;

import de.unipassau.allocationsystem.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for RolePermission entity operations.
 * Manages role-permission associations in RBAC system.
 */
@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long>, JpaSpecificationExecutor<RolePermission> {
    /**
     * Find role-permission association by role and permission IDs.
     * 
     * @param roleId the role ID
     * @param permissionId the permission ID
     * @return optional containing the association if found
     */
    Optional<RolePermission> findByRoleIdAndPermissionId(Long roleId, Long permissionId);
    
    /**
     * Find all permissions associated with a role.
     * 
     * @param roleId the role ID
     * @return list of role-permission associations
     */
    List<RolePermission> findByRoleId(Long roleId);
    
    /**
     * Find all roles associated with a permission.
     * 
     * @param permissionId the permission ID
     * @return list of role-permission associations
     */
    List<RolePermission> findByPermissionId(Long permissionId);
}
