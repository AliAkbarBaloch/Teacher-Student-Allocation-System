package de.unipassau.allocationsystem.mapper;

import de.unipassau.allocationsystem.dto.teacher.formsubmission.TeacherFormSubmissionCreateDto;
import de.unipassau.allocationsystem.dto.teacher.formsubmission.TeacherFormSubmissionResponseDto;
import de.unipassau.allocationsystem.dto.teacher.formsubmission.TeacherFormSubmissionStatusUpdateDto;
import de.unipassau.allocationsystem.entity.TeacherFormSubmission;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
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
        
        // Set distinct submission fields
        populateSubmissionFieldsFromDto(entity, createDto);
        
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
                .teacherId(getTeacherId(entity))
                .teacherFirstName(getTeacherFirstName(entity))
                .teacherLastName(getTeacherLastName(entity))
                .teacherEmail(getTeacherEmail(entity))
                .yearId(getYearId(entity))
                .yearName(getYearName(entity))
                .formToken(entity.getFormToken())
                .submittedAt(entity.getSubmittedAt())
                
                // Map distinct submission fields
                .schoolId(entity.getSchoolId())
                .employmentStatus(entity.getEmploymentStatus())
                .notes(entity.getNotes())
                .subjectIds(parseLongListFromString(entity.getSubjectIds()))
                .internshipTypePreference(entity.getInternshipTypePreference())
                .internshipCombinations(parseStringListFromString(entity.getInternshipCombinations()))
                .semesterAvailability(parseStringListFromString(entity.getSemesterAvailability()))
                .availabilityOptions(parseStringListFromString(entity.getAvailabilityOptions()))
                
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

    // ==================== Helper Methods ====================

    /**
     * Populate submission entity fields from DTO.
     */
    private void populateSubmissionFieldsFromDto(TeacherFormSubmission entity, TeacherFormSubmissionCreateDto dto) {
        entity.setSchoolId(dto.getSchoolId());
        entity.setEmploymentStatus(dto.getEmploymentStatus());
        entity.setNotes(dto.getNotes());
        entity.setSubjectIds(convertLongListToString(dto.getSubjectIds()));
        entity.setInternshipTypePreference(dto.getInternshipTypePreference());
        entity.setInternshipCombinations(convertStringListToString(dto.getInternshipCombinations()));
        entity.setSemesterAvailability(convertStringListToString(dto.getSemesterAvailability()));
        entity.setAvailabilityOptions(convertStringListToString(dto.getAvailabilityOptions()));
    }

    /**
     * Convert list of Longs to comma-separated string.
     */
    private String convertLongListToString(List<Long> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    /**
     * Convert list of Strings to comma-separated string.
     */
    private String convertStringListToString(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return String.join(",", list);
    }

    /**
     * Parse comma-separated string to list of Longs.
     */
    private List<Long> parseLongListFromString(String str) {
        if (str == null || str.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(str.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }

    /**
     * Parse comma-separated string to list of Strings.
     */
    private List<String> parseStringListFromString(String str) {
        if (str == null || str.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(str.split(","));
    }

    // Teacher getter helpers to reduce null checks
    private Long getTeacherId(TeacherFormSubmission entity) {
        return entity.getTeacher() != null ? entity.getTeacher().getId() : null;
    }

    private String getTeacherFirstName(TeacherFormSubmission entity) {
        return entity.getTeacher() != null ? entity.getTeacher().getFirstName() : null;
    }

    private String getTeacherLastName(TeacherFormSubmission entity) {
        return entity.getTeacher() != null ? entity.getTeacher().getLastName() : null;
    }

    private String getTeacherEmail(TeacherFormSubmission entity) {
        return entity.getTeacher() != null ? entity.getTeacher().getEmail() : null;
    }

    // AcademicYear getter helpers to reduce null checks
    private Long getYearId(TeacherFormSubmission entity) {
        return entity.getAcademicYear() != null ? entity.getAcademicYear().getId() : null;
    }

    private String getYearName(TeacherFormSubmission entity) {
        return entity.getAcademicYear() != null ? entity.getAcademicYear().getYearName() : null;
    }
}