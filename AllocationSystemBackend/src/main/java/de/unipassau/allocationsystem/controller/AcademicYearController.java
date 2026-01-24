package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.academicyear.AcademicYearCreateDto;
import de.unipassau.allocationsystem.dto.academicyear.AcademicYearResponseDto;
import de.unipassau.allocationsystem.dto.academicyear.AcademicYearUpdateDto;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.mapper.AcademicYearMapper;
import de.unipassau.allocationsystem.mapper.BaseMapper;
import de.unipassau.allocationsystem.service.AcademicYearService;
import de.unipassau.allocationsystem.service.CrudService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing academic years.
 * Provides CRUD operations for academic year entities.
 */
@RestController
@RequestMapping("/academic-years")
@RequiredArgsConstructor
@Tag(name = "Academic Years", description = "Academic year management APIs")
public class AcademicYearController
        extends CrudControllerBase<AcademicYear, AcademicYearCreateDto, AcademicYearUpdateDto, AcademicYearResponseDto> {

    private final AcademicYearService academicYearService;
    private final AcademicYearMapper academicYearMapper;

    @Override
    protected CrudService<AcademicYear, Long> getService() {
        return academicYearService;
    }

    @Override
    protected BaseMapper<AcademicYear, AcademicYearCreateDto, AcademicYearUpdateDto, AcademicYearResponseDto> getMapper() {
        return academicYearMapper;
    }
}