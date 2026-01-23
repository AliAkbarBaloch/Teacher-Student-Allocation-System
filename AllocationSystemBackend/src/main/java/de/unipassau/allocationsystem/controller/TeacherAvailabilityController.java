package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.teacher.availability.TeacherAvailabilityCreateDto;
import de.unipassau.allocationsystem.dto.teacher.availability.TeacherAvailabilityResponseDto;
import de.unipassau.allocationsystem.dto.teacher.availability.TeacherAvailabilityUpdateDto;
import de.unipassau.allocationsystem.entity.TeacherAvailability;
import de.unipassau.allocationsystem.mapper.BaseMapper;
import de.unipassau.allocationsystem.mapper.TeacherAvailabilityMapper;
import de.unipassau.allocationsystem.service.CrudService;
import de.unipassau.allocationsystem.service.TeacherAvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller for managing teacher availability.
 * Provides CRUD operations for teacher availability entries.
 */
@RestController
@RequestMapping("/teacher-availability")
@RequiredArgsConstructor
@Tag(name = "TeacherAvailability", description = "Teacher availability management APIs")
public class TeacherAvailabilityController extends CrudControllerBase<TeacherAvailability, TeacherAvailabilityCreateDto, TeacherAvailabilityUpdateDto, TeacherAvailabilityResponseDto> {

    private final TeacherAvailabilityService teacherAvailabilityService;
    private final TeacherAvailabilityMapper teacherAvailabilityMapper;

    @Override
    protected CrudService<TeacherAvailability, Long> getService() {
        return teacherAvailabilityService;
    }

    @Override
    protected BaseMapper<TeacherAvailability, TeacherAvailabilityCreateDto, TeacherAvailabilityUpdateDto, TeacherAvailabilityResponseDto> getMapper() {
        return teacherAvailabilityMapper;
    }

    /**
     * Retrieves teacher availability with pagination and sorting.
     * Converts returned items into DTOs to avoid lazy loading serialization issues.
     */
    @Operation(
            summary = "Get paginated teacher availability",
            description = "Retrieves teacher availability with pagination, sorting, and optional search"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Availability entries retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @Override
    @GetMapping("/paginate")
    public ResponseEntity<?> getPaginate(
            @RequestParam Map<String, String> queryParams,
            @RequestParam(value = "searchValue", required = false) String searchValue
    ) {
        Map<String, Object> result = teacherAvailabilityService.getPaginated(queryParams, searchValue);

        Object itemsObj = result.get("items");
        if (itemsObj instanceof List<?> items && !items.isEmpty() && items.get(0) instanceof TeacherAvailability) {
            @SuppressWarnings("unchecked")
            List<TeacherAvailability> entities = (List<TeacherAvailability>) itemsObj;

            List<TeacherAvailabilityResponseDto> dtoItems = teacherAvailabilityMapper.toResponseDtoList(entities);
            result.put("items", dtoItems);
        }

        return ok("Teacher availability retrieved successfully (paginated)", result);
    }
}