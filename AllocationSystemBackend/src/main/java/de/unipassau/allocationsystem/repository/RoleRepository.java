package de.unipassau.allocationsystem.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import de.unipassau.allocationsystem.entity.Role;

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
}

