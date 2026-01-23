package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.dto.teacher.formsubmission.TeacherFormSubmissionCreateDto;
import de.unipassau.allocationsystem.dto.teacher.formsubmission.TeacherFormSubmissionResponseDto;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.TeacherFormSubmission;

import java.time.LocalDateTime;

final class TeacherFormSubmissionServiceTestFixtures {

    private TeacherFormSubmissionServiceTestFixtures() {
    }

    static Teacher teacher() {
        Teacher teacher = new Teacher();
        teacher.setId(1L);
        teacher.setFirstName("John");
        teacher.setLastName("Doe");
        teacher.setEmail("john.doe@example.com");
        return teacher;
    }

    static AcademicYear academicYear(boolean locked) {
        AcademicYear academicYear = new AcademicYear();
        academicYear.setId(1L);
        academicYear.setYearName("2024/2025");
        academicYear.setIsLocked(locked);
        return academicYear;
    }

    static TeacherFormSubmission submission(Teacher teacher, AcademicYear academicYear, LocalDateTime now) {
        TeacherFormSubmission submission = new TeacherFormSubmission();
        submission.setId(1L);
        submission.setTeacher(teacher);
        submission.setAcademicYear(academicYear);
        submission.setFormToken("unique-token-123");
        submission.setSubmittedAt(now);
        submission.setIsProcessed(false);
        return submission;
    }

    static TeacherFormSubmissionResponseDto responseDto(LocalDateTime now) {
        return TeacherFormSubmissionResponseDto.builder()
                .id(1L)
                .teacherId(1L)
                .teacherFirstName("John")
                .teacherLastName("Doe")
                .teacherEmail("john.doe@example.com")
                .yearId(1L)
                .yearName("2024/2025")
                .formToken("unique-token-123")
                .submittedAt(now)
                .isProcessed(false)
                .build();
    }

    static TeacherFormSubmissionCreateDto createDto(
            long teacherId, long yearId, String formToken, LocalDateTime submittedAt
    ) {
        TeacherFormSubmissionCreateDto dto = new TeacherFormSubmissionCreateDto();
        dto.setTeacherId(teacherId);
        dto.setYearId(yearId);
        dto.setFormToken(formToken);
        dto.setSubmittedAt(submittedAt);
        return dto;
    }
}
