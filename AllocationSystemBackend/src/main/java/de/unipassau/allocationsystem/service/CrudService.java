package de.unipassau.allocationsystem.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Standard CRUD service interface.
 * All entity services MUST implement this interface to ensure consistent required methods and naming.
 *
 * @param <T> Entity type or Response DTO type
 * @param <ID> Primary key type (usually Long)
 */
public interface CrudService<T, ID> {

    /**
     * Get available sort fields for pagination.
     * Used by frontend to display sort options.
     *
     * @return List of maps with keys: "key" (field name), "label" (display name)
     */
    List<Map<String, String>> getSortFields();

    /**
     * Check if an entity exists by ID.
     *
     * @param id Entity identifier
     * @return true if exists, false otherwise
     */
    boolean existsById(ID id);

    /**
     * Get paginated results with optional search.
     *
     * Standard query params:
     * - page: Page number (1-indexed)
     * - pageSize: Number of items per page
     * - sortBy: Field to sort by
     * - sortOrder: ASC or DESC
     *
     * @param queryParams Pagination parameters
     * @param searchValue Search string to filter results (can be null)
     * @return Map with keys: data, currentPage, totalPages, totalItems, pageSize
     */
    Map<String, Object> getPaginated(Map<String, String> queryParams, String searchValue);

    /**
     * Get all entities without pagination.
     * Use with caution for large datasets.
     *
     * @return List of all entities
     */
    List<T> getAll();

    /**
     * Get a single entity by its identifier.
     *
     * @param id Entity identifier
     * @return Optional containing the entity if found
     */
    Optional<T> getById(ID id);

    /**
     * Create a new entity.
     * Should validate uniqueness constraints and business rules.
     *
     * @param entity Entity to create (can be Entity or CreateDto)
     * @return Created entity with generated ID
     * @throws de.unipassau.allocationsystem.exception.DuplicateResourceException if entity already exists
     */
    T create(T entity);

    /**
     * Update an existing entity.
     * Should validate entity exists and uniqueness constraints.
     *
     * @param id Entity identifier
     * @param entity Entity data to update (partial updates supported)
     * @return Updated entity
     * @throws de.unipassau.allocationsystem.exception.ResourceNotFoundException if entity not found
     * @throws de.unipassau.allocationsystem.exception.DuplicateResourceException if update violates uniqueness
     */
    T update(ID id, T entity);

    /**
     * Delete an entity by its identifier.
     * Implementation may be hard delete or soft delete depending on requirements.
     *
     * @param id Entity identifier
     * @throws de.unipassau.allocationsystem.exception.ResourceNotFoundException if entity not found
     */
    void delete(ID id);
}
