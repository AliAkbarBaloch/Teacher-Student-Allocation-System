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
    List<RESPONSE_DTO> toResponseDtoList(List<ENT> entities);
    
    /**
     * Updates an existing entity from an update DTO.
     * 
     * @param updateDto the update DTO with new values
     * @param entity the entity to update
     */
    void updateEntityFromDto(UPDATE_DTO updateDto, ENT entity);
}
