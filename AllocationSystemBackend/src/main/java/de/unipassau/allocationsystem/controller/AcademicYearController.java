package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.AcademicYearDto;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.mapper.AcademicYearMapper;
import de.unipassau.allocationsystem.service.AcademicYearService;
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
@RequestMapping("/academic-years")
@RequiredArgsConstructor
@Tag(name = "AcademicYears", description = "Academic Year management APIs")
public class AcademicYearController {

    private final AcademicYearService academicYearService;
    private final AcademicYearMapper academicYearMapper;

    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        List<Map<String, String>> result = academicYearService.getSortFields();
        return ResponseHandler.success("Sort fields retrieved successfully", result);
    }

    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        Map<String, Object> result = academicYearService.getPaginated(queryParams, includeRelations, searchValue);
        return ResponseHandler.success("Academic years retrieved successfully (paginated)", result);
    }

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations) {
        List<AcademicYearDto> result = academicYearMapper.toDtoList(academicYearService.getAll());
        return ResponseHandler.success("Academic years retrieved successfully", result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        AcademicYearDto result = academicYearService.getById(id)
                .map(academicYearMapper::toDto)
                .orElseThrow(() -> new NoSuchElementException("Academic year not found with id: " + id));
        return ResponseHandler.success("Academic year retrieved successfully", result);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody AcademicYearDto dto) {
        try {
            AcademicYear academicYear = academicYearMapper.toEntity(dto);
            AcademicYear created = academicYearService.create(academicYear);
            return ResponseHandler.created("Academic year created successfully", academicYearMapper.toDto(created));
        } catch (DataIntegrityViolationException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody AcademicYearDto dto) {
        try {
            AcademicYear academicYear = academicYearMapper.toEntity(dto);
            AcademicYear updated = academicYearService.update(id, academicYear);
            return ResponseHandler.updated("Academic year updated successfully", academicYearMapper.toDto(updated));
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Academic year not found");
        } catch (DataIntegrityViolationException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            academicYearService.delete(id);
            return ResponseHandler.noContent();
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Academic year not found");
        }
    }
}
