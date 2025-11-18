package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.entity.Permission;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.PermissionRepository;
import de.unipassau.allocationsystem.utils.PaginationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public boolean titleExists(String title) {
        return permissionRepository.findByTitle(title).isPresent();
    }

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

    private Specification<Permission> buildSearchSpecification(String searchValue) {
        if (searchValue == null || searchValue.trim().isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }

        String likePattern = "%" + searchValue.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), likePattern),
                cb.like(cb.lower(root.get("description")), likePattern)
        );
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = "PERMISSION",
            description = "Viewed list of permissions",
            captureNewValue = false
    )
    @Transactional
    public Map<String, Object> getPaginated(Map<String, String> queryParams, boolean includeRelations, String searchValue) {
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        Pageable pageable = PageRequest.of(params.page() - 1, params.pageSize(), sort);

        Specification<Permission> spec = buildSearchSpecification(searchValue);
        Page<Permission> page = permissionRepository.findAll(spec, pageable);

        return PaginationUtils.formatPaginationResponse(page);
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = "PERMISSION",
            description = "Viewed all permissions",
            captureNewValue = false
    )
    public List<Permission> getAll() {
        return permissionRepository.findAll();
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = "PERMISSION",
            description = "Viewed permission by id",
            captureNewValue = false
    )
    public Optional<Permission> getById(Long id) {
        return permissionRepository.findById(id);
    }

    @Audited(
            action = AuditAction.CREATE,
            entityName = "PERMISSION",
            description = "Created new permission",
            captureNewValue = true
    )
    @Transactional
    public Permission create(Permission permission) {
        if (permissionRepository.findByTitle(permission.getTitle()).isPresent()) {
            throw new DuplicateResourceException("Permission with title '" + permission.getTitle() + "' already exists");
        }
        return permissionRepository.save(permission);
    }

    @Audited(
            action = AuditAction.UPDATE,
            entityName = "PERMISSION",
            description = "Updated permission",
            captureNewValue = true
    )
    @Transactional
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
            entityName = "PERMISSION",
            description = "Deleted permission",
            captureNewValue = false
    )
    @Transactional
    public void delete(Long id) {
        if (!permissionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Permission not found with id: " + id);
        }
        permissionRepository.deleteById(id);
    }
    
}
