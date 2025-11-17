package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.SubjectCategoryDto;
import de.unipassau.allocationsystem.entity.SubjectCategory;
import de.unipassau.allocationsystem.mapper.SubjectCategoryMapper;
import de.unipassau.allocationsystem.service.SubjectCategoryService;
import de.unipassau.allocationsystem.utils.ResponseHandler;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class SubjectCategoryController {

    private final SubjectCategoryService subjectCategoryService;
    private final SubjectCategoryMapper subjectCategoryMapper;

    /**
     * Exposes the allowed sort keys and their labels so the frontend
     * can build dropdowns without hardcoding backend field names.
     */
    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        log.info("Fetching subject category sort fields");
        List<Map<String, String>> result = subjectCategoryService.getSortFields();
        return ResponseHandler.success("Sort fields retrieved successfully", result);
    }

    /**
     * Returns a paginated subset of subject categories, optionally
     * filtered by a case-insensitive search value and sorted by the
     * requested field/order combination.
     */
    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        log.info("Fetching paginated subject categories with params: {}", queryParams);
        Map<String, Object> result = subjectCategoryService.getPaginated(queryParams, includeRelations, searchValue);
        return ResponseHandler.success("Subject categories retrieved successfully (paginated)", result);
    }

    /**
     * Fetches all subject categories without pagination. Useful for
     * populating dropdowns where the total item count is small.
     */
    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations) {
        log.info("Fetching all subject categories (includeRelations={})", includeRelations);
        List<SubjectCategoryDto> result = subjectCategoryMapper.toDtoList(subjectCategoryService.getAll());
        return ResponseHandler.success("Subject categories retrieved successfully", result);
    }

    /**
     * Retrieves a single subject category by id or returns 404 if the
     * requested entity is not present in the database.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        log.info("Fetching subject category with id {}", id);
        SubjectCategoryDto result = subjectCategoryService.getById(id)
                .map(subjectCategoryMapper::toDto)
                .orElseThrow(() -> new NoSuchElementException("Subject category not found with id: " + id));
        return ResponseHandler.success("Subject category retrieved successfully", result);
    }

    /**
     * Creates a new subject category after validating the request body,
     * handling duplicate titles via a 400 response instead of a 500.
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SubjectCategoryDto dto) {
        try {
            log.info("Creating subject category with payload {}", dto);
            SubjectCategory subjectCategory = subjectCategoryMapper.toEntity(dto);
            SubjectCategory created = subjectCategoryService.create(subjectCategory);
            return ResponseHandler.created("Subject category created successfully", subjectCategoryMapper.toDto(created));
        } catch (DataIntegrityViolationException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    /**
     * Updates the category title of an existing subject category. When
     * the target id does not exist a 404 is returned; duplicate titles
     * yield a 400 with the repository error message.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody SubjectCategoryDto dto) {
        try {
            log.info("Updating subject category {} with payload {}", id, dto);
            SubjectCategory subjectCategory = subjectCategoryMapper.toEntity(dto);
            SubjectCategory updated = subjectCategoryService.update(id, subjectCategory);
            return ResponseHandler.updated("Subject category updated successfully", subjectCategoryMapper.toDto(updated));
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Subject category not found");
        } catch (DataIntegrityViolationException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    /**
     * Deletes a subject category by id, surfacing a 404 response when
     * the entity has already been removed or never existed.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            log.info("Deleting subject category {}", id);
            subjectCategoryService.delete(id);
            return ResponseHandler.noContent();
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Subject category not found");
        }
    }
}

