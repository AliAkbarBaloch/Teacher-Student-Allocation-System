package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.teacherassignment.TeacherAssignmentCreateDto;
import de.unipassau.allocationsystem.dto.teacherassignment.TeacherAssignmentResponseDto;
import de.unipassau.allocationsystem.entity.*;
import org.springframework.stereotype.Component;

@Component
public class TeacherAssignmentMapper {

    public TeacherAssignment toEntity(TeacherAssignmentCreateDto dto, AllocationPlan plan, Teacher teacher,
                                      InternshipType internshipType, Subject subject) {
        TeacherAssignment ta = new TeacherAssignment();
        ta.setAllocationPlan(plan);
        ta.setTeacher(teacher);
        ta.setInternshipType(internshipType);
        ta.setSubject(subject);
        ta.setStudentGroupSize(dto.getStudentGroupSize());
        ta.setAssignmentStatus(TeacherAssignment.AssignmentStatus.valueOf(dto.getAssignmentStatus()));
        ta.setIsManualOverride(dto.getIsManualOverride() != null && dto.getIsManualOverride());
        ta.setNotes(dto.getNotes());
        return ta;
    }

    public TeacherAssignmentResponseDto toResponseDto(TeacherAssignment entity) {
        TeacherAssignmentResponseDto dto = new TeacherAssignmentResponseDto();
        dto.setId(entity.getId());
        dto.setPlanId(entity.getAllocationPlan() != null ? entity.getAllocationPlan().getId() : null);
        dto.setTeacherId(entity.getTeacher() != null ? entity.getTeacher().getId() : null);
        dto.setInternshipTypeId(entity.getInternshipType() != null ? entity.getInternshipType().getId() : null);
        dto.setSubjectId(entity.getSubject() != null ? entity.getSubject().getId() : null);
        dto.setStudentGroupSize(entity.getStudentGroupSize());
        dto.setAssignmentStatus(entity.getAssignmentStatus() != null ? entity.getAssignmentStatus().name() : null);
        dto.setIsManualOverride(entity.getIsManualOverride());
        dto.setNotes(entity.getNotes());
        dto.setAssignedAt(entity.getAssignedAt());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
