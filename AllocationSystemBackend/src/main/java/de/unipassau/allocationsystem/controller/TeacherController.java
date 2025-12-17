package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.teacher.TeacherCreateDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherResponseDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherStatusUpdateDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherUpdateDto;
import de.unipassau.allocationsystem.dto.teacher.BulkImportResponseDto;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.mapper.TeacherMapper;
import de.unipassau.allocationsystem.service.TeacherService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

@RestController
@RequestMapping("/teachers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Teachers", description = "Teacher management APIs")
public class TeacherController {

    private final TeacherService teacherService;
    private final TeacherMapper teacherMapper;

    @Operation(
            summary = "Get sort fields",
            description = "Retrieves available fields that can be used for sorting teachers"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sort fields retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        List<Map<String, String>> result = teacherService.getSortFields();
        return ResponseHandler.success("Sort fields retrieved successfully", result);
    }

    @Operation(
            summary = "Get teacher by ID",
            description = "Retrieves a specific teacher by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Teacher found",
                    content = @Content(schema = @Schema(implementation = TeacherResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "Teacher not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        TeacherResponseDto result = teacherService.getById(id)
                .orElseThrow(() -> new NoSuchElementException("Teacher not found with id: " + id));
        return ResponseHandler.success("Teacher retrieved successfully", result);
    }

    @Operation(
            summary = "Get paginated teachers",
            description = "Retrieves teachers with pagination, sorting, and optional search"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Teachers retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        Map<String, Object> result = teacherService.getPaginated(queryParams, searchValue);
        
        // Map Teacher entities to DTOs
        @SuppressWarnings("unchecked")
        List<Teacher> teachers = (List<Teacher>) result.get("items");
        if (teachers != null) {
            List<TeacherResponseDto> teacherDtos = teacherMapper.toResponseDtoList(teachers);
            result.put("items", teacherDtos);
        }
        
        return ResponseHandler.success("Teachers retrieved successfully (paginated)", result);
    }

    @Operation(
            summary = "Get all teachers",
            description = "Retrieves all teachers without pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Teachers retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TeacherResponseDto.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("")
    public ResponseEntity<?> getAll(@RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations) {
        List<TeacherResponseDto> result = teacherService.getAll();
        return ResponseHandler.success("Teachers retrieved successfully", result);
    }

    @Operation(
            summary = "Create new teacher",
            description = "Creates a new teacher with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Teacher created successfully",
                    content = @Content(schema = @Schema(implementation = TeacherResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate teacher"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody TeacherCreateDto dto) {
        TeacherResponseDto created = teacherService.createTeacher(dto);
        return ResponseHandler.created("Teacher created successfully", created);
    }

    @Operation(
            summary = "Update teacher",
            description = "Updates an existing teacher with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Teacher updated successfully",
                    content = @Content(schema = @Schema(implementation = TeacherResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate email"),
            @ApiResponse(responseCode = "404", description = "Teacher not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody TeacherUpdateDto dto) {
        TeacherResponseDto updated = teacherService.updateTeacher(id, dto);
        return ResponseHandler.updated("Teacher updated successfully", updated);
    }

    @Operation(
            summary = "Update teacher status",
            description = "Updates the active status of a teacher (activate/deactivate)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Teacher status updated successfully",
                    content = @Content(schema = @Schema(implementation = TeacherResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Teacher not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody TeacherStatusUpdateDto statusDto) {
        TeacherResponseDto updated = teacherService.updateEmploymentStatus(id, statusDto.getEmploymentStatus());
        return ResponseHandler.updated("Teacher status updated successfully", updated);
    }

    @Operation(
            summary = "Delete teacher",
            description = "Deletes a teacher by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Teacher deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Teacher not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return ResponseHandler.noContent();
    }

    @Operation(
            summary = "Check existing emails",
            description = "Checks which email addresses already exist in the database. Used for bulk import validation."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Email check completed",
                    content = @Content(schema = @Schema(implementation = Set.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/check-emails")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> checkExistingEmails(@RequestBody List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return ResponseHandler.success("No emails to check", Set.of());
        }
        Set<String> existingEmails = teacherService.findExistingEmails(emails);
        return ResponseHandler.success("Email check completed", existingEmails);
    }

    @Operation(
            summary = "Bulk import teachers from Excel",
            description = "Imports multiple teachers from an Excel file. Requires ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Bulk import completed",
                    content = @Content(schema = @Schema(implementation = BulkImportResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid file format or data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN role required"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/bulk-import")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> bulkImport(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "skipInvalidRows", defaultValue = "false") boolean skipInvalidRows
    ) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                && !contentType.equals("application/vnd.ms-excel"))) {
            throw new IllegalArgumentException("Invalid file type. Please upload an Excel file (.xlsx or .xls)");
        }

        BulkImportResponseDto result = teacherService.bulkImportTeachers(file, skipInvalidRows);
        return ResponseHandler.success("Bulk import completed", result);
    }
}