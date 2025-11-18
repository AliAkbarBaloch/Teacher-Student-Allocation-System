package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.teacher.formsubmission.TeacherFormSubmissionCreateDto;
import de.unipassau.allocationsystem.dto.teacher.formsubmission.TeacherFormSubmissionResponseDto;
import de.unipassau.allocationsystem.dto.teacher.formsubmission.TeacherFormSubmissionStatusUpdateDto;
import de.unipassau.allocationsystem.entity.TeacherFormSubmission;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TeacherFormSubmissionMapper implements BaseMapper<
        TeacherFormSubmission,
        TeacherFormSubmissionCreateDto,
        TeacherFormSubmissionStatusUpdateDto,
        TeacherFormSubmissionResponseDto> {

    private final TeacherRepository teacherRepository;
    private final AcademicYearRepository academicYearRepository;

    @Override
    public TeacherFormSubmission toEntityCreate(TeacherFormSubmissionCreateDto createDto) {
        if (createDto == null) {
            return null;
        }
        TeacherFormSubmission entity = new TeacherFormSubmission();
        entity.setTeacher(teacherRepository.findById(createDto.getTeacherId()).orElse(null));
        entity.setAcademicYear(academicYearRepository.findById(createDto.getYearId()).orElse(null));
        entity.setFormToken(createDto.getFormToken());
        entity.setSubmittedAt(createDto.getSubmittedAt());
        entity.setSubmissionData(createDto.getSubmissionData());
        entity.setIsProcessed(false); // default
        return entity;
    }

    @Override
    public TeacherFormSubmission toEntityUpdate(TeacherFormSubmissionStatusUpdateDto updateDto) {
        if (updateDto == null) {
            return null;
        }
        TeacherFormSubmission entity = new TeacherFormSubmission();
        entity.setIsProcessed(updateDto.getIsProcessed());
        return entity;
    }

    @Override
    public TeacherFormSubmissionResponseDto toResponseDto(TeacherFormSubmission entity) {
        if (entity == null) {
            return null;
        }
        return TeacherFormSubmissionResponseDto.builder()
                .id(entity.getId())
                .teacherId(entity.getTeacher() != null ? entity.getTeacher().getId() : null)
                .teacherFirstName(entity.getTeacher() != null ? entity.getTeacher().getFirstName() : null)
                .teacherLastName(entity.getTeacher() != null ? entity.getTeacher().getLastName() : null)
                .teacherEmail(entity.getTeacher() != null ? entity.getTeacher().getEmail() : null)
                .yearId(entity.getAcademicYear() != null ? entity.getAcademicYear().getId() : null)
                .yearName(entity.getAcademicYear() != null ? entity.getAcademicYear().getYearName() : null)
                .formToken(entity.getFormToken())
                .submittedAt(entity.getSubmittedAt())
                .submissionData(entity.getSubmissionData())
                .isProcessed(entity.getIsProcessed())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    public List<TeacherFormSubmissionResponseDto> toResponseDtoList(List<TeacherFormSubmission> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void updateEntityFromDto(TeacherFormSubmissionStatusUpdateDto updateDto, TeacherFormSubmission entity) {
        if (updateDto == null || entity == null) {
            return;
        }
        if (updateDto.getIsProcessed() != null) {
            entity.setIsProcessed(updateDto.getIsProcessed());
        }
    }
}