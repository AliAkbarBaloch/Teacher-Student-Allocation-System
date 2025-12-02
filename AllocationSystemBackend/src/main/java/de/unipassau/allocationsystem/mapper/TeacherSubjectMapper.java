package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.teachersubject.TeacherSubjectCreateDto;
import de.unipassau.allocationsystem.dto.teachersubject.TeacherSubjectResponseDto;
import de.unipassau.allocationsystem.dto.teachersubject.TeacherSubjectUpdateDto;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.TeacherSubject;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.SubjectRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TeacherSubjectMapper implements BaseMapper<TeacherSubject, TeacherSubjectCreateDto, TeacherSubjectUpdateDto, TeacherSubjectResponseDto> {

    private final AcademicYearRepository academicYearRepository;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;

    @Override
    public TeacherSubject toEntityCreate(TeacherSubjectCreateDto dto) {
        if (dto == null) {
            return null;
        }
        TeacherSubject entity = new TeacherSubject();

        if (dto.getAcademicYearId() != null) {
            AcademicYear year = academicYearRepository.findById(dto.getAcademicYearId())
                    .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + dto.getAcademicYearId()));
            entity.setAcademicYear(year);
        }
        if (dto.getTeacherId() != null) {
            Teacher teacher = teacherRepository.findById(dto.getTeacherId())
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + dto.getTeacherId()));
            entity.setTeacher(teacher);
        }
        if (dto.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(dto.getSubjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + dto.getSubjectId()));
            entity.setSubject(subject);
        }
        entity.setAvailabilityStatus(dto.getAvailabilityStatus());
        entity.setGradeLevelFrom(dto.getGradeLevelFrom());
        entity.setGradeLevelTo(dto.getGradeLevelTo());
        entity.setNotes(dto.getNotes());

        return entity;
    }

    @Override
    public TeacherSubject toEntityUpdate(TeacherSubjectUpdateDto dto) {
        if (dto == null) {
            return null;
        }
        TeacherSubject entity = new TeacherSubject();
        entity.setAvailabilityStatus(dto.getAvailabilityStatus());
        entity.setGradeLevelFrom(dto.getGradeLevelFrom());
        entity.setGradeLevelTo(dto.getGradeLevelTo());
        entity.setNotes(dto.getNotes());
        return entity;
    }

    @Override
    public TeacherSubjectResponseDto toResponseDto(TeacherSubject entity) {
        if (entity == null) {
            return null;
        }
        return TeacherSubjectResponseDto.builder()
                .id(entity.getId())
                .academicYearId(entity.getAcademicYear() != null ? entity.getAcademicYear().getId() : null)
                .academicYearTitle(entity.getAcademicYear() != null ? entity.getAcademicYear().getYearName() : null)
                .teacherId(entity.getTeacher() != null ? entity.getTeacher().getId() : null)
                .teacherTitle(entity.getTeacher() != null ? (entity.getTeacher().getFirstName() + " " + entity.getTeacher().getLastName()) : null)
                .subjectId(entity.getSubject() != null ? entity.getSubject().getId() : null)
                .subjectTitle(entity.getSubject() != null ? entity.getSubject().getSubjectTitle() : null)
                .availabilityStatus(entity.getAvailabilityStatus())
                .gradeLevelFrom(entity.getGradeLevelFrom())
                .gradeLevelTo(entity.getGradeLevelTo())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    public List<TeacherSubjectResponseDto> toResponseDtoList(List<TeacherSubject> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateEntityFromDto(TeacherSubjectUpdateDto dto, TeacherSubject entity) {
        if (dto == null || entity == null) {
            return;
        }
        if (dto.getAvailabilityStatus() != null) {
            entity.setAvailabilityStatus(dto.getAvailabilityStatus());
        }
        if (dto.getGradeLevelFrom() != null) {
            entity.setGradeLevelFrom(dto.getGradeLevelFrom());
        }
        if (dto.getGradeLevelTo() != null) {
            entity.setGradeLevelTo(dto.getGradeLevelTo());
        }
        if (dto.getNotes() != null) {
            entity.setNotes(dto.getNotes());
        }
    }
}