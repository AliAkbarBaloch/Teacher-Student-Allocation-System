package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.audit.RoleCreateAudit;
import de.unipassau.allocationsystem.audit.RoleDeleteAudit;
import de.unipassau.allocationsystem.audit.RoleUpdateAudit;
import de.unipassau.allocationsystem.audit.RoleViewAllAudit;
import de.unipassau.allocationsystem.audit.RoleViewAudit;
import de.unipassau.allocationsystem.audit.RoleViewByIdAudit;
import de.unipassau.allocationsystem.entity.Role;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.RoleRepository;
import de.unipassau.allocationsystem.utils.PaginationUtils;
import de.unipassau.allocationsystem.utils.SearchSpecificationUtils;
import de.unipassau.allocationsystem.utils.SortFieldUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class RoleService implements CrudService<Role, Long> {

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

    @Override
    public boolean existsById(Long id) {
        return roleRepository.existsById(id);
    }

    /**
     * Loads an existing role or throws if not found.
     */
    private Role getExistingOrThrow(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
    }

    /**
     * Ensures the given title is unique.
     * If currentId is non-null, the record with that id is allowed to keep the same title.
     */
    private void assertTitleUniqueForId(String title, Long currentId) {
        roleRepository.findByTitle(title).ifPresent(existing -> {
            Long existingId = existing.getId();
            boolean isDifferent = (currentId == null) || (existingId != null && !existingId.equals(currentId));
            if (isDifferent) {
                throw new DuplicateResourceException("Role with title '" + title + "' already exists");
            }
        });
    }

    /**
     * Applies field updates from source to target role.
     * Only updates fields that are non-null in the source.
     */
    private void applyFieldUpdates(Role existing, Role data) {
        String newTitle = data.getTitle();
        if (newTitle != null && !newTitle.equals(existing.getTitle())) {
            assertTitleUniqueForId(newTitle, existing.getId());
            existing.setTitle(newTitle);
        }

        if (data.getDescription() != null) {
            existing.setDescription(data.getDescription());
        }
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
        assertTitleUniqueForId(role.getTitle(), null);
        return roleRepository.save(role);
    }

    @RoleUpdateAudit
    @Transactional
    @Override
    public Role update(Long id, Role data) {
        Role existing = getExistingOrThrow(id);
        applyFieldUpdates(existing, data);
        return roleRepository.save(existing);
    }

    @RoleDeleteAudit
    @Transactional
    @Override
    public void delete(Long id) {
        getExistingOrThrow(id);
        roleRepository.deleteById(id);
    }
}
