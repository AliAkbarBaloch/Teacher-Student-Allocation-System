package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.RolePermissionDto;
import de.unipassau.allocationsystem.entity.Permission;
import de.unipassau.allocationsystem.entity.Role;
import de.unipassau.allocationsystem.entity.RolePermission;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.PermissionRepository;
import de.unipassau.allocationsystem.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RolePermissionMapper implements BaseMapper<RolePermission, RolePermissionDto> {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public RolePermission toEntity(RolePermissionDto dto) {
        if (dto == null) {
            return null;
        }

        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + dto.getRoleId()));

        Permission permission = permissionRepository.findById(dto.getPermissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + dto.getPermissionId()));

        RolePermission entity = new RolePermission();
        if (dto.getId() != null && dto.getId() > 0) {
            entity.setId(dto.getId());
        }
        entity.setRole(role);
        entity.setPermission(permission);
        entity.setAccessLevel(dto.getAccessLevel());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());

        return entity;
    }

    @Override
    public RolePermissionDto toDto(RolePermission entity) {
        if (entity == null) {
            return null;
        }

        return RolePermissionDto.builder()
                .id(entity.getId())
                .roleId(entity.getRole().getId())
                .permissionId(entity.getPermission().getId())
                .accessLevel(entity.getAccessLevel())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .roleTitle(entity.getRole().getTitle())
                .permissionTitle(entity.getPermission().getTitle())
                .build();
    }

    @Override
    public List<RolePermissionDto> toDtoList(List<RolePermission> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
