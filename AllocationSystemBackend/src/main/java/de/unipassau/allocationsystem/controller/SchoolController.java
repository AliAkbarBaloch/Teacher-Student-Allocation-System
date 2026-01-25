package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.school.SchoolCreateDto;
import de.unipassau.allocationsystem.dto.school.SchoolResponseDto;
import de.unipassau.allocationsystem.dto.school.SchoolUpdateDto;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.mapper.BaseMapper;
import de.unipassau.allocationsystem.mapper.SchoolMapper;
import de.unipassau.allocationsystem.service.CrudService;
import de.unipassau.allocationsystem.service.SchoolService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing schools.
 * Provides CRUD operations for school entities.
 */
@RestController
@RequestMapping("/schools")
@RequiredArgsConstructor
@Tag(name = "Schools", description = "School management APIs")
public class SchoolController
        extends CrudControllerBase<School, SchoolCreateDto, SchoolUpdateDto, SchoolResponseDto> {

    private final SchoolService schoolService;
    private final SchoolMapper schoolMapper;

    @Override
    protected CrudService<School, Long> getService() {
        return schoolService;
    }

    @Override
    protected BaseMapper<School, SchoolCreateDto, SchoolUpdateDto, SchoolResponseDto> getMapper() {
        return schoolMapper;
    }
}