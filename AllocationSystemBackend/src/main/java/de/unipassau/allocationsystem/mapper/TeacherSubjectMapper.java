package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.teachersubject.TeacherSubjectResponseDto;
import de.unipassau.allocationsystem.entity.TeacherSubject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TeacherSubjectMapper {

    public TeacherSubjectResponseDto toDto(TeacherSubject entity) {
        if (entity == null) return null;
        return TeacherSubjectResponseDto.builder()
                .id(entity.getId())
                .yearId(entity.getAcademicYear() != null ? entity.getAcademicYear().getId() : null)
                .teacherId(entity.getTeacher() != null ? entity.getTeacher().getId() : null)
                .subjectId(entity.getSubject() != null ? entity.getSubject().getId() : null)
                .availabilityStatus(entity.getAvailabilityStatus())
                .gradeLevelFrom(entity.getGradeLevelFrom())
                .gradeLevelTo(entity.getGradeLevelTo())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
