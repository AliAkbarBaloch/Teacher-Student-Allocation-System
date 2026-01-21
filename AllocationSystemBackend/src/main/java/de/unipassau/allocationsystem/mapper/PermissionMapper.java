package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.permission.PermissionCreateDto;
import de.unipassau.allocationsystem.dto.permission.PermissionResponseDto;
import de.unipassau.allocationsystem.dto.permission.PermissionUpdateDto;
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
        entity.setTitle(createDto.getTitle());
        entity.setDescription(createDto.getDescription());
        return entity;
    }

    @Override
    public Permission toEntityUpdate(PermissionUpdateDto updateDto) {
        if (updateDto == null) {
            return null;
        }
        Permission entity = new Permission();
        entity.setTitle(updateDto.getTitle());
        entity.setDescription(updateDto.getDescription());
        return entity;
    }

    @Override
    public PermissionResponseDto toResponseDto(Permission entity) {
        if (entity == null) {
            return null;
        }
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
        if (updateDto.getTitle() != null) {
            entity.setTitle(updateDto.getTitle());
        }
        if (updateDto.getDescription() != null) {
            entity.setDescription(updateDto.getDescription());
        }
    }
}
