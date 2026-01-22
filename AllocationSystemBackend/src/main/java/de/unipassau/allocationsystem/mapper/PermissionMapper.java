package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.permission.PermissionCreateDto;
import de.unipassau.allocationsystem.dto.permission.PermissionResponseDto;
import de.unipassau.allocationsystem.dto.permission.PermissionUpdateDto;
import de.unipassau.allocationsystem.dto.permission.PermissionUpsertDto;
import de.unipassau.allocationsystem.entity.Permission;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
/**
 * Mapper for converting between Permission entities and DTOs.
 * Handles permission creation, updates, and response transformations for RBAC.
 */
public class PermissionMapper implements BaseMapper<Permission, PermissionCreateDto, PermissionUpdateDto, PermissionResponseDto> {

    @Override
    public Permission toEntityCreate(PermissionCreateDto createDto) {
        if (createDto == null) {
            return null;
        }
        Permission entity = new Permission();
        populateEntity(entity, createDto);
        return entity;
    }

    @Override
    public Permission toEntityUpdate(PermissionUpdateDto updateDto) {
        if (updateDto == null) {
            return null;
        }
        Permission entity = new Permission();
        populateEntity(entity, updateDto);
        return entity;
    }

    /**
     * Populates entity from DTO using common interface.
     * 
     * @param entity Target entity
     * @param dto Source DTO (create or update)
     */
    private void populateEntity(Permission entity, PermissionUpsertDto dto) {
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
    }

    @Override
    public PermissionResponseDto toResponseDto(Permission entity) {
        return entity == null ? null : buildResponseDto(entity);
    }

    /**
     * Builds response DTO from entity.
     * 
     * @param entity Source entity
     * @return Response DTO
     */
    private PermissionResponseDto buildResponseDto(Permission entity) {
        return new PermissionResponseDto(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    @Override
    public List<PermissionResponseDto> toResponseDtoList(List<Permission> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateEntityFromDto(PermissionUpdateDto updateDto, Permission entity) {
        if (updateDto == null || entity == null) {
            return;
        }
        setIfNotNull(updateDto.getTitle(), entity::setTitle);
        setIfNotNull(updateDto.getDescription(), entity::setDescription);
    }

    /**
     * Sets a value on entity if not null.
     * 
     * @param value Value to set
     * @param setter Setter method reference
     * @param <T> Type of value
     */
    private static <T> void setIfNotNull(T value, java.util.function.Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
