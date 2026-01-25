package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.role.RoleCreateDto;
import de.unipassau.allocationsystem.dto.role.RoleUpdateDto;
import de.unipassau.allocationsystem.dto.role.RoleResponseDto;
import de.unipassau.allocationsystem.dto.role.RoleUpsertDto;
import de.unipassau.allocationsystem.entity.Role;
import de.unipassau.allocationsystem.mapper.util.MapperUtil;
import org.springframework.stereotype.Component;

@Component
/**
 * Mapper for converting between Role entities and DTOs.
 * Handles role creation, updates, and response transformations for RBAC.
 */
public class RoleMapper implements BaseMapper<Role, RoleCreateDto, RoleUpdateDto, RoleResponseDto> {

    @Override
    public Role toEntityCreate(RoleCreateDto createDto) {
        return toNewEntity((RoleUpsertDto) createDto, Role::new, this::populateEntity);
    }

    @Override
    public Role toEntityUpdate(RoleUpdateDto updateDto) {
        return toNewEntity((RoleUpsertDto) updateDto, Role::new, this::populateEntity);
    }

    private void populateEntity(Role entity, RoleUpsertDto dto) {
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
    }

    @Override
    public RoleResponseDto toResponseDto(Role entity) {
        return RoleResponseDto.fromEntity(entity);
    }

    @Override
    public void updateEntityFromDto(RoleUpdateDto updateDto, Role entity) {
        if (updateDto == null || entity == null) {
            return;
        }
        MapperUtil.updateTitleAndDescription(updateDto.getTitle(), updateDto.getDescription(),
                entity::setTitle, entity::setDescription);
    }
}