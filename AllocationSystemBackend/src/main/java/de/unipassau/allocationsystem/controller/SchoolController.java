package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.school.SchoolCreateDto;
import de.unipassau.allocationsystem.dto.school.SchoolResponseDto;
import de.unipassau.allocationsystem.dto.school.SchoolStatusUpdateDto;
import de.unipassau.allocationsystem.dto.school.SchoolUpdateDto;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.SchoolMapper;
import de.unipassau.allocationsystem.service.SchoolService;
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

@RestController
@RequestMapping("/schools")
@RequiredArgsConstructor
@Tag(name = "Schools", description = "School management APIs")
public class SchoolController {

    private final SchoolService schoolService;
    private final SchoolMapper schoolMapper;

    @Operation(
            summary = "Get sort fields",
            description = "Retrieves available fields that can be used for sorting schools"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sort fields retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/sort-fields")
    public ResponseEntity<?> getSortFields() {
        List<Map<String, String>> result = schoolService.getSortFields();
        return ResponseHandler.success("Sort fields retrieved successfully", result);
    }

    @Operation(
            summary = "Get school by ID",
            description = "Retrieves a specific school by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "School found",
                    content = @Content(schema = @Schema(implementation = SchoolResponseDto.class))
            ),
            @ApiResponse(responseCode = "404", description = "School not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        SchoolResponseDto result = schoolService.getById(id)
                .map(schoolMapper::toResponseDto)
                .orElseThrow(() -> new ResourceNotFoundException("School not found with id: " + id));
        return ResponseHandler.success("School retrieved successfully", result);
    }

    @Operation(
            summary = "Get paginated schools",
            description = "Retrieves schools with pagination, sorting, and optional search"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Schools retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        Map<String, Object> result = schoolService.getPaginated(queryParams, searchValue);
        
        // Map School entities to DTOs
        @SuppressWarnings("unchecked")
        List<School> schools = (List<School>) result.get("items");
        if (schools != null) {
            List<SchoolResponseDto> schoolDtos = schoolMapper.toResponseDtoList(schools);
            result.put("items", schoolDtos);
        }
        
        return ResponseHandler.success("Schools retrieved successfully (paginated)", result);
    }

    @Operation(
            summary = "Get all schools",
            description = "Retrieves all schools without pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Schools retrieved successfully",
                    content = @Content(schema = @Schema(implementation = SchoolResponseDto.class))
            ),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("")
    public ResponseEntity<?> getAll(@RequestParam(value = "includeRelations", defaultValue = "true") boolean includeRelations) {
        List<SchoolResponseDto> result = schoolMapper.toResponseDtoList(schoolService.getAll());
        return ResponseHandler.success("Schools retrieved successfully", result);
    }

    @Operation(
            summary = "Create new school",
            description = "Creates a new school with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "School created successfully",
                    content = @Content(schema = @Schema(implementation = SchoolResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate school"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody SchoolCreateDto dto) {
        School school = schoolMapper.toEntityCreate(dto);
        School created = schoolService.create(school);
        return ResponseHandler.created("School created successfully", schoolMapper.toResponseDto(created));
    }

    @Operation(
            summary = "Update school",
            description = "Updates an existing school with the provided details"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "School updated successfully",
                    content = @Content(schema = @Schema(implementation = SchoolResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input or duplicate name"),
            @ApiResponse(responseCode = "404", description = "School not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody SchoolUpdateDto dto) {
        School school = schoolMapper.toEntityUpdate(dto);
        School updated = schoolService.update(id, school);
        return ResponseHandler.updated("School updated successfully", schoolMapper.toResponseDto(updated));
    }

    @Operation(
            summary = "Update school status",
            description = "Updates the active status of a school (activate/deactivate)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "School status updated successfully",
                    content = @Content(schema = @Schema(implementation = SchoolResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "School not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody SchoolStatusUpdateDto statusDto) {
        School updated = schoolService.updateStatus(id, statusDto.getIsActive());
        return ResponseHandler.updated("School status updated successfully", schoolMapper.toResponseDto(updated));
    }

    @Operation(
            summary = "Delete school",
            description = "Deletes a school by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "School deleted successfully"),
            @ApiResponse(responseCode = "404", description = "School not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        schoolService.delete(id);
        return ResponseHandler.noContent();
    }
}