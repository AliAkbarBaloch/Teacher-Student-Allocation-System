package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.utils.PaginationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract base service providing common CRUD implementations.
 * This is OPTIONAL - use it to reduce boilerplate code.
 *
 * You can still implement CrudService directly if you need custom logic.
 *
 * @param <T> Entity type
 * @param <ID> Primary key type
 * @param <R> Repository type
 */
public abstract class BaseService<T, ID, R extends JpaRepository<T, ID> & JpaSpecificationExecutor<T>> implements CrudService<T, ID> {

    protected final R repository;

    protected BaseService(R repository) {
        this.repository = repository;
    }

    /**
     * Build search specification for filtering entities.
     * Override this method to customize search logic.
     *
     * @param searchValue Search string
     * @return JPA Specification for filtering
     */
    protected abstract Specification<T> buildSearchSpecification(String searchValue);

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPaginated(Map<String, String> queryParams, String searchValue) {
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        Pageable pageable = PageRequest.of(params.page() - 1, params.pageSize(), sort);

        Specification<T> spec = buildSearchSpecification(searchValue);
        Page<T> page = repository.findAll(spec, pageable);

        return PaginationUtils.formatPaginationResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> getAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<T> getById(ID id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(ID id) {
        return repository.existsById(id);
    }

    /**
     * Helper method to get entity or throw exception.
     * Use this in update/delete methods.
     */
    protected T getEntityOrThrow(ID id, String entityName) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(entityName + " not found with id: " + id));
    }

    /**
     * Count total entities.
     */
    @Transactional(readOnly = true)
    public long count() {
        return repository.count();
    }
}
