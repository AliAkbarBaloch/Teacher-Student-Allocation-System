package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.rolepermission.RolePermissionCreateDto;
import de.unipassau.allocationsystem.dto.rolepermission.RolePermissionResponseDto;
import de.unipassau.allocationsystem.dto.rolepermission.RolePermissionUpdateDto;
import de.unipassau.allocationsystem.entity.RolePermission;
import de.unipassau.allocationsystem.mapper.BaseMapper;
import de.unipassau.allocationsystem.mapper.RolePermissionMapper;
import de.unipassau.allocationsystem.service.CrudService;
import de.unipassau.allocationsystem.service.RolePermissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing role permissions.
 * Provides CRUD operations for role permission entities used in role-based access control.
 */
@RestController
@RequestMapping("/role-permissions")
@RequiredArgsConstructor
@Tag(name = "RolePermissions", description = "Role Permissions management APIs")
public class RolePermissionController
        extends CrudControllerBase<RolePermission, RolePermissionCreateDto, RolePermissionUpdateDto, RolePermissionResponseDto> {

    private final RolePermissionService rolePermissionService;
    private final RolePermissionMapper rolePermissionMapper;

    @Override
    protected CrudService<RolePermission, Long> getService() {
        return rolePermissionService;
    }

    @Override
    protected BaseMapper<RolePermission, RolePermissionCreateDto, RolePermissionUpdateDto, RolePermissionResponseDto> getMapper() {
        return rolePermissionMapper;
    }
}