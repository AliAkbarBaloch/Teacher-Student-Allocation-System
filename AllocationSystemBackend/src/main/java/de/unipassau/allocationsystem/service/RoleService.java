package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.entity.Role;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.RoleRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class RoleService  implements CrudService<Role, Long>{

    private final RoleRepository roleRepository;

    public List<Map<String, String>> getSortFields() {
        List<Map<String, String>> fields = new ArrayList<>();
        fields.add(Map.of("key", "id", "label", "ID"));
        fields.add(Map.of("key", "title", "label", "Title"));
        fields.add(Map.of("key", "createdAt", "label", "Creation Date"));
        fields.add(Map.of("key", "updatedAt", "label", "Last Updated"));
        return fields;
    }

    public List<String> getSortFieldKeys() {
        List<String> keys = new ArrayList<>();
        for (Map<String, String> field : getSortFields()) {
            keys.add(field.get("key"));
        }
        return keys;
    }

    private Specification<Role> buildSearchSpecification(String searchValue) {
        if (searchValue == null || searchValue.trim().isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }

        String likePattern = "%" + searchValue.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), likePattern),
                cb.like(cb.lower(root.get("description")), likePattern)
        );
    }

    public boolean titleExists(String title) {
        return roleRepository.findByTitle(title).isPresent();
    }

    @Override
    public boolean existsById(Long id) {
        return roleRepository.existsById(id);
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.ROLE,
            description = "Viewed list of roles",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Map<String, Object> getPaginated(Map<String, String> queryParams, String searchValue) {
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        Pageable pageable = PageRequest.of(params.page() - 1, params.pageSize(), sort);

        Specification<Role> spec = buildSearchSpecification(searchValue);
        Page<Role> page = roleRepository.findAll(spec, pageable);

        return PaginationUtils.formatPaginationResponse(page);
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.ROLE,
            description = "Viewed all roles",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public List<Role> getAll() {
        return roleRepository.findAll();
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.ROLE,
            description = "Viewed role by id",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Optional<Role> getById(Long id) {
        return roleRepository.findById(id);
    }

    @Audited(
            action = AuditAction.CREATE,
            entityName = AuditEntityNames.ROLE,
            description = "Created new role",
            captureNewValue = true
    )
    @Transactional
    @Override
    public Role create(Role role) {
        if (roleRepository.findByTitle(role.getTitle()).isPresent()) {
            throw new DuplicateResourceException("Role with title '" + role.getTitle() + "' already exists");
        }
        return roleRepository.save(role);
    }

    @Audited(
            action = AuditAction.UPDATE,
            entityName = AuditEntityNames.ROLE,
            description = "Updated role",
            captureNewValue = true
    )
    @Transactional
    @Override
    public Role update(Long id, Role data) {
        Role existing = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        if (data.getTitle() != null && !data.getTitle().equals(existing.getTitle())) {
            if (roleRepository.findByTitle(data.getTitle()).isPresent()) {
                throw new DuplicateResourceException("Role with title '" + data.getTitle() + "' already exists");
            }
            existing.setTitle(data.getTitle());
        }

        if (data.getDescription() != null) {
            existing.setDescription(data.getDescription());
        }

        return roleRepository.save(existing);
    }

    @Audited(
            action = AuditAction.DELETE,
            entityName = AuditEntityNames.ROLE,
            description = "Deleted role",
            captureNewValue = false
    )
    @Transactional
    @Override
    public void delete(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Role not found with id: " + id);
        }
        roleRepository.deleteById(id);
    }
}
