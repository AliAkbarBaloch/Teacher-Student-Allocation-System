package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.InternshipTypeDto;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.mapper.InternshipTypeMapper;
import de.unipassau.allocationsystem.service.InternshipTypeService;
import de.unipassau.allocationsystem.utils.ResponseHandler;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/internship-types")
@RequiredArgsConstructor
@Tag(name = "Internship Types", description = "Internship type management APIs")
public class InternshipTypeController {

    private final InternshipTypeService internshipTypeService;
    private final InternshipTypeMapper internshipTypeMapper;

    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        List<Map<String, String>> result = internshipTypeService.getSortFields();
        return ResponseHandler.success("Sort fields retrieved successfully", result);
    }

    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        Map<String, Object> result = internshipTypeService.getPaginated(queryParams, includeRelations, searchValue);
        return ResponseHandler.success("Internship types retrieved successfully (paginated)", result);
    }

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations) {
        List<InternshipTypeDto> result = internshipTypeMapper.toDtoList(internshipTypeService.getAll());
        return ResponseHandler.success("Internship types retrieved successfully", result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        InternshipTypeDto result = internshipTypeService.getById(id)
                .map(internshipTypeMapper::toDto)
                .orElseThrow(() -> new NoSuchElementException("InternshipType not found with id: " + id));
        return ResponseHandler.success("Internship type retrieved successfully", result);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody InternshipTypeDto dto) {
        try {
            InternshipType internshipType = internshipTypeMapper.toEntity(dto);
            InternshipType created = internshipTypeService.create(internshipType);
            return ResponseHandler.created("Internship type created successfully", internshipTypeMapper.toDto(created));
        } catch (DataIntegrityViolationException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody InternshipTypeDto dto) {
        try {
            InternshipType internshipType = internshipTypeMapper.toEntity(dto);
            InternshipType updated = internshipTypeService.update(id, internshipType);
            return ResponseHandler.updated("Internship type updated successfully", internshipTypeMapper.toDto(updated));
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("InternshipType not found");
        } catch (DataIntegrityViolationException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            internshipTypeService.delete(id);
            return ResponseHandler.noContent();
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("InternshipType not found");
        }
    }
}
