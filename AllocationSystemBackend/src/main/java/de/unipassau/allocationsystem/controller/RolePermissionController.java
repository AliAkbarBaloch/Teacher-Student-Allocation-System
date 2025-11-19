package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.rolepermission.RolePermissionCreateDto;
import de.unipassau.allocationsystem.dto.rolepermission.RolePermissionResponseDto;
import de.unipassau.allocationsystem.dto.rolepermission.RolePermissionUpdateDto;
import de.unipassau.allocationsystem.entity.RolePermission;
import de.unipassau.allocationsystem.mapper.RolePermissionMapper;
import de.unipassau.allocationsystem.service.RolePermissionService;
import de.unipassau.allocationsystem.utils.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(
            summary = "Get sort fields",
            description = "Retrieves available fields that can be used for sorting role permissions"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sort fields retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        List<Map<String, String>> result = rolePermissionService.getSortFields();
        return ResponseHandler.success("Sort fields retrieved successfully", result);
    }

    @Operation(
            summary = "Get role permission by ID",
            description = "Retrieves a specific role permission by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Role permission found",
                    content = @Content(schema = @Schema(implementation = RolePermissionResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Role permission not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        RolePermissionResponseDto result = rolePermissionService.getById(id)
                .map(rolePermissionMapper::toResponseDto)
                .orElseThrow(() -> new NoSuchElementException("Role permission not found with id: " + id));
        return ResponseHandler.success("Role permission retrieved successfully", result);
    }

    @Operation(
            summary = "Get paginated role permissions",
            description = "Retrieves role permissions with pagination, sorting, and optional search"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role permissions retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        Map<String, Object> result = rolePermissionService.getPaginated(queryParams, searchValue);
        return ResponseHandler.success("Role permissions retrieved successfully (paginated)", result);
    }

    @Operation(
            summary = "Get all role permissions",
            description = "Retrieves all role permissions without pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Role permissions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = RolePermissionResponseDto.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("")
    public ResponseEntity<?> getAll(@RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations) {
        List<RolePermissionResponseDto> result = rolePermissionMapper.toResponseDtoList(rolePermissionService.getAll());
        return ResponseHandler.success("Role permissions retrieved successfully", result);
    }

    @Operation(
            summary = "Create new role permission",
            description = "Creates a new role permission with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Role permission created successfully",
                    content = @Content(schema = @Schema(implementation = RolePermissionResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate role permission"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody RolePermissionCreateDto dto) {
        try {
            RolePermission rolePermission = rolePermissionMapper.toEntityCreate(dto);
            RolePermission created = rolePermissionService.create(rolePermission);
            return ResponseHandler.created("Role permission created successfully", rolePermissionMapper.toResponseDto(created));
        } catch (DataIntegrityViolationException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @Operation(
            summary = "Update role permission",
            description = "Updates an existing role permission with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Role permission updated successfully",
                    content = @Content(schema = @Schema(implementation = RolePermissionResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate title"),
            @ApiResponse(responseCode = "404", description = "Role permission not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody RolePermissionUpdateDto dto) {
        try {
            RolePermission rolePermission = rolePermissionMapper.toEntityUpdate(dto);
            RolePermission updated = rolePermissionService.update(id, rolePermission);
            return ResponseHandler.updated("Role permission updated successfully", rolePermissionMapper.toResponseDto(updated));
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Role permission not found");
        } catch (DataIntegrityViolationException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @Operation(
            summary = "Delete role permission",
            description = "Deletes a role permission by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Role permission deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Role permission not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
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