package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.dto.TeacherFormSubmissionCreateDto;
import de.unipassau.allocationsystem.dto.TeacherFormSubmissionResponseDto;
import de.unipassau.allocationsystem.dto.TeacherFormSubmissionStatusUpdateDto;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.AuditLog;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.TeacherFormSubmission;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.TeacherFormSubmissionMapper;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.TeacherFormSubmissionRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import de.unipassau.allocationsystem.utils.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Service for managing teacher form submissions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherFormSubmissionService {

    private final TeacherFormSubmissionRepository teacherFormSubmissionRepository;
    private final TeacherRepository teacherRepository;
    private final AcademicYearRepository academicYearRepository;
    private final TeacherFormSubmissionMapper teacherFormSubmissionMapper;

    /**
     * Get all form submissions with optional filters and pagination.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getFormSubmissions(
            Long teacherId, Long yearId, Boolean isProcessed, Map<String, String> queryParams) {
        log.info("Fetching form submissions with filters - teacherId: {}, yearId: {}, isProcessed: {}",
                teacherId, yearId, isProcessed);

        // Validate and extract pagination parameters
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);

        // Build specification for filtering
        Specification<TeacherFormSubmission> spec = (root, query, cb) -> cb.conjunction();

        // Optional filter by teacher
        if (teacherId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("teacher").get("id"), teacherId));
        }

        // Optional filter by academic year
        if (yearId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("academicYear").get("id"), yearId));
        }

        // Optional filter by processing status
        if (isProcessed != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("isProcessed"), isProcessed));
        }

        // Create pageable with sorting
        String sortField = "id".equals(params.sortBy()) ? "id" : params.sortBy();
        Sort.Direction direction = params.sortOrder();
        Sort sort = Sort.by(direction, sortField);
        Pageable pageable = PageRequest.of(params.page() - 1, params.pageSize(), sort);

        // Fetch paginated results
        Page<TeacherFormSubmission> page = teacherFormSubmissionRepository.findAll(spec, pageable);
        log.info("Found {} submissions (page {} of {})",
                page.getNumberOfElements(), page.getNumber() + 1, page.getTotalPages());

        // Convert to DTOs
        Page<TeacherFormSubmissionResponseDto> dtoPage = page.map(teacherFormSubmissionMapper::toDto);

        // Return paginated response
        return PaginationUtils.formatPaginationResponse(dtoPage);
    }

    /**
     * Get a specific form submission by ID.
     */
    @Transactional(readOnly = true)
    public TeacherFormSubmissionResponseDto getFormSubmissionById(Long id) {
        log.info("Fetching form submission with ID: {}", id);

        TeacherFormSubmission submission = teacherFormSubmissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Form submission not found with ID: " + id));

        return teacherFormSubmissionMapper.toDto(submission);
    }

    /**
     * Create a new form submission.
     */
    @Audited(
        action = AuditLog.AuditAction.CREATE,
        entityName = "TEACHER_FORM_SUBMISSION",
        description = "Created new teacher form submission",
        captureNewValue = true
    )
    @Transactional
    public TeacherFormSubmissionResponseDto createFormSubmission(TeacherFormSubmissionCreateDto createDto) {
        log.info("Creating new form submission for teacher ID: {} and year ID: {}",
                createDto.getTeacherId(), createDto.getYearId());

        // Validate teacher exists
        Teacher teacher = teacherRepository.findById(createDto.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + createDto.getTeacherId()));

        // Validate academic year exists
        AcademicYear academicYear = academicYearRepository.findById(createDto.getYearId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with ID: " + createDto.getYearId()));

        // Check if academic year is locked (optional business rule)
        if (Boolean.TRUE.equals(academicYear.getIsLocked())) {
            throw new IllegalArgumentException("Cannot create submission for locked academic year: " + academicYear.getYearName());
        }

        // Check if form token already exists
        if (teacherFormSubmissionRepository.existsByFormToken(createDto.getFormToken())) {
            throw new DuplicateResourceException("Form token already exists: " + createDto.getFormToken());
        }

        // Create submission
        TeacherFormSubmission submission = new TeacherFormSubmission();
        submission.setTeacher(teacher);
        submission.setAcademicYear(academicYear);
        submission.setFormToken(createDto.getFormToken());
        submission.setSubmittedAt(createDto.getSubmittedAt());
        submission.setSubmissionData(createDto.getSubmissionData());
        submission.setIsProcessed(false);

        TeacherFormSubmission saved = teacherFormSubmissionRepository.save(submission);

        log.info("Form submission created successfully with ID: {}", saved.getId());
        return teacherFormSubmissionMapper.toDto(saved);
    }

    /**
     * Update the processing status of a form submission.
     */
    @Audited(
        action = AuditLog.AuditAction.UPDATE,
        entityName = "TEACHER_FORM_SUBMISSION",
        description = "Updated teacher form submission processing status",
        captureNewValue = true
    )
    @Transactional
    public TeacherFormSubmissionResponseDto updateFormSubmissionStatus(Long id, TeacherFormSubmissionStatusUpdateDto statusDto) {
        log.info("Updating processing status for submission ID: {} to: {}", id, statusDto.getIsProcessed());

        TeacherFormSubmission submission = teacherFormSubmissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Form submission not found with ID: " + id));

        // Update status
        submission.setIsProcessed(statusDto.getIsProcessed());
        TeacherFormSubmission updated = teacherFormSubmissionRepository.save(submission);

        log.info("Form submission status updated successfully for ID: {}", id);
        return teacherFormSubmissionMapper.toDto(updated);
    }
}
