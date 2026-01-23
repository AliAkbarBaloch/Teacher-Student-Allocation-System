package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.internshiptype.InternshipTypeCreateDto;
import de.unipassau.allocationsystem.dto.internshiptype.InternshipTypeResponseDto;
import de.unipassau.allocationsystem.dto.internshiptype.InternshipTypeUpdateDto;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.mapper.BaseMapper;
import de.unipassau.allocationsystem.mapper.InternshipTypeMapper;
import de.unipassau.allocationsystem.service.CrudService;
import de.unipassau.allocationsystem.service.InternshipTypeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing internship types.
 * Provides CRUD operations for internship type entities.
 */
@RestController
@RequestMapping("/internship-types")
@RequiredArgsConstructor
@Tag(name = "Internship Types", description = "Internship type management APIs")
public class InternshipTypeController
        extends CrudControllerBase<InternshipType, InternshipTypeCreateDto, InternshipTypeUpdateDto, InternshipTypeResponseDto> {

    private final InternshipTypeService internshipTypeService;
    private final InternshipTypeMapper internshipTypeMapper;

    @Override
    protected CrudService<InternshipType, Long> getService() {
        return internshipTypeService;
    }

    @Override
    protected BaseMapper<InternshipType, InternshipTypeCreateDto, InternshipTypeUpdateDto, InternshipTypeResponseDto> getMapper() {
        return internshipTypeMapper;
    }
}