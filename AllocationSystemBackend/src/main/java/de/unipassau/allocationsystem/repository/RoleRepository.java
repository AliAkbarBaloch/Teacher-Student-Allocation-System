package de.unipassau.allocationsystem.repository;

import de.unipassau.allocationsystem.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {
    Optional<Role> findByTitle(String title);

    List<Role> findAll();

    Optional<Role> findById(Long id);
}
