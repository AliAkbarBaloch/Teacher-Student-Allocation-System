package de.unipassau.allocationsystem.repository;

import de.unipassau.allocationsystem.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Role entity operations.
 * Manages RBAC roles.
 */
public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {
    /**
     * Find role by title.
     * 
     * @param title the role title
     * @return optional containing the role if found
     */
    Optional<Role> findByTitle(String title);

    /**
     * Find all roles.
     * 
     * @return list of all roles
     */
    List<Role> findAll();

    /**
     * Find role by ID.
     * 
     * @param id the role ID
     * @return optional containing the role if found
     */
    Optional<Role> findById(Long id);
}
