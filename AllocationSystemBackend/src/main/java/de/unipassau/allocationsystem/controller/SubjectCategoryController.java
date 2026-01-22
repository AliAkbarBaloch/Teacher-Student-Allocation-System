package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.subjectcategory.SubjectCategoryCreateDto;
import de.unipassau.allocationsystem.dto.subjectcategory.SubjectCategoryResponseDto;
import de.unipassau.allocationsystem.dto.subjectcategory.SubjectCategoryUpdateDto;
import de.unipassau.allocationsystem.entity.SubjectCategory;
import de.unipassau.allocationsystem.mapper.BaseMapper;
import de.unipassau.allocationsystem.mapper.SubjectCategoryMapper;
import de.unipassau.allocationsystem.service.CrudService;
import de.unipassau.allocationsystem.service.SubjectCategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing subject categories.
 * Provides CRUD operations for subject category entities.
 */
@RestController
@RequestMapping("/subject-categories")
@RequiredArgsConstructor
@Tag(name = "SubjectCategories", description = "Subject Category management APIs")
public class SubjectCategoryController
        extends CrudControllerBase<SubjectCategory, SubjectCategoryCreateDto, SubjectCategoryUpdateDto, SubjectCategoryResponseDto> {

    private final SubjectCategoryService subjectCategoryService;
    private final SubjectCategoryMapper subjectCategoryMapper;

    @Override
    protected CrudService<SubjectCategory, Long> getService() {
        return subjectCategoryService;
    }

    @Override
    protected BaseMapper<SubjectCategory, SubjectCategoryCreateDto, SubjectCategoryUpdateDto, SubjectCategoryResponseDto> getMapper() {
        return subjectCategoryMapper;
    }
}