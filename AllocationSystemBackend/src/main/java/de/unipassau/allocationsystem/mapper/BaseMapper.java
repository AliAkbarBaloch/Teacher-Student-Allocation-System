package de.unipassau.allocationsystem.mapper;

import java.util.List;

/**
 * Base mapper interface for entity-DTO conversions.
 * Provides standard mapping methods for create, update, and response operations.
 * 
 * @param <ENT> Entity type
 * @param <CREATE_DTO> Create DTO type
 * @param <UPDATE_DTO> Update DTO type
 * @param <RESPONSE_DTO> Response DTO type
 */
public interface BaseMapper<ENT, CREATE_DTO, UPDATE_DTO, RESPONSE_DTO> {
        /**
         * Generic helper to create a new entity from a DTO using a factory and populator.
         * @param dto The DTO (create or update)
         * @param factory Entity constructor (e.g., MyEntity::new)
         * @param populator Populates the entity from the DTO
         * @return New entity or null if dto is null
         */
        default <D> ENT toNewEntity(D dto, java.util.function.Supplier<ENT> factory, java.util.function.BiConsumer<ENT, D> populator) {
            if (dto == null) {
                return null;
            }
            ENT entity = factory.get();
            populator.accept(entity, dto);
            return entity;
        }
    /**
     * Converts a create DTO to a new entity.
     * 
     * @param createDto the create DTO
     * @return new entity instance
     */
    ENT toEntityCreate(CREATE_DTO createDto);
    
    /**
     * Converts an update DTO to an entity for update operations.
     * 
     * @param createDto the update DTO
     * @return entity instance with update data
     */
    ENT toEntityUpdate(UPDATE_DTO createDto);

    /**
     * Converts an entity to a response DTO.
     * 
     * @param entity the entity
     * @return response DTO
     */
    RESPONSE_DTO toResponseDto(ENT entity);
    
    /**
     * Converts a list of entities to response DTOs.
     * 
     * @param entities list of entities
     * @return list of response DTOs
     */
    default List<RESPONSE_DTO> toResponseDtoList(List<ENT> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponseDto)
                .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Updates an existing entity from an update DTO.
     * 
     * @param updateDto the update DTO with new values
     * @param entity the entity to update
     */
    void updateEntityFromDto(UPDATE_DTO updateDto, ENT entity);
}
