package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.teachersubject.TeacherSubjectCreateDto;
import de.unipassau.allocationsystem.dto.teachersubject.TeacherSubjectResponseDto;
import de.unipassau.allocationsystem.dto.teachersubject.TeacherSubjectUpdateDto;
import de.unipassau.allocationsystem.entity.TeacherSubject;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.TeacherSubjectMapper;
import de.unipassau.allocationsystem.service.TeacherSubjectService;
import de.unipassau.allocationsystem.utils.PaginationUtils;
import de.unipassau.allocationsystem.utils.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/teacher-subjects")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "TeacherSubjects", description = "Teacher-subject management APIs")
public class TeacherSubjectController {

    private final TeacherSubjectService service;
    private final TeacherSubjectMapper mapper;

    @Operation(
            summary = "Get sort fields",
            description = "Retrieves available fields that can be used for sorting teacher-subjects"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sort fields retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        log.info("Fetching teacher-subject sort fields");
        List<Map<String, String>> result = service.getSortFields();
        return ResponseHandler.success("Sort fields retrieved successfully", result);
    }

    @Operation(
            summary = "Get paginated teacher-subjects",
            description = "Retrieves teacher-subjects with pagination, sorting, and optional search"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teacher-subjects retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        log.info("Fetching paginated teacher-subjects with params: {}", queryParams);
        Map<String, Object> result = service.getPaginated(queryParams, searchValue);

        // Convert items to DTOs to avoid lazy loading serialization issues
        if (result.containsKey("items")) {
            List<TeacherSubject> items = (List<TeacherSubject>) result.get("items");
            List<TeacherSubjectResponseDto> dtoItems = mapper.toResponseDtoList(items);
            result.put("items", dtoItems);
        }

        return ResponseHandler.success("Teacher-subjects retrieved successfully (paginated)", result);
    }

    @Operation(
            summary = "Get all teacher-subjects",
            description = "Retrieves all teacher-subjects without pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Teacher-subjects retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TeacherSubjectResponseDto.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<?> getAll() {
        log.info("Fetching all teacher-subjects");
        List<TeacherSubjectResponseDto> result = mapper.toResponseDtoList(service.getAll());
        return ResponseHandler.success("Teacher-subjects retrieved successfully", result);
    }

    @Operation(
            summary = "Get teacher-subject by ID",
            description = "Retrieves a specific teacher-subject by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Teacher-subject found",
                    content = @Content(schema = @Schema(implementation = TeacherSubjectResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Teacher-subject not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        log.info("Fetching teacher-subject by id: {}", id);
        TeacherSubjectResponseDto result = service.getById(id)
                .map(mapper::toResponseDto)
                .orElseThrow(() -> new NoSuchElementException("Teacher-subject not found with id: " + id));
        return ResponseHandler.success("Teacher-subject retrieved successfully", result);
    }

    @Operation(
            summary = "Create new teacher-subject mapping",
            description = "Creates a new teacher-subject mapping with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Teacher-subject mapping created successfully",
                    content = @Content(schema = @Schema(implementation = TeacherSubjectResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate mapping"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody TeacherSubjectCreateDto dto) {
        log.info("Creating teacher-subject mapping with payload {}", dto);
        try {
            TeacherSubject entity = mapper.toEntityCreate(dto);
            TeacherSubject created = service.create(entity);
            return ResponseHandler.created("Teacher-subject mapping created successfully", mapper.toResponseDto(created));
        } catch (DuplicateResourceException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        } catch (ResourceNotFoundException e) {
            return ResponseHandler.notFound(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @Operation(
            summary = "Update teacher-subject mapping",
            description = "Updates an existing teacher-subject mapping with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Teacher-subject mapping updated successfully",
                    content = @Content(schema = @Schema(implementation = TeacherSubjectResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Teacher-subject not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody TeacherSubjectUpdateDto dto) {
        log.info("Updating teacher-subject {} with payload {}", id, dto);
        try {
            TeacherSubject update = mapper.toEntityUpdate(dto);
            TeacherSubject updated = service.update(id, update);
            return ResponseHandler.updated("Teacher-subject mapping updated successfully", mapper.toResponseDto(updated));
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Teacher-subject not found");
        } catch (DataIntegrityViolationException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @Operation(
            summary = "Delete teacher-subject mapping",
            description = "Deletes a teacher-subject mapping by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Teacher-subject mapping deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Teacher-subject not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        log.info("Deleting teacher-subject mapping {}", id);
        try {
            service.delete(id);
            return ResponseHandler.noContent();
        } catch (NoSuchElementException e) {
            return ResponseHandler.notFound("Teacher-subject not found");
        }
    }
}