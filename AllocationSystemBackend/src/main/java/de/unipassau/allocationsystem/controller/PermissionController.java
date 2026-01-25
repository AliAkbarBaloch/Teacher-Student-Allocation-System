package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.controller.docs.permission.CreatePermissionDocs;
import de.unipassau.allocationsystem.controller.docs.permission.DeletePermissionDocs;
import de.unipassau.allocationsystem.controller.docs.permission.GetAllPermissionsDocs;
import de.unipassau.allocationsystem.controller.docs.permission.GetPermissionByIdDocs;
import de.unipassau.allocationsystem.controller.docs.permission.GetPermissionPaginatedDocs;
import de.unipassau.allocationsystem.controller.docs.permission.GetPermissionSortFieldsDocs;
import de.unipassau.allocationsystem.controller.docs.permission.UpdatePermissionDocs;
import de.unipassau.allocationsystem.dto.permission.PermissionCreateDto;
import de.unipassau.allocationsystem.dto.permission.PermissionResponseDto;
import de.unipassau.allocationsystem.dto.permission.PermissionUpdateDto;
import de.unipassau.allocationsystem.entity.Permission;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.PermissionMapper;
import de.unipassau.allocationsystem.service.PermissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller for managing permissions.
 * Provides CRUD operations for permission entities used in role-based access control.
 */
@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@Tag(name = "Permissions", description = "Permission management APIs")
public class PermissionController extends ApiControllerSupport {

    private final PermissionService permissionService;
    private final PermissionMapper permissionMapper;

    /**
     * getSortFields: retrieves available fields for sorting permissions.
     * 
     * @return ResponseEntity containing list of sortable fields
     */
    @GetPermissionSortFieldsDocs
    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        return ok("Sort fields retrieved successfully", permissionService.getSortFields());
    }

    /**
     * getById: retrieves a specific permission by its ID.
     * 
     * @param id The ID of the permission
     * @return ResponseEntity containing the permission details
     * @throws ResourceNotFoundException if permission not found
     */
    @GetPermissionByIdDocs
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        PermissionResponseDto dto = permissionService.getById(id)
                .map(permissionMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + id));
        return ok("Permission retrieved successfully", dto);
    }

    /**
     * getPaginate: retrieves permissions with pagination, sorting, and optional search.
     * 
     * @param queryParams Map containing pagination parameters (page, size, sort)
     * @param includeRelations Flag to include related entities
     * @param searchValue Optional search term for filtering
     * @return ResponseEntity containing paginated permissions
     */
    @GetPermissionPaginatedDocs
    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        return ok("Permissions retrieved successfully (paginated)",
                  permissionService.getPaginated(queryParams, searchValue));
    }

    /**
     * getAll: retrieves all permissions without pagination.
     * 
     * @param includeRelations Flag to include related entities
     * @return ResponseEntity containing list of all permissions
     */
    @GetAllPermissionsDocs
    @GetMapping("")
    public ResponseEntity<?> getAll(@RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations) {
        List<PermissionResponseDto> list = permissionMapper.toResponseDtoList(permissionService.getAll());
        return ok("Permissions retrieved successfully", list);
    }

    /**
     * create: creates a new permission.
     * 
     * @param dto Permission creation data
     * @return ResponseEntity containing the created permission
     */
    @CreatePermissionDocs
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody PermissionCreateDto dto) {
        Permission createdEntity = permissionService.create(permissionMapper.toEntityCreate(dto));
        return created("Permission created successfully", permissionMapper.toResponseDto(createdEntity));
    }

    /**
     * update: updates an existing permission.
     * 
     * @param id The ID of the permission to update
     * @param dto Permission update data
     * @return ResponseEntity containing the updated permission
     */
    @UpdatePermissionDocs
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody PermissionUpdateDto dto) {
        Permission updatedEntity = permissionService.update(id, permissionMapper.toEntityUpdate(dto));
        return updated("Permission updated successfully", permissionMapper.toResponseDto(updatedEntity));
    }

    /**
     * delete: deletes a permission by its ID.
     * 
     * @param id The ID of the permission to delete
     * @return ResponseEntity with no content
     */
    @DeletePermissionDocs
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return noContent();
    }
}
