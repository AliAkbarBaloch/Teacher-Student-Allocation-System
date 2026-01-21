package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.dto.teacher.formsubmission.FormLinkGenerateRequestDto;
import de.unipassau.allocationsystem.dto.teacher.formsubmission.FormLinkResponseDto;
import de.unipassau.allocationsystem.dto.teacher.formsubmission.PublicFormSubmissionDto;
import de.unipassau.allocationsystem.dto.teacher.formsubmission.TeacherFormSubmissionCreateDto;
import de.unipassau.allocationsystem.dto.teacher.formsubmission.TeacherFormSubmissionResponseDto;
import de.unipassau.allocationsystem.dto.teacher.formsubmission.TeacherFormSubmissionStatusUpdateDto;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.AuditLog;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.TeacherFormSubmission;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.TeacherFormSubmissionMapper;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.SubjectRepository;
import de.unipassau.allocationsystem.repository.TeacherFormSubmissionRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import de.unipassau.allocationsystem.utils.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing teacher form submissions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TeacherFormSubmissionService {

    private final TeacherFormSubmissionRepository teacherFormSubmissionRepository;
    private final TeacherRepository teacherRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherFormSubmissionMapper teacherFormSubmissionMapper;
    private final FormTokenService formTokenService;
    private final EntityManager entityManager;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Initializes the service and validates frontend URL configuration.
     */
    @PostConstruct
    public void init() {
        log.info("TeacherFormSubmissionService initialized with frontendUrl: {}", frontendUrl);
        if (frontendUrl == null || frontendUrl.trim().isEmpty()) {
            log.warn("Frontend URL is not configured. Using default: http://localhost:5173");
            frontendUrl = "http://localhost:5173";
        }
    }

    /**
     * Get all form submissions with optional filters and pagination.
     */
    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.TEACHER_FORM_SUBMISSION,
            description = "Viewed list of teacher form submissions",
            captureNewValue = false
    )
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
        Page<TeacherFormSubmissionResponseDto> dtoPage = page.map(teacherFormSubmissionMapper::toResponseDto);

        // Return paginated response
        return PaginationUtils.formatPaginationResponse(dtoPage);
    }

    /**
     * Get a specific form submission by ID.
     */
    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.TEACHER_FORM_SUBMISSION,
            description = "Viewed teacher form submission details",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    public TeacherFormSubmissionResponseDto getFormSubmissionById(Long id) {
        log.info("Fetching form submission with ID: {}", id);

        TeacherFormSubmission submission = teacherFormSubmissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Form submission not found with ID: " + id));

        return teacherFormSubmissionMapper.toResponseDto(submission);
    }

    /**
     * Create a new form submission.
     */
    @Audited(
            action = AuditLog.AuditAction.CREATE,
            entityName = AuditEntityNames.TEACHER_FORM_SUBMISSION,
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
        TeacherFormSubmission submission = teacherFormSubmissionMapper.toEntityCreate(createDto);

        TeacherFormSubmission saved = teacherFormSubmissionRepository.save(submission);

        log.info("Form submission created successfully with ID: {}", saved.getId());
        return teacherFormSubmissionMapper.toResponseDto(saved);
    }

    /**
     * Update the processing status of a form submission.
     */
    @Audited(
            action = AuditLog.AuditAction.UPDATE,
            entityName = AuditEntityNames.TEACHER_FORM_SUBMISSION,
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
        return teacherFormSubmissionMapper.toResponseDto(updated);
    }

    /**
     * Generate a unique form token and link for a teacher.
     * This creates a shareable link that teachers can use to submit their form.
     */
    @Audited(
            action = AuditLog.AuditAction.CREATE,
            entityName = AuditEntityNames.TEACHER_FORM_SUBMISSION,
            description = "Generated form link for teacher",
            captureNewValue = false
    )
    @Transactional
    public FormLinkResponseDto generateFormLink(FormLinkGenerateRequestDto request) {
        log.info("Generating form link for teacher ID: {} and year ID: {}",
                request.getTeacherId(), request.getYearId());

        // Validate teacher exists
        Teacher teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + request.getTeacherId()));

        // Validate academic year exists
        AcademicYear academicYear = academicYearRepository.findById(request.getYearId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with ID: " + request.getYearId()));

        // Check if academic year is locked
        if (Boolean.TRUE.equals(academicYear.getIsLocked())) {
            throw new IllegalArgumentException("Cannot generate form link for locked academic year: " + academicYear.getYearName());
        }

        // Check if submission already exists for this SPECIFIC teacher/year combination
        // This check ensures we only prevent duplicates for the SAME teacher AND SAME year
        // The same teacher CAN be invited for different academic years
        boolean existingSubmission = teacherFormSubmissionRepository.existsByTeacherIdAndAcademicYearId(
                request.getTeacherId(), 
                request.getYearId()
        );

        if (existingSubmission) {
            log.warn("Attempted to generate form link for teacher {} and year {} - submission already exists for this teacher/year combination", 
                    request.getTeacherId(), request.getYearId());
            throw new IllegalArgumentException("A form submission already exists for this teacher and academic year");
        }

        // Generate unique form token
        String formToken = formTokenService.generateUniqueFormToken(request.getTeacherId(), request.getYearId());

        // Create a submission record to track that the link has been generated
        // This allows us to show "invited" status in the UI
        TeacherFormSubmission submission = new TeacherFormSubmission();
        submission.setTeacher(teacher);
        submission.setAcademicYear(academicYear);
        submission.setFormToken(formToken);
        submission.setSubmittedAt(null); // Will be set when form is actually submitted
        // Submission data fields will be set when form is actually submitted
        submission.setIsProcessed(false);
        
        TeacherFormSubmission saved = teacherFormSubmissionRepository.save(submission);
        // Flush to ensure the record is persisted immediately
        entityManager.flush();
        log.info("Created submission record for generated link - ID: {}, token: {}, teacherId: {}, yearId: {}", 
                saved.getId(), formToken, request.getTeacherId(), request.getYearId());

        // Build form URL
        String formUrl = buildFormUrl(formToken);

        log.info("Form link generated successfully - token: {}, URL: {}", formToken, formUrl);

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

    /**
     * Submit a form via public endpoint using form token.
     * This endpoint is public (no authentication required).
     */
    @Transactional
    public TeacherFormSubmissionResponseDto submitFormByToken(String formToken, PublicFormSubmissionDto submissionDto) {
        log.info("Processing form submission for token: {}", formToken);

        // Validate token format
        if (formToken == null || formToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid form token");
        }

        // Check if submission already exists for this token
        Optional<TeacherFormSubmission> existingSubmissionOpt = teacherFormSubmissionRepository.findByFormToken(formToken);
        
        if (existingSubmissionOpt.isPresent()) {
            TeacherFormSubmission existing = existingSubmissionOpt.get();
            
            // If submission data already exists (check if submittedAt is set), form has been submitted
            if (existing.getSubmittedAt() != null) {
                throw new DuplicateResourceException("Form has already been submitted with this token");
            }
            
            // Otherwise, update the existing record with submission data
            // Validate subject IDs exist before updating
            validateSubjectIds(submissionDto.getSubjectIds());
            existing.setSubmittedAt(LocalDateTime.now());
            populateSubmissionFields(existing, submissionDto);
            existing.setIsProcessed(false);
            
            TeacherFormSubmission saved = teacherFormSubmissionRepository.save(existing);
            log.info("Form submission updated via token - ID: {}, token: {}", saved.getId(), formToken);
            return teacherFormSubmissionMapper.toResponseDto(saved);
        }

        // Decode token and validate teacher/year
        FormTokenService.TokenData tokenData = formTokenService.decodeFormToken(formToken);
        Teacher teacher = validateAndGetTeacher(tokenData.getTeacherId());
        AcademicYear academicYear = validateAndGetAcademicYear(tokenData.getYearId());
        validateAcademicYearNotLocked(academicYear);
        
        // Validate subject IDs exist
        validateSubjectIds(submissionDto.getSubjectIds());

        // Fallback: Create new submission if record doesn't exist (shouldn't happen if link was generated properly)
        // This handles edge cases where a token might be manually created
        TeacherFormSubmission submission = new TeacherFormSubmission();
        submission.setTeacher(teacher);
        submission.setAcademicYear(academicYear);
        submission.setFormToken(formToken);
        submission.setSubmittedAt(LocalDateTime.now());
        populateSubmissionFields(submission, submissionDto);
        submission.setIsProcessed(false);

        TeacherFormSubmission saved = teacherFormSubmissionRepository.save(submission);

        log.info("Form submission created successfully via token - ID: {}, token: {}", saved.getId(), formToken);
        return teacherFormSubmissionMapper.toResponseDto(saved);
    }

    /**
     * Get form details by token (for public form page).
     * Returns teacher and year info so the form can be pre-filled.
     * Decodes token to get teacher/year info without requiring a database lookup.
     */
    @Transactional(readOnly = true)
    public FormLinkResponseDto getFormDetailsByToken(String formToken) {
        log.info("Fetching form details for token: {}", formToken);

        // Check if submission already exists and has been submitted
        Optional<TeacherFormSubmission> existingOpt = teacherFormSubmissionRepository.findByFormToken(formToken);
        if (existingOpt.isPresent()) {
            TeacherFormSubmission existing = existingOpt.get();
            // Only throw error if form has actually been submitted (submittedAt is not null)
            if (existing.getSubmittedAt() != null) {
                throw new IllegalArgumentException("This form has already been submitted");
            }
            // If record exists but submittedAt is null, it means link was generated but not submitted yet
            // Continue to decode token and return form details
        }

        // Decode token and validate teacher/year
        FormTokenService.TokenData tokenData = formTokenService.decodeFormToken(formToken);
        Teacher teacher = validateAndGetTeacher(tokenData.getTeacherId());
        AcademicYear academicYear = validateAndGetAcademicYear(tokenData.getYearId());
        
        // Build form URL
        String formUrl = buildFormUrl(formToken);

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

    // ==================== Helper Methods ====================


    /**
     * Validate and retrieve teacher by ID.
     */
    private Teacher validateAndGetTeacher(Long teacherId) {
        return teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + teacherId));
    }

    /**
     * Validate and retrieve academic year by ID.
     */
    private AcademicYear validateAndGetAcademicYear(Long yearId) {
        return academicYearRepository.findById(yearId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with ID: " + yearId));
    }

    /**
     * Validate that academic year is not locked.
     */
    private void validateAcademicYearNotLocked(AcademicYear academicYear) {
        if (Boolean.TRUE.equals(academicYear.getIsLocked())) {
            throw new IllegalArgumentException("Cannot perform operation for locked academic year: " + academicYear.getYearName());
        }
    }

    /**
     * Validate that all subject IDs exist in the database.
     */
    private void validateSubjectIds(List<Long> subjectIds) {
        if (subjectIds == null || subjectIds.isEmpty()) {
            return; // Validation annotations handle empty list
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
    private void populateSubmissionFields(TeacherFormSubmission submission, PublicFormSubmissionDto dto) {
        submission.setSchoolId(dto.getSchoolId());
        submission.setNotes(dto.getNotes());
        submission.setSubjectIds(convertLongListToString(dto.getSubjectIds()));
        // Store internship type IDs in internshipCombinations field as comma-separated string
        submission.setInternshipCombinations(convertLongListToString(dto.getInternshipTypeIds()));
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
     * Build form URL from token.
     */
    private String buildFormUrl(String formToken) {
        if (frontendUrl == null || frontendUrl.trim().isEmpty()) {
            log.error("Frontend URL is not configured. Please set app.frontend.url in application.properties");
            throw new IllegalStateException("Frontend URL is not configured. Please set app.frontend.url in application.properties");
        }
        return frontendUrl + "/form/" + formToken;
    }

}
