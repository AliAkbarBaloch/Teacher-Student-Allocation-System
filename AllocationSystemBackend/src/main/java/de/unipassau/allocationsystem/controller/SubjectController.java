package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.subject.SubjectCreateDto;
import de.unipassau.allocationsystem.dto.subject.SubjectResponseDto;
import de.unipassau.allocationsystem.dto.subject.SubjectUpdateDto;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.mapper.BaseMapper;
import de.unipassau.allocationsystem.mapper.SubjectMapper;
import de.unipassau.allocationsystem.service.CrudService;
import de.unipassau.allocationsystem.service.SubjectService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for comprehensive subject management operations.
 * Provides endpoints for CRUD operations on subjects with pagination, search, and validation.
 */
@RestController
@RequestMapping("/subjects")
@RequiredArgsConstructor
@Tag(name = "Subjects", description = "Subject management APIs")
public class SubjectController
        extends CrudControllerBase<Subject, SubjectCreateDto, SubjectUpdateDto, SubjectResponseDto> {

    private final SubjectService subjectService;
    private final SubjectMapper subjectMapper;

    @Override
    protected CrudService<Subject, Long> getService() {
        return subjectService;
    }

    @Override
    protected BaseMapper<Subject, SubjectCreateDto, SubjectUpdateDto, SubjectResponseDto> getMapper() {
        return subjectMapper;
    }
}

