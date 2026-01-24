package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintCreateDto;
import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintResponseDto;
import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintUpdateDto;
import de.unipassau.allocationsystem.entity.ZoneConstraint;
import de.unipassau.allocationsystem.mapper.BaseMapper;
import de.unipassau.allocationsystem.mapper.ZoneConstraintMapper;
import de.unipassau.allocationsystem.service.CrudService;
import de.unipassau.allocationsystem.service.ZoneConstraintService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;
import java.util.Map;

/**
 * REST controller for managing zone constraints.
 * Handles CRUD operations for zone-based internship type restrictions.
 */
@RestController
@RequestMapping("/zone-constraints")
@RequiredArgsConstructor
@Tag(name = "Zone Constraints", description = "Zone constraint management APIs")
public class ZoneConstraintController
        extends CrudControllerBase<ZoneConstraint, ZoneConstraintCreateDto, ZoneConstraintUpdateDto, ZoneConstraintResponseDto> {

    private final ZoneConstraintService zoneConstraintService;
    private final ZoneConstraintMapper zoneConstraintMapper;

    @Override
    protected CrudService<ZoneConstraint, Long> getService() {
        return zoneConstraintService;
    }

    @Override
    protected BaseMapper<ZoneConstraint, ZoneConstraintCreateDto, ZoneConstraintUpdateDto, ZoneConstraintResponseDto> getMapper() {
        return zoneConstraintMapper;
    }

    @GetMapping("")
    @Override
    public org.springframework.http.ResponseEntity<?> getAll() {
        var list = getMapper().toResponseDtoList(getService().getAll());
        return ok("Zone constraints retrieved successfully", list);
    }

    @GetMapping("/paginate")
    @Override
    public org.springframework.http.ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        return ok("Zone constraints retrieved successfully (paginated)", getService().getPaginated(queryParams, searchValue));
    }

    @GetMapping("/{id}")
    @Override
    public org.springframework.http.ResponseEntity<?> getById(@PathVariable Long id) {
        ZoneConstraint dto = getService().getById(id)
                .orElseThrow(() -> new de.unipassau.allocationsystem.exception.ResourceNotFoundException("ZoneConstraint not found with id: " + id));
        return ok("Zone constraint retrieved successfully", getMapper().toResponseDto(dto));
    }

    @PostMapping
    @Override
    public org.springframework.http.ResponseEntity<?> create(@Valid @RequestBody ZoneConstraintCreateDto dto) {
        ZoneConstraint created = getService().create(getMapper().toEntityCreate(dto));
        return created("Zone constraint created successfully", getMapper().toResponseDto(created));
    }

    @PutMapping("/{id}")
    @Override
    public org.springframework.http.ResponseEntity<?> update(@PathVariable Long id, @RequestBody ZoneConstraintUpdateDto dto) {
        ZoneConstraint updated = getService().update(id, getMapper().toEntityUpdate(dto));
        return updated("Zone constraint updated successfully", getMapper().toResponseDto(updated));
    }
}
