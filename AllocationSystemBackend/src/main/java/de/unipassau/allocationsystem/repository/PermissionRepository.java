package de.unipassau.allocationsystem.repository;

import de.unipassau.allocationsystem.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {
    Optional<Permission> findByTitle(String title);

    List<Permission> findAll();

    Optional<Permission> findById(Long id);
}
