package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.controller.docs.CreateDocs;
import de.unipassau.allocationsystem.controller.docs.DeleteDocs;
import de.unipassau.allocationsystem.controller.docs.GetAllDocs;
import de.unipassau.allocationsystem.controller.docs.GetByIdDocs;
import de.unipassau.allocationsystem.controller.docs.GetPaginatedDocs;
import de.unipassau.allocationsystem.controller.docs.GetSortFieldsDocs;
import de.unipassau.allocationsystem.controller.docs.UpdateDocs;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.BaseMapper;
import de.unipassau.allocationsystem.service.CrudService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Generic base controller providing common CRUD operations for all REST API endpoints.
 * Eliminates code duplication by:
 * - Providing default implementations for all 7 CRUD operations
 * - Including Spring @*Mapping annotations on methods
 * - Including OpenAPI documentation annotations
 * 
 * Subclasses only need to:
 * 1. Extend this class with 4 type parameters: Entity, CreateDto, UpdateDto, ResponseDto
 * 2. Implement getService() and getMapper() for dependency injection
 * 3. Add @RestController, @RequestMapping, @RequiredArgsConstructor, and @Tag annotations
 * 
 * Example:
 * <pre>
 * @RestController
 * @RequestMapping("/academic-years")
 * @RequiredArgsConstructor
 * @Tag(name = "Academic Years", description = "Academic year management APIs")
 * public class AcademicYearController extends CrudControllerBase<AcademicYear, AcademicYearCreateDto,
 *                                                                 AcademicYearUpdateDto, AcademicYearResponseDto> {
 *     private final AcademicYearService service;
 *     private final AcademicYearMapper mapper;
 *     
 *     protected CrudService<AcademicYear, Long> getService() {
 *         return service;
 *     }
 *     
 *     protected BaseMapper<AcademicYear, AcademicYearCreateDto, AcademicYearUpdateDto, AcademicYearResponseDto> getMapper() {
 *         return mapper;
 *     }
 * }
 * </pre>
 * 
 * @param <Entity> The entity type managed by this controller
 * @param <CreateDto> The DTO type for entity creation
 * @param <UpdateDto> The DTO type for entity updates
 * @param <ResponseDto> The DTO type for entity responses
 */
public abstract class CrudControllerBase<Entity, CreateDto, UpdateDto, ResponseDto> extends ApiControllerSupport {

    /**
     * Get the service instance for CRUD operations.
     * Must be implemented by subclasses.
     */
    protected abstract CrudService<Entity, Long> getService();

    /**
     * Get the mapper instance for DTO conversions.
     * Must be implemented by subclasses.
     */
    protected abstract BaseMapper<Entity, CreateDto, UpdateDto, ResponseDto> getMapper();

    /**
     * Get available sort fields for the entity.
     */
    @GetSortFieldsDocs
    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        return ok("Sort fields retrieved successfully", getService().getSortFields());
    }

    /**
     * Get entity by ID.
     */
    @GetByIdDocs
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        ResponseDto dto = getService().getById(id)
                .map(getMapper()::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Entity not found with id: " + id));
        return ok("Entity retrieved successfully", dto);
    }

    /**
     * Get paginated entities.
     */
    @GetPaginatedDocs
    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        return ok("Entities retrieved successfully (paginated)",
                  getService().getPaginated(queryParams, searchValue));
    }

    /**
     * Get all entities.
     */
    @GetAllDocs
    @GetMapping("")
    public ResponseEntity<?> getAll() {
        List<ResponseDto> list = getMapper().toResponseDtoList(getService().getAll());
        return ok("Entities retrieved successfully", list);
    }

    /**
     * Create new entity.
     */
    @CreateDocs
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateDto dto) {
        Entity createdEntity = getService().create(getMapper().toEntityCreate(dto));
        return created("Entity created successfully", getMapper().toResponseDto(createdEntity));
    }

    /**
     * Update existing entity.
     */
    @UpdateDocs
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody UpdateDto dto) {
        Entity updatedEntity = getService().update(id, getMapper().toEntityUpdate(dto));
        return updated("Entity updated successfully", getMapper().toResponseDto(updatedEntity));
    }

    /**
     * Delete entity.
     */
    @DeleteDocs
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        getService().delete(id);
        return noContent();
    }
}
