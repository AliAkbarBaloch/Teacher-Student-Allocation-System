package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.SubjectCategoryDto;
import de.unipassau.allocationsystem.entity.SubjectCategory;
import de.unipassau.allocationsystem.mapper.SubjectCategoryMapper;
import de.unipassau.allocationsystem.service.SubjectCategoryService;
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
@RequestMapping("/subject-categories")
@RequiredArgsConstructor
@Tag(name = "SubjectCategories", description = "Subject Category management APIs")
public class SubjectCategoryController {

    private final SubjectCategoryService subjectCategoryService;
    private final SubjectCategoryMapper subjectCategoryMapper;

    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        List<Map<String, String>> result = subjectCategoryService.getSortFields();
        return ResponseHandler.success("Sort fields retrieved successfully", result);
    }

    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        Map<String, Object> result = subjectCategoryService.getPaginated(queryParams, includeRelations, searchValue);
        return ResponseHandler.success("Subject categories retrieved successfully (paginated)", result);
    }

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations) {
        List<SubjectCategoryDto> result = subjectCategoryMapper.toDtoList(subjectCategoryService.getAll());
        return ResponseHandler.success("Subject categories retrieved successfully", result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        SubjectCategoryDto result = subjectCategoryService.getById(id)
                .map(subjectCategoryMapper::toDto)
                .orElseThrow(() -> new NoSuchElementException("Subject category not found with id: " + id));
        return ResponseHandler.success("Subject category retrieved successfully", result);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SubjectCategoryDto dto) {
        try {
            SubjectCategory subjectCategory = subjectCategoryMapper.toEntity(dto);
            SubjectCategory created = subjectCategoryService.create(subjectCategory);
            return ResponseHandler.created("Subject category created successfully", subjectCategoryMapper.toDto(created));
        } catch (DataIntegrityViolationException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody SubjectCategoryDto dto) {
        try {
            SubjectCategory subjectCategory = subjectCategoryMapper.toEntity(dto);
            SubjectCategory updated = subjectCategoryService.update(id, subjectCategory);
            return ResponseHandler.updated("Subject category updated successfully", subjectCategoryMapper.toDto(updated));
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Subject category not found");
        } catch (DataIntegrityViolationException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            subjectCategoryService.delete(id);
            return ResponseHandler.noContent();
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Subject category not found");
        }
    }
}

