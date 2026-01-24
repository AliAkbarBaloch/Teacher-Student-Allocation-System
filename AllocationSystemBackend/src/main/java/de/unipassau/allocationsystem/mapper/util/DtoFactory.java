package de.unipassau.allocationsystem.mapper.util;

import java.util.function.Function;

/**
 * Utility for creating response DTOs from entities.
 * Eliminates duplicate fromEntity factory logic across DTOs.
 */
public class DtoFactory {
    /**
     * Creates a DTO from an entity using a converter function.
     * Handles null checks automatically.
     * 
     * @param entity The entity to convert (may be null)
     * @param converter Function to convert entity to DTO
     * @return The DTO, or null if entity is null
     */
    public static <E, D> D fromEntity(E entity, Function<E, D> converter) {
        if (entity == null) {
            return null;
        }
        return converter.apply(entity);
    }
}
