package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.PermissionDto;
import de.unipassau.allocationsystem.entity.Permission;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PermissionMapper {

    public Permission toEntity(PermissionDto dto) {
        if (dto == null) {
            return null;
        }
        return new Permission(
                dto.getId(),
                dto.getTitle(),
                dto.getDescription(),
                dto.getCreatedAt(),
                dto.getUpdatedAt()
        );
    }

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

    public List<PermissionDto> toDtoList(List<Permission> entities) {
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
