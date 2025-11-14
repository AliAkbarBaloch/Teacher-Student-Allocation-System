package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.PermissionDto;
import de.unipassau.allocationsystem.entity.Permission;
import de.unipassau.allocationsystem.mapper.PermissionMapper;
import de.unipassau.allocationsystem.service.PermissionService;
import de.unipassau.allocationsystem.utils.ResponseHandler;
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
public class PermissionController {

    private final PermissionService permissionService;
    private final PermissionMapper permissionMapper;


    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        List<Map<String, String>> result = permissionService.getSortFields();
        return ResponseHandler.success("Sort fields retrieved successfully", result);
    }

    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        Map<String, Object> result = permissionService.getPaginated(queryParams, includeRelations, searchValue);
        return ResponseHandler.success("Permissions retrieved successfully (paginated)", result);
    }

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations) {
        List<PermissionDto> result = permissionMapper.toDtoList(permissionService.getAll());
        return ResponseHandler.success("Permissions retrieved successfully", result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        PermissionDto result = permissionService.getById(id)
                .map(permissionMapper::toDto)
                .orElseThrow(() -> new NoSuchElementException("Permission not found with id: " + id));
        return ResponseHandler.success("Permission retrieved successfully", result);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody PermissionDto dto) {
        try {
            Permission permission = permissionMapper.toEntity(dto);
            Permission created = permissionService.create(permission);
            return ResponseHandler.created("Permission created successfully", permissionMapper.toDto(created));
        } catch (DataIntegrityViolationException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

     @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody PermissionDto dto) {
        try {
            Permission permission = permissionMapper.toEntity(dto);
            Permission updated = permissionService.update(id, permission);
            return ResponseHandler.updated("Permission updated successfully", permissionMapper.toDto(updated));
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Permission not found");
        } catch (DataIntegrityViolationException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

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
