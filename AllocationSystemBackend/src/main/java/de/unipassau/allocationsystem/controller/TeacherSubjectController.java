package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.teachersubject.TeacherSubjectCreateDto;
import de.unipassau.allocationsystem.dto.teachersubject.TeacherSubjectFilterDto;
import de.unipassau.allocationsystem.dto.teachersubject.TeacherSubjectResponseDto;
import de.unipassau.allocationsystem.dto.teachersubject.TeacherSubjectUpdateDto;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.TeacherSubject;
import de.unipassau.allocationsystem.mapper.TeacherSubjectMapper;
import de.unipassau.allocationsystem.service.TeacherSubjectService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/teachers-subjects")
@RequiredArgsConstructor
@Tag(name = "TeacherSubjects", description = "Manage teacher-subject mappings")
public class TeacherSubjectController {

    private final TeacherSubjectService service;
    private final TeacherSubjectMapper mapper;

        @Operation(summary = "List subject mappings for a teacher")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teacher-subject mappings retrieved",
                content = @Content(schema = @Schema(implementation = TeacherSubjectResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        @GetMapping("/{teacherId}/subjects")
    public ResponseEntity<?> listForTeacher(@PathVariable Long teacherId,
                                            @ModelAttribute TeacherSubjectFilterDto filter) {
        // ensure teacher id is present in filter
        filter.setTeacherId(teacherId);
        Map<String, String> qp = Map.of();
        // convert filter to query params map if needed; reuse service method expecting map
        // For simplicity, call service with empty query params and teacherId
        var page = service.listByFilters(qp, teacherId);
        return ResponseHandler.success("Teacher-subject mappings retrieved", page.map(mapper::toDto));
    }

        @Operation(summary = "Create teacher-subject mapping")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Teacher-subject mapping created",
                content = @Content(schema = @Schema(implementation = TeacherSubjectResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate mapping"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        @PostMapping("/{teacherId}/subjects")
        @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@PathVariable Long teacherId, @Valid @RequestBody TeacherSubjectCreateDto dto) {
        try {
                TeacherSubject entity = new TeacherSubject();
                Teacher teacher = new Teacher();
                teacher.setId(teacherId);
                entity.setTeacher(teacher);
                AcademicYear year = new AcademicYear();
                year.setId(dto.getYearId());
                entity.setAcademicYear(year);
                Subject subject = new Subject();
                subject.setId(dto.getSubjectId());
                entity.setSubject(subject);
                entity.setAvailabilityStatus(dto.getAvailabilityStatus());
                entity.setGradeLevelFrom(dto.getGradeLevelFrom());
                entity.setGradeLevelTo(dto.getGradeLevelTo());
                entity.setNotes(dto.getNotes());
            TeacherSubject created = service.create(entity);
            return ResponseHandler.created("Teacher-subject mapping created", mapper.toDto(created));
        } catch (Exception e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

        @Operation(summary = "Update teacher-subject mapping")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teacher-subject mapping updated",
                content = @Content(schema = @Schema(implementation = TeacherSubjectResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Teacher-subject mapping not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        @PutMapping("/{teacherId}/subjects/{id}")
        @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long teacherId, @PathVariable Long id, @RequestBody TeacherSubjectUpdateDto dto) {
        try {
            TeacherSubject update = new TeacherSubject();
            update.setAvailabilityStatus(dto.getAvailabilityStatus());
            update.setGradeLevelFrom(dto.getGradeLevelFrom());
            update.setGradeLevelTo(dto.getGradeLevelTo());
            update.setNotes(dto.getNotes());
            TeacherSubject updated = service.update(id, update);
            return ResponseHandler.updated("Teacher-subject mapping updated", mapper.toDto(updated));
        } catch (Exception e) {
            return ResponseHandler.badRequest(e.getMessage(), Map.of());
        }
    }

    @Operation(summary = "Delete teacher-subject mapping")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Teacher-subject mapping deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Teacher-subject mapping not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{teacherId}/subjects/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long teacherId, @PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseHandler.noContent();
        } catch (Exception e) {
            return ResponseHandler.notFound(e.getMessage());
        }
    }
}
