package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.dto.teacher.formsubmission.FormLinkResponseDto;
import de.unipassau.allocationsystem.dto.teacher.formsubmission.PublicFormSubmissionDto;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.TeacherFormSubmission;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.SubjectRepository;
import de.unipassau.allocationsystem.repository.TeacherFormSubmissionRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import org.slf4j.Logger;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Helper utilities for {@link TeacherFormSubmissionService}.
 * <p>
 * Extracted to reduce service file size while keeping business logic unchanged.
 * Package-private by design (same package usage only).
 */
final class TeacherFormSubmissionSupport {

    private TeacherFormSubmissionSupport() {
        // utility
    }

    /**
     * Null/blank string check.
     */
    static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Adds an optional teacher filter to the specification.
     */
    static Specification<TeacherFormSubmission> addTeacherFilter(
            Specification<TeacherFormSubmission> spec,
            Long teacherId
    ) {
        if (teacherId == null) {
            return spec;
        }
        return spec.and((root, query, cb) -> cb.equal(root.get("teacher").get("id"), teacherId));
    }

    /**
     * Adds an optional academic year filter to the specification.
     */
    static Specification<TeacherFormSubmission> addYearFilter(
            Specification<TeacherFormSubmission> spec,
            Long yearId
    ) {
        if (yearId == null) {
            return spec;
        }
        return spec.and((root, query, cb) -> cb.equal(root.get("academicYear").get("id"), yearId));
    }

    /**
     * Adds an optional processed-status filter to the specification.
     */
    static Specification<TeacherFormSubmission> addProcessedFilter(
            Specification<TeacherFormSubmission> spec,
            Boolean isProcessed
    ) {
        if (isProcessed == null) {
            return spec;
        }
        return spec.and((root, query, cb) -> cb.equal(root.get("isProcessed"), isProcessed));
    }

    /**
     * Validates and retrieves teacher by ID.
     */
    static Teacher validateAndGetTeacher(TeacherRepository teacherRepository, Long teacherId) {
        return teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + teacherId));
    }

    /**
     * Validates and retrieves academic year by ID.
     */
    static AcademicYear validateAndGetAcademicYear(AcademicYearRepository academicYearRepository, Long yearId) {
        return academicYearRepository.findById(yearId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with ID: " + yearId));
    }

    /**
     * Validates that the academic year is not locked.
     */
    static void validateAcademicYearNotLocked(AcademicYear academicYear) {
        if (Boolean.TRUE.equals(academicYear.getIsLocked())) {
            throw new IllegalArgumentException(
                    "Cannot perform operation for locked academic year: " + academicYear.getYearName()
            );
        }
    }

    /**
     * Ensures no existing submission exists for the same teacher-year combination.
     */
    static void ensureNoExistingSubmissionForTeacherYear(
            TeacherFormSubmissionRepository repo,
            Long teacherId,
            Long yearId,
            Logger log
    ) {
        boolean exists = repo.existsByTeacherIdAndAcademicYearId(teacherId, yearId);
        if (exists) {
            log.warn("Attempted to generate form link for teacher {} and year {} - submission already exists",
                    teacherId, yearId);
            throw new IllegalArgumentException("A form submission already exists for this teacher and academic year");
        }
    }

    /**
     * Creates and persists an invitation (link generated but not submitted yet).
     */
    static TeacherFormSubmission createInvitationRecord(
            TeacherFormSubmissionRepository repo,
            Teacher teacher,
            AcademicYear academicYear,
            String token
    ) {
        TeacherFormSubmission submission = new TeacherFormSubmission();
        submission.setTeacher(teacher);
        submission.setAcademicYear(academicYear);
        submission.setFormToken(token);
        submission.setSubmittedAt(null);
        submission.setIsProcessed(false);
        return repo.save(submission);
    }

    /**
     * Validates token is not null/blank.
     */
    static void validateTokenNotBlank(String token) {
        if (isBlank(token)) {
            throw new IllegalArgumentException("Invalid form token");
        }
    }

    /**
     * Ensures the token was not already used for a submitted form.
     */
    static void ensureNotSubmitted(TeacherFormSubmissionRepository repo, String token) {
        Optional<TeacherFormSubmission> existingOpt = repo.findByFormToken(token);
        if (existingOpt.isPresent() && existingOpt.get().getSubmittedAt() != null) {
            throw new IllegalArgumentException("This form has already been submitted");
        }
    }

    /**
     * Validate that all subject IDs exist in the database.
     */
    static void validateSubjectIds(SubjectRepository subjectRepository, List<Long> subjectIds) {
        if (subjectIds == null || subjectIds.isEmpty()) {
            return;
        }
        for (Long subjectId : subjectIds) {
            if (!subjectRepository.existsById(subjectId)) {
                throw new ResourceNotFoundException("Subject not found with ID: " + subjectId);
            }
        }
    }

    /**
     * Populate submission entity fields from DTO.
     */
    static void populateSubmissionFields(TeacherFormSubmission submission, PublicFormSubmissionDto dto) {
        submission.setSchoolId(dto.getSchoolId());
        submission.setNotes(dto.getNotes());
        submission.setSubjectIds(convertLongListToString(dto.getSubjectIds()));
        submission.setInternshipCombinations(convertLongListToString(dto.getInternshipTypeIds()));
    }

    /**
     * Convert list of Longs to comma-separated string.
     */
    static String convertLongListToString(List<Long> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    /**
     * Build form URL from token.
     */
    static String buildFormUrl(String frontendUrl, String formToken, Logger log) {
        if (isBlank(frontendUrl)) {
            log.error("Frontend URL is not configured. Please set app.frontend.url in application.properties");
            throw new IllegalStateException(
                    "Frontend URL is not configured. Please set app.frontend.url in application.properties"
            );
        }
        return frontendUrl + "/form/" + formToken;
    }

    /**
     * Build standard form link response DTO.
     */
    static FormLinkResponseDto buildFormLinkResponse(
            String formToken,
            String formUrl,
            Teacher teacher,
            AcademicYear academicYear
    ) {
        return FormLinkResponseDto.builder()
                .formToken(formToken)
                .formUrl(formUrl)
                .teacherId(teacher.getId())
                .teacherName(teacher.getFirstName() + " " + teacher.getLastName())
                .teacherEmail(teacher.getEmail())
                .yearId(academicYear.getId())
                .yearName(academicYear.getYearName())
                .build();
    }
}
