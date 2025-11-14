package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.RoleDto;
import de.unipassau.allocationsystem.entity.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RoleMapper {
    public Role toEntity(RoleDto dto) {
        if (dto == null) {
            return null;
        }
        return new Role(
                dto.getId(),
                dto.getTitle(),
                dto.getDescription(),
                dto.getCreatedAt(),
                dto.getUpdatedAt()
        );
    }

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

    public List<RoleDto> toDtoList(List<Role> entities) {
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
