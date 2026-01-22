package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.permission.PermissionCreateDto;
import de.unipassau.allocationsystem.dto.permission.PermissionResponseDto;
import de.unipassau.allocationsystem.dto.permission.PermissionUpdateDto;
import de.unipassau.allocationsystem.dto.permission.PermissionUpsertDto;
import de.unipassau.allocationsystem.entity.Permission;
import de.unipassau.allocationsystem.mapper.util.MapperUtil;
import org.springframework.stereotype.Component;

@Component
/**
 * Mapper for converting between Permission entities and DTOs.
 * Handles permission creation, updates, and response transformations for RBAC.
 */
public class PermissionMapper implements BaseMapper<Permission, PermissionCreateDto, PermissionUpdateDto, PermissionResponseDto> {

    @Override
    public Permission toEntityCreate(PermissionCreateDto createDto) {
        return toNewEntity(createDto, Permission::new, this::populateEntity);
    }

    @Override
    public Permission toEntityUpdate(PermissionUpdateDto updateDto) {
        return toNewEntity(updateDto, Permission::new, this::populateEntity);
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
        return PermissionResponseDto.fromEntity(entity);
    }

    @Override
    public void updateEntityFromDto(PermissionUpdateDto updateDto, Permission entity) {
        if (updateDto == null || entity == null) {
            return;
        }
        MapperUtil.setIfNotNull(updateDto.getTitle(), entity::setTitle);
        MapperUtil.setIfNotNull(updateDto.getDescription(), entity::setDescription);
    }
}
