package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.TeacherFormSubmissionResponseDto;
import de.unipassau.allocationsystem.entity.TeacherFormSubmission;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TeacherFormSubmissionMapper implements BaseMapper<TeacherFormSubmission, TeacherFormSubmissionResponseDto> {

    private final TeacherRepository teacherRepository;
    private final AcademicYearRepository academicYearRepository;

    @Override
    public TeacherFormSubmission toEntity(TeacherFormSubmissionResponseDto dto) {
        if (dto == null) {
            return null;
        }
        TeacherFormSubmission entity = new TeacherFormSubmission();
        if (dto.getId() != null && dto.getId() > 0) {
            entity.setId(dto.getId());
        }
        entity.setFormToken(dto.getFormToken());
        entity.setSubmittedAt(dto.getSubmittedAt());
        entity.setSubmissionData(dto.getSubmissionData());
        entity.setIsProcessed(dto.getIsProcessed());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setUpdatedAt(dto.getUpdatedAt());

        // Fetch related entities by ID
        entity.setTeacher(
                teacherRepository.findById(dto.getTeacherId())
                        .orElse(null)
        );
        entity.setAcademicYear(
                academicYearRepository.findById(dto.getYearId())
                        .orElse(null)
        );

        return entity;
    }

    @Override
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

    @Override
    public List<TeacherFormSubmissionResponseDto> toDtoList(List<TeacherFormSubmission> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
