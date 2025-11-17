package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.TeacherFormSubmissionResponseDto;
import de.unipassau.allocationsystem.entity.TeacherFormSubmission;
import org.springframework.stereotype.Component;

/**
 * Mapper for TeacherFormSubmission entity and DTOs.
 */
@Component
public class TeacherFormSubmissionMapper {

    /**
     * Convert TeacherFormSubmission entity to response DTO with denormalized fields.
     *
     * @param entity TeacherFormSubmission entity
     * @return TeacherFormSubmissionResponseDto
     */
    public TeacherFormSubmissionResponseDto toDto(TeacherFormSubmission entity) {
        if (entity == null) {
            return null;
        }

        return TeacherFormSubmissionResponseDto.builder()
                .id(entity.getId())
                .teacherId(entity.getTeacher().getId())
                .teacherFirstName(entity.getTeacher().getFirstName())
                .teacherLastName(entity.getTeacher().getLastName())
                .teacherEmail(entity.getTeacher().getEmail())
                .yearId(entity.getAcademicYear().getId())
                .yearName(entity.getAcademicYear().getYearName())
                .formToken(entity.getFormToken())
                .submittedAt(entity.getSubmittedAt())
                .submissionData(entity.getSubmissionData())
                .isProcessed(entity.getIsProcessed())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
