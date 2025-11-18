package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.permission.PermissionCreateDto;
import de.unipassau.allocationsystem.dto.permission.PermissionResponseDto;
import de.unipassau.allocationsystem.dto.permission.PermissionUpdateDto;
import de.unipassau.allocationsystem.entity.Permission;
import de.unipassau.allocationsystem.mapper.PermissionMapper;
import de.unipassau.allocationsystem.service.PermissionService;
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
@RequestMapping("/permissions")
@RequiredArgsConstructor
@Tag(name = "Permissions", description = "Permission management APIs")
public class PermissionController {

    private final PermissionService permissionService;
    private final PermissionMapper permissionMapper;

    @Operation(
            summary = "Get sort fields",
            description = "Retrieves available fields that can be used for sorting permissions"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sort fields retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        List<Map<String, String>> result = permissionService.getSortFields();
        return ResponseHandler.success("Sort fields retrieved successfully", result);
    }

    @Operation(
            summary = "Get paginated permissions",
            description = "Retrieves permissions with pagination, sorting, and optional search"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Permissions retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        Map<String, Object> result = permissionService.getPaginated(queryParams, searchValue);
        return ResponseHandler.success("Permissions retrieved successfully (paginated)", result);
    }

    @Operation(
            summary = "Get all permissions",
            description = "Retrieves all permissions without pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Permissions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PermissionResponseDto.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations) {
        List<PermissionResponseDto> result = permissionMapper.toResponseDtoList(permissionService.getAll());
        return ResponseHandler.success("Permissions retrieved successfully", result);
    }

    @Operation(
            summary = "Get permission by ID",
            description = "Retrieves a specific permission by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Permission found",
                    content = @Content(schema = @Schema(implementation = PermissionResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Permission not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        PermissionResponseDto result = permissionService.getById(id)
                .map(permissionMapper::toResponseDto)
                .orElseThrow(() -> new NoSuchElementException("Permission not found with id: " + id));
        return ResponseHandler.success("Permission retrieved successfully", result);
    }

    @Operation(
            summary = "Create new permission",
            description = "Creates a new permission with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Permission created successfully",
                    content = @Content(schema = @Schema(implementation = PermissionResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate permission"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody PermissionCreateDto dto) {
        try {
            Permission permission = permissionMapper.toEntityCreate(dto);
            Permission created = permissionService.create(permission);
            return ResponseHandler.created("Permission created successfully", permissionMapper.toResponseDto(created));
        } catch (DataIntegrityViolationException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @Operation(
            summary = "Update permission",
            description = "Updates an existing permission with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Permission updated successfully",
                    content = @Content(schema = @Schema(implementation = PermissionResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate title"),
            @ApiResponse(responseCode = "404", description = "Permission not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
     @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody PermissionUpdateDto dto) {
        try {
            Permission permission = permissionMapper.toEntityUpdate(dto);
            Permission updated = permissionService.update(id, permission);
            return ResponseHandler.updated("Permission updated successfully", permissionMapper.toResponseDto(updated));
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Permission not found");
        } catch (DataIntegrityViolationException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @Operation(
            summary = "Delete permission",
            description = "Deletes a permission by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Permission deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Permission not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            permissionService.delete(id);
            return ResponseHandler.noContent();
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Permission not found");
        }
    }
}
