package de.unipassau.allocationsystem.repository;

import de.unipassau.allocationsystem.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Permission entity operations.
 * Manages RBAC permissions.
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {
    /**
     * Find permission by title.
     * 
     * @param title the permission title
     * @return optional containing the permission if found
     */
    Optional<Permission> findByTitle(String title);

    /**
     * Find all permissions.
     * 
     * @return list of all permissions
     */
    List<Permission> findAll();

    /**
     * Find permission by ID.
     * 
     * @param id the permission ID
     * @return optional containing the permission if found
     */
    Optional<Permission> findById(Long id);
}
