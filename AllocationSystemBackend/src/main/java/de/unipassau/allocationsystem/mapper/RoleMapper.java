package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.role.RoleCreateDto;
import de.unipassau.allocationsystem.dto.role.RoleUpdateDto;
import de.unipassau.allocationsystem.dto.role.RoleResponseDto;
import de.unipassau.allocationsystem.entity.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RoleMapper implements BaseMapper<Role, RoleCreateDto, RoleUpdateDto, RoleResponseDto> {

    @Override
    public Role toEntityCreate(RoleCreateDto createDto) {
        if (createDto == null) return null;
        Role entity = new Role();
        entity.setTitle(createDto.getTitle());
        entity.setDescription(createDto.getDescription());
        return entity;
    }

    @Override
    public Role toEntityUpdate(RoleUpdateDto updateDto) {
        if (updateDto == null) return null;
        Role entity = new Role();
        entity.setTitle(updateDto.getTitle());
        entity.setDescription(updateDto.getDescription());
        return entity;
    }

    @Override
    public RoleResponseDto toResponseDto(Role entity) {
        if (entity == null) return null;
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
        if (entities == null) return null;
        return entities.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateEntityFromDto(RoleUpdateDto updateDto, Role entity) {
        if (updateDto == null || entity == null) return;
        if (updateDto.getTitle() != null) {
            entity.setTitle(updateDto.getTitle());
        }
        if (updateDto.getDescription() != null) {
            entity.setDescription(updateDto.getDescription());
        }
    }
}