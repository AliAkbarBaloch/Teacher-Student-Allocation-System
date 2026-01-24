package de.unipassau.allocationsystem.dto.common;

/**
 * Generic interface for entity-to-DTO conversion.
 * Defines a contract for DTOs that can be constructed from entities.
 * 
 * @param <E> The entity type
 * @param <D> The DTO type
 */
@FunctionalInterface
public interface EntityToDto<E, D> {
    /**
     * Converts an entity to a DTO.
     * 
     * @param entity The entity to convert
     * @return The DTO, or null if entity is null
     */
    D fromEntity(E entity);
}
