package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.rolepermission.RolePermissionCreateDto;
import de.unipassau.allocationsystem.dto.rolepermission.RolePermissionResponseDto;
import de.unipassau.allocationsystem.dto.rolepermission.RolePermissionUpdateDto;
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
public class RolePermissionMapper implements BaseMapper<RolePermission, RolePermissionCreateDto, RolePermissionUpdateDto, RolePermissionResponseDto> {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public RolePermission toEntityCreate(RolePermissionCreateDto createDto) {
        if (createDto == null) return null;

        Role role = roleRepository.findById(createDto.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + createDto.getRoleId()));
        Permission permission = permissionRepository.findById(createDto.getPermissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + createDto.getPermissionId()));

        RolePermission entity = new RolePermission();
        entity.setRole(role);
        entity.setPermission(permission);
        entity.setAccessLevel(createDto.getAccessLevel());
        return entity;
    }

    @Override
    public RolePermission toEntityUpdate(RolePermissionUpdateDto updateDto) {
        // This finds entities only by ID if using in PATCH-style. Otherwise, use in-place update method.
        // Here just create a partial entity
        RolePermission entity = new RolePermission();
        if (updateDto.getRoleId() != null) {
            Role role = roleRepository.findById(updateDto.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + updateDto.getRoleId()));
            entity.setRole(role);
        }
        if (updateDto.getPermissionId() != null) {
            Permission permission = permissionRepository.findById(updateDto.getPermissionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + updateDto.getPermissionId()));
            entity.setPermission(permission);
        }
        if (updateDto.getAccessLevel() != null) {
            entity.setAccessLevel(updateDto.getAccessLevel());
        }
        return entity;
    }

    @Override
    public RolePermissionResponseDto toResponseDto(RolePermission entity) {
        if (entity == null) return null;
        return new RolePermissionResponseDto(
                entity.getId(),
                entity.getRole().getId(),
                entity.getPermission().getId(),
                entity.getAccessLevel(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getRole().getTitle(),
                entity.getPermission().getTitle()
        );
    }

    @Override
    public List<RolePermissionResponseDto> toResponseDtoList(List<RolePermission> entities) {
        if (entities == null) return null;
        return entities.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateEntityFromDto(RolePermissionUpdateDto updateDto, RolePermission entity) {
        if (updateDto == null || entity == null) return;
        if (updateDto.getRoleId() != null) {
            Role role = roleRepository.findById(updateDto.getRoleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + updateDto.getRoleId()));
            entity.setRole(role);
        }
        if (updateDto.getPermissionId() != null) {
            Permission permission = permissionRepository.findById(updateDto.getPermissionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + updateDto.getPermissionId()));
            entity.setPermission(permission);
        }
        if (updateDto.getAccessLevel() != null) {
            entity.setAccessLevel(updateDto.getAccessLevel());
        }
    }
}
