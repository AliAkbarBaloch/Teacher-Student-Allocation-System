package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.controller.docs.CreateDocs;
import de.unipassau.allocationsystem.controller.docs.DeleteDocs;
import de.unipassau.allocationsystem.controller.docs.GetAllDocs;
import de.unipassau.allocationsystem.controller.docs.GetByIdDocs;
import de.unipassau.allocationsystem.controller.docs.GetPaginatedDocs;
import de.unipassau.allocationsystem.controller.docs.GetSortFieldsDocs;
import de.unipassau.allocationsystem.controller.docs.UpdateDocs;
import de.unipassau.allocationsystem.dto.internshiptype.InternshipTypeCreateDto;
import de.unipassau.allocationsystem.dto.internshiptype.InternshipTypeResponseDto;
import de.unipassau.allocationsystem.dto.internshiptype.InternshipTypeUpdateDto;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.InternshipTypeMapper;
import de.unipassau.allocationsystem.service.InternshipTypeService;
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
 * REST controller for managing internship types.
 * Provides CRUD operations for internship type entities.
 */
@RestController
@RequestMapping("/internship-types")
@RequiredArgsConstructor
@Tag(name = "Internship Types", description = "Internship type management APIs")
public class InternshipTypeController extends ApiControllerSupport {

    private final InternshipTypeService internshipTypeService;
    private final InternshipTypeMapper internshipTypeMapper;

    /**
     * getSortFields: retrieves available fields for sorting internship types.
     * 
     * @return ResponseEntity containing list of sortable fields
     */
    @GetSortFieldsDocs
    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        return ok("Sort fields retrieved successfully", internshipTypeService.getSortFields());
    }

    /**
     * getById: retrieves a specific internship type by its ID.
     * 
     * @param id The ID of the internship type
     * @return ResponseEntity containing the internship type details
     * @throws ResourceNotFoundException if internship type not found
     */
    @GetByIdDocs
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        InternshipTypeResponseDto dto = internshipTypeService.getById(id)
                .map(internshipTypeMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("InternshipType not found with id: " + id));
        return ok("Internship type retrieved successfully", dto);
    }

    /**
     * getPaginate: retrieves internship types with pagination and optional search.
     * 
     * @param queryParams Map containing pagination parameters (page, size, sort)
     * @param searchValue Optional search term for filtering
     * @return ResponseEntity containing paginated internship types
     */
    @GetPaginatedDocs
    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        return ok("Internship types retrieved successfully (paginated)",
                  internshipTypeService.getPaginated(queryParams, searchValue));
    }

    /**
     * getAll: retrieves all internship types without pagination.
     * 
     * @return ResponseEntity containing list of all internship types
     */
    @GetAllDocs
    @GetMapping("")
    public ResponseEntity<?> getAll() {
        List<InternshipTypeResponseDto> list = internshipTypeMapper.toResponseDtoList(internshipTypeService.getAll());
        return ok("Internship types retrieved successfully", list);
    }

    /**
     * create: creates a new internship type.
     * 
     * @param dto Internship type creation data
     * @return ResponseEntity containing the created internship type
     */
    @CreateDocs
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody InternshipTypeCreateDto dto) {
        InternshipType createdEntity = internshipTypeService.create(internshipTypeMapper.toEntityCreate(dto));
        return created("Internship type created successfully", internshipTypeMapper.toResponseDto(createdEntity));
    }

    /**
     * update: updates an existing internship type.
     * 
     * @param id The ID of the internship type to update
     * @param dto Internship type update data
     * @return ResponseEntity containing the updated internship type
     */
    @UpdateDocs
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody InternshipTypeUpdateDto dto) {
        InternshipType updatedEntity = internshipTypeService.update(id, internshipTypeMapper.toEntityUpdate(dto));
        return updated("Internship type updated successfully", internshipTypeMapper.toResponseDto(updatedEntity));
    }

    /**
     * delete: deletes an internship type by its ID.
     * 
     * @param id The ID of the internship type to delete
     * @return ResponseEntity with no content
     */
    @DeleteDocs
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        internshipTypeService.delete(id);
        return noContent();
    }
}