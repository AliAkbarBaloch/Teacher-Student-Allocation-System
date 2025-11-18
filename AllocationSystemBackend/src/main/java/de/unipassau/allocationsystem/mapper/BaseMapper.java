package de.unipassau.allocationsystem.mapper;

import java.util.List;

/**
 * Base interface for all mapper classes.
 * Defines standard methods that every mapper must implement.
 *
 * @param <ENT> Entity type
 * @param <DTO> DTO type
 */
public interface BaseMapper<ENT, DTO> {
    /**
     * Convert DTO to Entity.
     *
     * @param dto the DTO to convert
     * @return the converted entity, or null if dto is null
     */
    ENT toEntity(DTO dto);

    /**
     * Convert Entity to DTO.
     *
     * @param entity the entity to convert
     * @return the converted DTO, or null if entity is null
     */
    DTO toDto(ENT entity);

    /**
     * Convert a list of entities to a list of DTOs.
     *
     * @param entities the list of entities to convert
     * @return the converted list of DTOs
     */
    List<DTO> toDtoList(List<ENT> entities);

    /**
     * Convert a list of DTOs to a list of entities.
     * Optional - implement only if needed.
     *
     * @param dtos the list of DTOs to convert
     * @return the converted list of entities
     */
    default List<ENT> toEntityList(List<DTO> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(java.util.stream.Collectors.toList());
    }
}
