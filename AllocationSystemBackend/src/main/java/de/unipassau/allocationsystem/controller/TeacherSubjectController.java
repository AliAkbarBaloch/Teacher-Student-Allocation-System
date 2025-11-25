package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.teachersubject.TeacherSubjectCreateDto;
import de.unipassau.allocationsystem.dto.teachersubject.TeacherSubjectResponseDto;
import de.unipassau.allocationsystem.dto.teachersubject.TeacherSubjectUpdateDto;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.entity.Teacher;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/teacher-subjects")
@RequiredArgsConstructor
@Tag(name = "TeacherSubjects", description = "Teacher-subject management api")
public class TeacherSubjectController {

    private final TeacherSubjectService service;
    private final TeacherSubjectMapper mapper;

    @Operation(summary = "Get sort fields")
    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        List<Map<String, String>> fields = List.of(
                Map.of("key", "id", "label", "ID"),
                Map.of("key", "yearId", "label", "Year ID"),
                Map.of("key", "teacherId", "label", "Teacher ID"),
                Map.of("key", "subjectId", "label", "Subject ID"),
                Map.of("key", "availabilityStatus", "label", "Availability"),
                Map.of("key", "createdAt", "label", "Creation Date"),
                Map.of("key", "updatedAt", "label", "Last Updated")
        );
        return ResponseHandler.success("Sort fields retrieved successfully", fields);
    }

    @Operation(summary = "Get by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found",
                    content = @Content(schema = @Schema(implementation = TeacherSubjectResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            TeacherSubject entity = service.getById(id);
            TeacherSubjectResponseDto dto = mapper.toDto(entity);
            return ResponseHandler.success("Teacher-subject retrieved successfully", dto);
        } catch (ResourceNotFoundException e) {
            return ResponseHandler.notFound(e.getMessage());
        }
    }

    @Operation(summary = "Get paginated")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paginated list retrieved"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters")
    })
    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(@RequestParam Map<String, String> queryParams,
                                         @RequestParam(value = "searchValue", required = false) String searchValue) {
        // service.listByFilters accepts queryParams and an optional teacherId; pass teacherId if present in queryParams
        Long teacherId = queryParams.containsKey("teacherId") ? Long.valueOf(queryParams.get("teacherId")) : null;
        var page = service.listByFilters(queryParams, teacherId).map(mapper::toDto);
        Map<String, Object> result = PaginationUtils.formatPaginationResponse(page);
        return ResponseHandler.success("Teacher-subjects retrieved successfully (paginated)", result);
    }

    @Operation(summary = "Get all teacher-subjects")
    @GetMapping("")
    public ResponseEntity<?> getAll() {
        // Reuse pagination with default params and return the first page content as list
        var page = service.listByFilters(Map.of(), null).map(mapper::toDto);
        List<TeacherSubjectResponseDto> list = page.getContent().stream().collect(Collectors.toList());
        return ResponseHandler.success("Teacher-subjects retrieved successfully", list);
    }

    @Operation(summary = "Create new teacher-subject mapping")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate mapping"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("")
    public ResponseEntity<?> create(@Valid @RequestBody TeacherSubjectCreateDto dto) {
        try {
            TeacherSubject entity = new TeacherSubject();
            AcademicYear year = new AcademicYear();
            year.setId(dto.getAcademicYearId());
            entity.setAcademicYear(year);
            Teacher teacher = new Teacher();
            teacher.setId(dto.getTeacherId());
            entity.setTeacher(teacher);
            Subject subject = new Subject();
            subject.setId(dto.getSubjectId());
            entity.setSubject(subject);
            entity.setAvailabilityStatus(dto.getAvailabilityStatus());
            entity.setGradeLevelFrom(dto.getGradeLevelFrom());
            entity.setGradeLevelTo(dto.getGradeLevelTo());
            entity.setNotes(dto.getNotes());

            TeacherSubject created = service.create(entity);
            return ResponseHandler.created("Teacher-subject mapping created successfully", mapper.toDto(created));
        } catch (DuplicateResourceException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        } catch (ResourceNotFoundException e) {
            return ResponseHandler.notFound(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        } catch (Exception e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @Operation(summary = "Update teacher-subject")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody TeacherSubjectUpdateDto dto) {
        try {
            TeacherSubject update = new TeacherSubject();
            update.setAvailabilityStatus(dto.getAvailabilityStatus());
            update.setGradeLevelFrom(dto.getGradeLevelFrom());
            update.setGradeLevelTo(dto.getGradeLevelTo());
            update.setNotes(dto.getNotes());

            TeacherSubject updated = service.update(id, update);
            return ResponseHandler.updated("Teacher-subject mapping updated successfully", mapper.toDto(updated));
        } catch (ResourceNotFoundException e) {
            return ResponseHandler.notFound(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        } catch (Exception e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @Operation(summary = "Delete teacher-subject")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseHandler.noContent();
        } catch (ResourceNotFoundException e) {
            return ResponseHandler.notFound(e.getMessage());
        }
    }
}
