package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.RoleDto;
import de.unipassau.allocationsystem.entity.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RoleMapper implements BaseMapper<Role, RoleDto> {

    @Override
    public Role toEntity(RoleDto dto) {
        if (dto == null) {
            return null;
        }
        Role entity = new Role();
        if (dto.getId() != null && dto.getId() > 0) {
            entity.setId(dto.getId());
        }
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());
        return entity;
    }

    @Override
    public RoleDto toDto(Role entity) {
        if (entity == null) {
            return null;
        }
        return new RoleDto(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    @Override
    public List<RoleDto> toDtoList(List<Role> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
