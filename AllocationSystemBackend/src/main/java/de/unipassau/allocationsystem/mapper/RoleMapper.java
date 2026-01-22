package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.role.RoleCreateDto;
import de.unipassau.allocationsystem.dto.role.RoleUpdateDto;
import de.unipassau.allocationsystem.dto.role.RoleResponseDto;
import de.unipassau.allocationsystem.dto.role.RoleUpsertDto;
import de.unipassau.allocationsystem.entity.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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
        if (entity == null) {
            return null;
        }
        return new RoleResponseDto(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    @Override
    public List<RoleResponseDto> toResponseDtoList(List<Role> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateEntityFromDto(RoleUpdateDto updateDto, Role entity) {
        if (updateDto == null || entity == null) {
            return;
        }
        setIfNotNull(updateDto.getTitle(), entity::setTitle);
        setIfNotNull(updateDto.getDescription(), entity::setDescription);
    }

    private static <T> void setIfNotNull(T value, java.util.function.Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}