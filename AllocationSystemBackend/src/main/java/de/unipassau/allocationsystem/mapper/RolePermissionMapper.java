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
public class RolePermissionMapper {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RolePermission toEntity(RolePermissionDto dto) {
        if (dto == null) {
            return null;
        }

        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + dto.getRoleId()));

        Permission permission = permissionRepository.findById(dto.getPermissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + dto.getPermissionId()));

        RolePermission rolePermission = new RolePermission();
        rolePermission.setId(dto.getId());
        rolePermission.setRole(role);
        rolePermission.setPermission(permission);
        rolePermission.setAccessLevel(dto.getAccessLevel());
        rolePermission.setCreatedAt(dto.getCreatedAt());
        rolePermission.setUpdatedAt(dto.getUpdatedAt());

        return rolePermission;
    }

    public RolePermissionDto toDto(RolePermission entity) {
        if (entity == null) {
            return null;
        }

        RolePermissionDto dto = new RolePermissionDto();
        dto.setId(entity.getId());
        dto.setRoleId(entity.getRole().getId());
        dto.setPermissionId(entity.getPermission().getId());
        dto.setAccessLevel(entity.getAccessLevel());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setRoleTitle(entity.getRole().getTitle());
        dto.setPermissionTitle(entity.getPermission().getTitle());

        return dto;
    }

    public List<RolePermissionDto> toDtoList(List<RolePermission> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
