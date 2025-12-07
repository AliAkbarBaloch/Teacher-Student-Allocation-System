package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.teacherassignment.TeacherAssignmentCreateDto;
import de.unipassau.allocationsystem.dto.teacherassignment.TeacherAssignmentUpdateDto;
import de.unipassau.allocationsystem.dto.teacherassignment.TeacherAssignmentResponseDto;
import de.unipassau.allocationsystem.entity.TeacherAssignment;
import de.unipassau.allocationsystem.entity.AllocationPlan;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.AllocationPlanRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import de.unipassau.allocationsystem.repository.InternshipTypeRepository;
import de.unipassau.allocationsystem.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TeacherAssignmentMapper implements BaseMapper<TeacherAssignment, TeacherAssignmentCreateDto, TeacherAssignmentUpdateDto, TeacherAssignmentResponseDto> {

    private final AllocationPlanRepository allocationPlanRepository;
    private final TeacherRepository teacherRepository;
    private final InternshipTypeRepository internshipTypeRepository;
    private final SubjectRepository subjectRepository;

    @Override
    public TeacherAssignment toEntityCreate(TeacherAssignmentCreateDto dto) {
        if (dto == null) {
            return null;
        }
        TeacherAssignment entity = new TeacherAssignment();

        // Check and set relations
        if (dto.getPlanId() != null) {
            AllocationPlan plan = allocationPlanRepository.findById(dto.getPlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Allocation plan not found with id: " + dto.getPlanId()));
            entity.setAllocationPlan(plan);
        }
        if (dto.getTeacherId() != null) {
            Teacher teacher = teacherRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + dto.getTeacherId()));
            entity.setTeacher(teacher);
        }
        if (dto.getInternshipTypeId() != null) {
            InternshipType internshipType = internshipTypeRepository.findById(dto.getInternshipTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Internship type not found with id: " + dto.getInternshipTypeId()));
            entity.setInternshipType(internshipType);
        }
        if (dto.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + dto.getSubjectId()));
            entity.setSubject(subject);
        }

        entity.setStudentGroupSize(dto.getStudentGroupSize());
        entity.setAssignmentStatus(dto.getAssignmentStatus() != null ? TeacherAssignment.AssignmentStatus.valueOf(dto.getAssignmentStatus()) : null);
        entity.setIsManualOverride(dto.getIsManualOverride() != null && dto.getIsManualOverride());
        entity.setNotes(dto.getNotes());
        return entity;
    }

    @Override
    public TeacherAssignment toEntityUpdate(TeacherAssignmentUpdateDto dto) {
        if (dto == null) {
            return null;
        }
        TeacherAssignment entity = new TeacherAssignment();
        entity.setStudentGroupSize(dto.getStudentGroupSize());
        entity.setAssignmentStatus(dto.getAssignmentStatus() != null ? TeacherAssignment.AssignmentStatus.valueOf(dto.getAssignmentStatus()) : null);
        entity.setIsManualOverride(dto.getIsManualOverride());
        entity.setNotes(dto.getNotes());
        return entity;
    }

    @Override
    public TeacherAssignmentResponseDto toResponseDto(TeacherAssignment entity) {
        if (entity == null) {
            return null;
        }
        return new TeacherAssignmentResponseDto(
            entity.getId(),
            entity.getAllocationPlan() != null ? entity.getAllocationPlan().getId() : null,
            entity.getTeacher() != null ? entity.getTeacher().getId() : null,
            entity.getInternshipType() != null ? entity.getInternshipType().getId() : null,
            entity.getSubject() != null ? entity.getSubject().getId() : null,
            entity.getStudentGroupSize(),
            entity.getAssignmentStatus() != null ? entity.getAssignmentStatus().name() : null,
            entity.getIsManualOverride(),
            entity.getNotes(),
            entity.getAssignedAt(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    @Override
    public List<TeacherAssignmentResponseDto> toResponseDtoList(List<TeacherAssignment> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateEntityFromDto(TeacherAssignmentUpdateDto dto, TeacherAssignment entity) {
        if (dto == null || entity == null) {
            return;
        }
        if (dto.getStudentGroupSize() != null) {
            entity.setStudentGroupSize(dto.getStudentGroupSize());
        }
        if (dto.getAssignmentStatus() != null) {
            entity.setAssignmentStatus(TeacherAssignment.AssignmentStatus.valueOf(dto.getAssignmentStatus()));
        }
        if (dto.getIsManualOverride() != null) {
            entity.setIsManualOverride(dto.getIsManualOverride());
        }
        if (dto.getNotes() != null) {
            entity.setNotes(dto.getNotes());
        }
    }
}