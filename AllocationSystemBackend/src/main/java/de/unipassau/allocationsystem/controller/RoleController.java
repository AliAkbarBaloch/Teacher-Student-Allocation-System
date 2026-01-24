package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.role.RoleCreateDto;
import de.unipassau.allocationsystem.dto.role.RoleResponseDto;
import de.unipassau.allocationsystem.dto.role.RoleUpdateDto;
import de.unipassau.allocationsystem.entity.Role;
import de.unipassau.allocationsystem.mapper.BaseMapper;
import de.unipassau.allocationsystem.mapper.RoleMapper;
import de.unipassau.allocationsystem.service.CrudService;
import de.unipassau.allocationsystem.service.RoleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing roles.
 * Provides CRUD operations for role entities used in role-based access control.
 */
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Role management APIs")
public class RoleController
        extends CrudControllerBase<Role, RoleCreateDto, RoleUpdateDto, RoleResponseDto> {

    private final RoleService roleService;
    private final RoleMapper roleMapper;

    @Override
    protected CrudService<Role, Long> getService() {
        return roleService;
    }

    @Override
    protected BaseMapper<Role, RoleCreateDto, RoleUpdateDto, RoleResponseDto> getMapper() {
        return roleMapper;
    }
}
