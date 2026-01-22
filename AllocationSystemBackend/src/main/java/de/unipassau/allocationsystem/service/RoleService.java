package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.audit.RoleCreateAudit;
import de.unipassau.allocationsystem.audit.RoleUpdateAudit;
import de.unipassau.allocationsystem.audit.RoleDeleteAudit;
import de.unipassau.allocationsystem.audit.RoleViewAudit;
import de.unipassau.allocationsystem.audit.RoleViewAllAudit;
import de.unipassau.allocationsystem.audit.RoleViewByIdAudit;
import de.unipassau.allocationsystem.entity.Role;
import de.unipassau.allocationsystem.repository.RoleRepository;
import de.unipassau.allocationsystem.utils.PaginationUtils;
import de.unipassau.allocationsystem.utils.SortFieldUtils;
import de.unipassau.allocationsystem.utils.SearchSpecificationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
/**
 * Service for managing user roles.
 * Handles CRUD operations for role entities.
 */
public class RoleService  implements CrudService<Role, Long> {

    private final RoleRepository roleRepository;

    /**
     * Returns the sortable fields metadata.
     * 
     * @return list of sort field metadata
     */
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

    private Specification<Role> buildSearchSpecification(String searchValue) {
        // Search across title and description
        return SearchSpecificationUtils.buildMultiFieldLikeSpecification(
            new String[]{"title", "description"}, searchValue
        );
    }

    /**
     * Checks if a role with the given title exists.
     * 
     * @param title the role title to check
     * @return true if title exists, false otherwise
     */
    public boolean titleExists(String title) {
        return roleRepository.findByTitle(title).isPresent();
    }

    /**
     * Validates that the role title is unique, throws exception if duplicate found.
     * 
     * @param title the role title to validate
     * @throws DuplicateResourceException if title already exists
     */
    private void validateRoleTitleUniqueness(String title) {
        if (roleRepository.findByTitle(title).isPresent()) {
            throw new DuplicateResourceException("Role with title '" + title + "' already exists");
        }
    }

    /**
     * Validates that a new role title doesn't conflict with existing records (for updates).
     * Allows the same title if it's the current role being updated.
     * 
     * @param newTitle the new role title
     * @param oldTitle the old role title
     * @throws DuplicateResourceException if new title conflicts with another role's title
     */
    private void validateRoleTitleForUpdate(String newTitle, String oldTitle) {
        if (!newTitle.equals(oldTitle) && roleRepository.findByTitle(newTitle).isPresent()) {
            throw new DuplicateResourceException("Role with title '" + newTitle + "' already exists");
        }
    }

    /**
     * Validates that a role exists with the given ID.
     * 
     * @param id the role ID
     * @throws ResourceNotFoundException if not found
     */
    private void validateExistence(Long id) {
        if (!roleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Role not found with id: " + id);
        }
    }

    /**
     * Applies field updates from source to target role.
     * Only updates fields that are non-null in the source.
     * 
     * @param existing the target role to update
     * @param data the source data with new values
     */
    private void applyFieldUpdates(Role existing, Role data) {
        if (data.getTitle() != null) {
            validateRoleTitleForUpdate(data.getTitle(), existing.getTitle());
            existing.setTitle(data.getTitle());
        }
        if (data.getDescription() != null) {
            existing.setDescription(data.getDescription());
        }
    }

    @Override
    public boolean existsById(Long id) {
        return roleRepository.existsById(id);
    }

        @RoleViewAudit
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

        @RoleViewAllAudit
    @Transactional(readOnly = true)
    @Override
    public List<Role> getAll() {
        return roleRepository.findAll();
    }

        @RoleViewByIdAudit
    @Transactional(readOnly = true)
    @Override
    public Optional<Role> getById(Long id) {
        return roleRepository.findById(id);
    }

        @RoleCreateAudit
    @Transactional
    @Override
    public Role create(Role role) {
        validateRoleTitleUniqueness(role.getTitle());
        return roleRepository.save(role);
    }

        @RoleUpdateAudit
    @Transactional
    @Override
    public Role update(Long id, Role data) {
        Role existing = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        applyFieldUpdates(existing, data);
        return roleRepository.save(existing);
    }

        @RoleDeleteAudit
    @Transactional
    @Override
    public void delete(Long id) {
        validateExistence(id);
        roleRepository.deleteById(id);
    }
}
