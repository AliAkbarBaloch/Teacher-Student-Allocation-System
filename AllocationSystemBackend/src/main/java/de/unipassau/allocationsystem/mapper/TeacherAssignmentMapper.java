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
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
/**
 * Mapper for converting between TeacherAssignment entities and DTOs.
 * Handles assignment mapping with allocation plan, teacher, internship type, and subject resolution.
 */
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

        TeacherAssignmentResponseDto dto = new TeacherAssignmentResponseDto();
        dto.setId(entity.getId());

        mapAllocationPlan(entity, dto);
        mapTeacher(entity, dto);
        mapInternshipType(entity, dto);
        mapSubject(entity, dto);

        dto.setStudentGroupSize(entity.getStudentGroupSize());
        dto.setAssignmentStatus(Optional.ofNullable(entity.getAssignmentStatus()).map(Enum::name).orElse(null));
        dto.setIsManualOverride(entity.getIsManualOverride());
        dto.setNotes(entity.getNotes());
        dto.setAssignedAt(entity.getAssignedAt());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }

    private void mapAllocationPlan(TeacherAssignment entity, TeacherAssignmentResponseDto dto) {
        AllocationPlan plan = entity.getAllocationPlan();
        if (plan != null) {
            dto.setPlanId(plan.getId());
            String planTitle = buildPlanTitle(plan);
            dto.setPlanTitle(planTitle);
        }
    }

    private String buildPlanTitle(AllocationPlan plan) {
        return Optional.ofNullable(plan.getAcademicYear())
                .map(year -> year.getYearName() + " (" + plan.getPlanVersion() + ")")
                .orElse(null);
    }

    private void mapTeacher(TeacherAssignment entity, TeacherAssignmentResponseDto dto) {
        Teacher teacher = entity.getTeacher();
        if (teacher != null) {
            dto.setTeacherId(teacher.getId());
            dto.setTeacherTitle(teacher.getFirstName() + " " + teacher.getLastName());
        }
    }

    private void mapInternshipType(TeacherAssignment entity, TeacherAssignmentResponseDto dto) {
        InternshipType type = entity.getInternshipType();
        if (type != null) {
            dto.setInternshipTypeId(type.getId());
            dto.setInternshipTypeTitle(type.getInternshipCode() + " " + type.getPeriodType());
        }
    }

    private void mapSubject(TeacherAssignment entity, TeacherAssignmentResponseDto dto) {
        Subject subject = entity.getSubject();
        if (subject != null) {
            dto.setSubjectId(subject.getId());
            dto.setSubjectTitle(subject.getSubjectTitle());
        }
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
