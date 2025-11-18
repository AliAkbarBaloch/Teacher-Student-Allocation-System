package de.unipassau.allocationsystem.mapper;

import java.util.List;

/**
 * Extended mapper interface for entities with create/update/response DTOs.
 *
 * @param <E> Entity type
 * @param <C> Create DTO type
 * @param <U> Update DTO type
 * @param <R> Response DTO type
 */
public interface CrudMapper<E, C, U, R> {
    /**
     * Convert create DTO to entity.
     */
    E toEntity(C createDto);

    /**
     * Convert entity to response DTO.
     */
    R toResponseDto(E entity);

    /**
     * Update entity from update DTO (partial update).
     */
    void updateEntityFromDto(U updateDto, E entity);

    /**
     * Convert list of entities to response DTOs.
     */
    default List<R> toResponseDtoList(List<E> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponseDto)
                .collect(java.util.stream.Collectors.toList());
    }
}
