package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.teacherassignment.TeacherAssignmentCreateDto;
import de.unipassau.allocationsystem.dto.teacherassignment.TeacherAssignmentResponseDto;
import de.unipassau.allocationsystem.dto.teacherassignment.TeacherAssignmentUpdateDto;
import de.unipassau.allocationsystem.entity.TeacherAssignment;
import de.unipassau.allocationsystem.mapper.BaseMapper;
import de.unipassau.allocationsystem.mapper.TeacherAssignmentMapper;
import de.unipassau.allocationsystem.service.CrudService;
import de.unipassau.allocationsystem.service.TeacherAssignmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing teacher assignments.
 * Provides CRUD operations for teacher assignment entities within allocation plans.
 */
@RestController
@RequestMapping("/teacher-assignments")
@RequiredArgsConstructor
@Tag(name = "Teacher Assignments", description = "Manage teacher assignments within allocation plans")
public class TeacherAssignmentController
        extends CrudControllerBase<TeacherAssignment, TeacherAssignmentCreateDto, TeacherAssignmentUpdateDto, TeacherAssignmentResponseDto> {

    private final TeacherAssignmentService assignmentService;
    private final TeacherAssignmentMapper assignmentMapper;

    @Override
    protected CrudService<TeacherAssignment, Long> getService() {
        return assignmentService;
    }

    @Override
    protected BaseMapper<TeacherAssignment, TeacherAssignmentCreateDto, TeacherAssignmentUpdateDto, TeacherAssignmentResponseDto> getMapper() {
        return assignmentMapper;
    }
}