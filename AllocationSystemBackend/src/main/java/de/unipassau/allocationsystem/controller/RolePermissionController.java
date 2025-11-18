package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.rolepermission.RolePermissionCreateDto;
import de.unipassau.allocationsystem.dto.rolepermission.RolePermissionResponseDto;
import de.unipassau.allocationsystem.dto.rolepermission.RolePermissionUpdateDto;
import de.unipassau.allocationsystem.entity.RolePermission;
import de.unipassau.allocationsystem.mapper.RolePermissionMapper;
import de.unipassau.allocationsystem.service.RolePermissionService;
import de.unipassau.allocationsystem.utils.ResponseHandler;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/role-permissions")
@RequiredArgsConstructor
@Tag(name = "RolePermissions", description = "Role Permissions management APIs")
public class RolePermissionController {
    private final RolePermissionService rolePermissionService;
    private final RolePermissionMapper rolePermissionMapper;

    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        List<Map<String, String>> result = rolePermissionService.getSortFields();
        return ResponseHandler.success("Sort fields retrieved successfully", result);
    }

    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        Map<String, Object> result = rolePermissionService.getPaginated(queryParams, searchValue);
        return ResponseHandler.success("Role permissions retrieved successfully (paginated)", result);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        List<RolePermissionResponseDto> result = rolePermissionMapper.toResponseDtoList(rolePermissionService.getAll());
        return ResponseHandler.success("Role permissions retrieved successfully", result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        RolePermissionResponseDto result = rolePermissionService.getById(id)
                .map(rolePermissionMapper::toResponseDto)
                .orElseThrow(() -> new NoSuchElementException("Role permission not found with id: " + id));
        return ResponseHandler.success("Role permission retrieved successfully", result);
    }

    @GetMapping("/by-role/{roleId}")
    public ResponseEntity<?> getByRoleId(@PathVariable Long roleId) {
        List<RolePermissionResponseDto> result = rolePermissionMapper.toResponseDtoList(
                rolePermissionService.getByRoleId(roleId));
        return ResponseHandler.success("Role permissions retrieved successfully", result);
    }

    @GetMapping("/by-permission/{permissionId}")
    public ResponseEntity<?> getByPermissionId(@PathVariable Long permissionId) {
        List<RolePermissionResponseDto> result = rolePermissionMapper.toResponseDtoList(
                rolePermissionService.getByPermissionId(permissionId));
        return ResponseHandler.success("Role permissions retrieved successfully", result);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody RolePermissionCreateDto dto) {
        try {
            RolePermission rolePermission = rolePermissionMapper.toEntityCreate(dto);
            RolePermission created = rolePermissionService.create(rolePermission);
            return ResponseHandler.created("Role permission created successfully",
                    rolePermissionMapper.toResponseDto(created));
        } catch (DataIntegrityViolationException | IllegalArgumentException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody RolePermissionUpdateDto dto) {
        try {
            RolePermission rolePermission = rolePermissionMapper.toEntityUpdate(dto);
            RolePermission updated = rolePermissionService.update(id, rolePermission);
            return ResponseHandler.updated("Role permission updated successfully",
                    rolePermissionMapper.toResponseDto(updated));
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Role permission not found");
        } catch (DataIntegrityViolationException | IllegalArgumentException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            rolePermissionService.delete(id);
            return ResponseHandler.noContent();
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Role permission not found");
        }
    }
}
