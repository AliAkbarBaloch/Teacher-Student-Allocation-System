package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.PermissionDto;
import de.unipassau.allocationsystem.entity.Permission;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PermissionMapper implements BaseMapper<Permission, PermissionDto> {

    @Override
    public Permission toEntity(PermissionDto dto) {
        if (dto == null) {
            return null;
        }
        Permission entity = new Permission();
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
    public PermissionDto toDto(Permission entity) {
        if (entity == null) {
            return null;
        }
        return new PermissionDto(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    @Override
    public List<PermissionDto> toDtoList(List<Permission> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
