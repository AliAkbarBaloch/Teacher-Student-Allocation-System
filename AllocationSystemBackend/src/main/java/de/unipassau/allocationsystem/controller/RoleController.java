package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.RoleDto;
import de.unipassau.allocationsystem.entity.Role;
import de.unipassau.allocationsystem.mapper.RoleMapper;
import de.unipassau.allocationsystem.service.RoleService;
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
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;
    private final RoleMapper roleMapper;


    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        List<Map<String, String>> result = roleService.getSortFields();
        return ResponseHandler.success("Sort fields retrieved successfully", result);
    }

    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        Map<String, Object> result = roleService.getPaginated(queryParams, includeRelations, searchValue);
        return ResponseHandler.success("Roles retrieved successfully (paginated)", result);
    }

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations) {
        List<RoleDto> result = roleMapper.toDtoList(roleService.getAll());
        return ResponseHandler.success("Roles retrieved successfully", result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        RoleDto result = roleService.getById(id)
                .map(roleMapper::toDto)
                .orElseThrow(() -> new NoSuchElementException("Role not found with id: " + id));
        return ResponseHandler.success("Role retrieved successfully", result);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody RoleDto dto) {
        try {
            Role role = roleMapper.toEntity(dto);
            Role created = roleService.create(role);
            return ResponseHandler.created("Role created successfully", roleMapper.toDto(created));
        } catch (DataIntegrityViolationException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody RoleDto dto) {
        try {
            Role role = roleMapper.toEntity(dto);
            Role updated = roleService.update(id, role);
            return ResponseHandler.updated("Role updated successfully", roleMapper.toDto(updated));
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Role not found");
        } catch (DataIntegrityViolationException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            roleService.delete(id);
            return ResponseHandler.noContent();
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Role not found");
        }
    }
}
