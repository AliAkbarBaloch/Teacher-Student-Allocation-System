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
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
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

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

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
        if (TeacherFormSubmissionSupport.isBlank(frontendUrl)) {
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
            Long teacherId, Long yearId, Boolean isProcessed, Map<String, String> queryParams
    ) {
        log.info("Fetching form submissions with filters - teacherId: {}, yearId: {}, isProcessed: {}",
                teacherId, yearId, isProcessed);

        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);

        Specification<TeacherFormSubmission> spec = (root, query, cb) -> cb.conjunction();
        spec = TeacherFormSubmissionSupport.addTeacherFilter(spec, teacherId);
        spec = TeacherFormSubmissionSupport.addYearFilter(spec, yearId);
        spec = TeacherFormSubmissionSupport.addProcessedFilter(spec, isProcessed);

        Pageable pageable = buildPageable(params);

        Page<TeacherFormSubmission> page = teacherFormSubmissionRepository.findAll(spec, pageable);
        log.info("Found {} submissions (page {} of {})",
                page.getNumberOfElements(), page.getNumber() + 1, page.getTotalPages());

        Page<TeacherFormSubmissionResponseDto> dtoPage = page.map(teacherFormSubmissionMapper::toResponseDto);
        return PaginationUtils.formatPaginationResponse(dtoPage);
    }

    private Pageable buildPageable(PaginationUtils.PaginationParams params) {
        String sortField = "id".equals(params.sortBy()) ? "id" : params.sortBy();
        Sort sort = Sort.by(params.sortOrder(), sortField);
        return PageRequest.of(params.page() - 1, params.pageSize(), sort);
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
    public TeacherFormSubmissionResponseDto createFormSubmission(TeacherFormSubmissionCreateDto createDto) {
        log.info("Creating new form submission for teacher ID: {} and year ID: {}",
                createDto.getTeacherId(), createDto.getYearId());

        TeacherFormSubmissionSupport.validateAndGetTeacher(teacherRepository, createDto.getTeacherId());

        AcademicYear academicYear =
                TeacherFormSubmissionSupport.validateAndGetAcademicYear(academicYearRepository, createDto.getYearId());
        TeacherFormSubmissionSupport.validateAcademicYearNotLocked(academicYear);

        if (teacherFormSubmissionRepository.existsByFormToken(createDto.getFormToken())) {
            throw new DuplicateResourceException("Form token already exists: " + createDto.getFormToken());
        }

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
    public TeacherFormSubmissionResponseDto updateFormSubmissionStatus(
            Long id, TeacherFormSubmissionStatusUpdateDto statusDto
    ) {
        log.info("Updating processing status for submission ID: {} to: {}", id, statusDto.getIsProcessed());

        TeacherFormSubmission submission = teacherFormSubmissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Form submission not found with ID: " + id));

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
    public FormLinkResponseDto generateFormLink(FormLinkGenerateRequestDto request) {
        log.info("Generating form link for teacher ID: {} and year ID: {}",
                request.getTeacherId(), request.getYearId());

        Teacher teacher =
                TeacherFormSubmissionSupport.validateAndGetTeacher(teacherRepository, request.getTeacherId());
        AcademicYear academicYear =
                TeacherFormSubmissionSupport.validateAndGetAcademicYear(academicYearRepository, request.getYearId());
        TeacherFormSubmissionSupport.validateAcademicYearNotLocked(academicYear);

        TeacherFormSubmissionSupport.ensureNoExistingSubmissionForTeacherYear(
                teacherFormSubmissionRepository, request.getTeacherId(), request.getYearId(), log
        );

        String formToken = formTokenService.generateUniqueFormToken(request.getTeacherId(), request.getYearId());

        TeacherFormSubmission saved = TeacherFormSubmissionSupport.createInvitationRecord(
                teacherFormSubmissionRepository, teacher, academicYear, formToken
        );

        entityManager.flush();
        log.info("Created submission record for generated link - ID: {}, token: {}, teacherId: {}, yearId: {}",
                saved.getId(), formToken, request.getTeacherId(), request.getYearId());

        String formUrl = TeacherFormSubmissionSupport.buildFormUrl(frontendUrl, formToken, log);

        log.info("Form link generated successfully - token: {}, URL: {}", formToken, formUrl);

        return TeacherFormSubmissionSupport.buildFormLinkResponse(formToken, formUrl, teacher, academicYear);
    }

    /**
     * Submit a form via public endpoint using form token.
     * This endpoint is public (no authentication required).
     */
    public TeacherFormSubmissionResponseDto submitFormByToken(String formToken, PublicFormSubmissionDto submissionDto) {
        log.info("Processing form submission for token: {}", formToken);

        TeacherFormSubmissionSupport.validateTokenNotBlank(formToken);

        Optional<TeacherFormSubmission> existingOpt = teacherFormSubmissionRepository.findByFormToken(formToken);
        if (existingOpt.isPresent()) {
            return submitIntoExistingRecord(formToken, existingOpt.get(), submissionDto);
        }

        return submitAsNewRecord(formToken, submissionDto);
    }

    private TeacherFormSubmissionResponseDto submitIntoExistingRecord(
            String formToken, TeacherFormSubmission existing, PublicFormSubmissionDto submissionDto
    ) {
        if (existing.getSubmittedAt() != null) {
            throw new DuplicateResourceException("Form has already been submitted with this token");
        }

        TeacherFormSubmissionSupport.validateSubjectIds(subjectRepository, submissionDto.getSubjectIds());

        existing.setSubmittedAt(LocalDateTime.now());
        TeacherFormSubmissionSupport.populateSubmissionFields(existing, submissionDto);
        existing.setIsProcessed(false);

        TeacherFormSubmission saved = teacherFormSubmissionRepository.save(existing);
        log.info("Form submission updated via token - ID: {}, token: {}", saved.getId(), formToken);
        return teacherFormSubmissionMapper.toResponseDto(saved);
    }

    private TeacherFormSubmissionResponseDto submitAsNewRecord(String formToken, PublicFormSubmissionDto submissionDto) {
        FormTokenService.TokenData tokenData = formTokenService.decodeFormToken(formToken);

        Teacher teacher =
                TeacherFormSubmissionSupport.validateAndGetTeacher(teacherRepository, tokenData.getTeacherId());
        AcademicYear academicYear =
                TeacherFormSubmissionSupport.validateAndGetAcademicYear(academicYearRepository, tokenData.getYearId());
        TeacherFormSubmissionSupport.validateAcademicYearNotLocked(academicYear);

        TeacherFormSubmissionSupport.validateSubjectIds(subjectRepository, submissionDto.getSubjectIds());

        TeacherFormSubmission submission = new TeacherFormSubmission();
        submission.setTeacher(teacher);
        submission.setAcademicYear(academicYear);
        submission.setFormToken(formToken);
        submission.setSubmittedAt(LocalDateTime.now());
        TeacherFormSubmissionSupport.populateSubmissionFields(submission, submissionDto);
        submission.setIsProcessed(false);

        TeacherFormSubmission saved = teacherFormSubmissionRepository.save(submission);
        log.info("Form submission created successfully via token - ID: {}, token: {}", saved.getId(), formToken);
        return teacherFormSubmissionMapper.toResponseDto(saved);
    }

    /**
     * Get form details by token (for public form page).
     * Returns teacher and year info so the form can be pre-filled.
     */
    @Transactional(readOnly = true)
    public FormLinkResponseDto getFormDetailsByToken(String formToken) {
        log.info("Fetching form details for token: {}", formToken);

        TeacherFormSubmissionSupport.validateTokenNotBlank(formToken);
        TeacherFormSubmissionSupport.ensureNotSubmitted(teacherFormSubmissionRepository, formToken);

        FormTokenService.TokenData tokenData = formTokenService.decodeFormToken(formToken);
        Teacher teacher =
                TeacherFormSubmissionSupport.validateAndGetTeacher(teacherRepository, tokenData.getTeacherId());
        AcademicYear academicYear =
                TeacherFormSubmissionSupport.validateAndGetAcademicYear(academicYearRepository, tokenData.getYearId());

        String formUrl = TeacherFormSubmissionSupport.buildFormUrl(frontendUrl, formToken, log);
        return TeacherFormSubmissionSupport.buildFormLinkResponse(formToken, formUrl, teacher, academicYear);
    }
}
