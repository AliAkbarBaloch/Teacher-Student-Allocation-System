package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.role.RoleCreateDto;
import de.unipassau.allocationsystem.dto.role.RoleResponseDto;
import de.unipassau.allocationsystem.dto.role.RoleUpdateDto;
import de.unipassau.allocationsystem.entity.Role;
import de.unipassau.allocationsystem.mapper.RoleMapper;
import de.unipassau.allocationsystem.service.RoleService;
import de.unipassau.allocationsystem.utils.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Role management APIs")
public class RoleController {

    private final RoleService roleService;
    private final RoleMapper roleMapper;

    @Operation(
            summary = "Get sort fields",
            description = "Retrieves available fields that can be used for sorting roles"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sort fields retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        List<Map<String, String>> result = roleService.getSortFields();
        return ResponseHandler.success("Sort fields retrieved successfully", result);
    }

    @Operation(
            summary = "Get role by ID",
            description = "Retrieves a specific role by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Role found",
                    content = @Content(schema = @Schema(implementation = RoleResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Role not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        RoleResponseDto result = roleService.getById(id)
                .map(roleMapper::toResponseDto)
                .orElseThrow(() -> new NoSuchElementException("Role not found with id: " + id));
        return ResponseHandler.success("Role retrieved successfully", result);
    }

    @Operation(
            summary = "Get paginated roles",
            description = "Retrieves roles with pagination, sorting, and optional search"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Roles retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        Map<String, Object> result = roleService.getPaginated(queryParams, searchValue);
        return ResponseHandler.success("Roles retrieved successfully (paginated)", result);
    }

    @Operation(
            summary = "Get all roles",
            description = "Retrieves all roles without pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Roles retrieved successfully",
                    content = @Content(schema = @Schema(implementation = RoleResponseDto.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("")
    public ResponseEntity<?> getAll(@RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations) {
        List<RoleResponseDto> result = roleMapper.toResponseDtoList(roleService.getAll());
        return ResponseHandler.success("Roles retrieved successfully", result);
    }

    @Operation(
            summary = "Create new role",
            description = "Creates a new role with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Role created successfully",
                    content = @Content(schema = @Schema(implementation = RoleResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate role"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody RoleCreateDto dto) {
        Role role = roleMapper.toEntityCreate(dto);
        Role created = roleService.create(role);
        return ResponseHandler.created("Role created successfully", roleMapper.toResponseDto(created));
    }

    @Operation(
            summary = "Update role",
            description = "Updates an existing role with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Role updated successfully",
                    content = @Content(schema = @Schema(implementation = RoleResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate title"),
            @ApiResponse(responseCode = "404", description = "Role not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody RoleUpdateDto dto) {
        Role role = roleMapper.toEntityUpdate(dto);
        Role updated = roleService.update(id, role);
        return ResponseHandler.updated("Role updated successfully", roleMapper.toResponseDto(updated));
    }

    @Operation(
            summary = "Delete role",
            description = "Deletes a role by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Role deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Role not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseHandler.noContent();
    }
}