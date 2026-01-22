package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.controller.docs.CreateDocs;
import de.unipassau.allocationsystem.controller.docs.DeleteDocs;
import de.unipassau.allocationsystem.controller.docs.GetAllDocs;
import de.unipassau.allocationsystem.controller.docs.GetByIdDocs;
import de.unipassau.allocationsystem.controller.docs.GetPaginatedDocs;
import de.unipassau.allocationsystem.controller.docs.GetSortFieldsDocs;
import de.unipassau.allocationsystem.controller.docs.UpdateDocs;
import de.unipassau.allocationsystem.dto.academicyear.AcademicYearCreateDto;
import de.unipassau.allocationsystem.dto.academicyear.AcademicYearResponseDto;
import de.unipassau.allocationsystem.dto.academicyear.AcademicYearUpdateDto;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.AcademicYearMapper;
import de.unipassau.allocationsystem.service.AcademicYearService;
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
 * REST controller for managing academic years.
 * Provides CRUD operations and listing functionality for academic year entities.
 */
@RestController
@RequestMapping("/academic-years")
@RequiredArgsConstructor
@Tag(name = "AcademicYears", description = "Academic Year management APIs")
public class AcademicYearController extends ApiControllerSupport {

    private final AcademicYearService academicYearService;
    private final AcademicYearMapper academicYearMapper;

    /**
     * getSortFields: retrieves available fields for sorting academic years.
     * 
     * @return ResponseEntity containing list of sortable fields
     */
    @GetSortFieldsDocs
    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        return ok("Sort fields retrieved successfully", academicYearService.getSortFields());
    }

    /**
     * getById: retrieves a specific academic year by its ID.
     * 
     * @param id The ID of the academic year
     * @return ResponseEntity containing the academic year details
     * @throws ResourceNotFoundException if academic year not found
     */
    @GetByIdDocs
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        AcademicYearResponseDto dto = academicYearService.getById(id)
                .map(academicYearMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + id));
        return ok("Academic year retrieved successfully", dto);
    }

    /**
     * getPaginate: retrieves academic years with pagination, sorting, and optional search.
     * 
     * @param queryParams Map containing pagination parameters (page, size, sort)
     * @param searchValue Optional search term for filtering
     * @return ResponseEntity containing paginated academic years
     */
    @GetPaginatedDocs
    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        return ok("Academic years retrieved successfully (paginated)",
                  academicYearService.getPaginated(queryParams, searchValue));
    }

    /**
     * getAll: retrieves all academic years without pagination.
     * 
     * @return ResponseEntity containing list of all academic years
     */
    @GetAllDocs
    @GetMapping("")
    public ResponseEntity<?> getAll() {
        List<AcademicYearResponseDto> list = academicYearMapper.toResponseDtoList(academicYearService.getAll());
        return ok("Academic years retrieved successfully", list);
    }

    /**
     * create: creates a new academic year.
     * 
     * @param dto Academic year creation data
     * @return ResponseEntity containing the created academic year
     */
    @CreateDocs
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody AcademicYearCreateDto dto) {
        AcademicYear createdEntity = academicYearService.create(academicYearMapper.toEntityCreate(dto));
        return created("Academic year created successfully", academicYearMapper.toResponseDto(createdEntity));
    }

    /**
     * update: updates an existing academic year.
     * 
     * @param id The ID of the academic year to update
     * @param dto Academic year update data
     * @return ResponseEntity containing the updated academic year
     */
    @UpdateDocs
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody AcademicYearUpdateDto dto) {
        AcademicYear updatedEntity = academicYearService.update(id, academicYearMapper.toEntityUpdate(dto));
        return updated("Academic year updated successfully", academicYearMapper.toResponseDto(updatedEntity));
    }

    /**
     * delete: deletes an academic year by its ID.
     * 
     * @param id The ID of the academic year to delete
     * @return ResponseEntity with no content
     */
    @DeleteDocs
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        academicYearService.delete(id);
        return noContent();
    }
}