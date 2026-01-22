package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.dto.teacher.BulkImportResponseDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherCreateDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherResponseDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherUpdateDto;
import de.unipassau.allocationsystem.entity.AuditLog;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.TeacherMapper;
import de.unipassau.allocationsystem.repository.SchoolRepository;
import de.unipassau.allocationsystem.repository.SubjectRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import de.unipassau.allocationsystem.utils.PaginationUtils;
import de.unipassau.allocationsystem.utils.SearchSpecificationUtils;
import de.unipassau.allocationsystem.utils.SortFieldUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing teacher entities.
 * <p>
 * Handles CRUD operations, employment status updates, and delegates bulk import to {@link TeacherImportService}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TeacherService implements CrudService<TeacherResponseDto, Long> {

    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final SchoolRepository schoolRepository;
    private final TeacherMapper teacherMapper;

    /**
     * Bulk import implementation is moved to a dedicated service to keep this class below the file-size threshold.
     */
    private final TeacherImportService teacherImportService;

    /**
     * Loads subjects by IDs. If any ID does not exist, throws {@link ResourceNotFoundException}.
     *
     * @param subjectIds list of subject IDs (nullable)
     * @return resolved subject set (never null)
     */
    Set<Subject> resolveSubjects(List<Long> subjectIds) {
        if (subjectIds == null || subjectIds.isEmpty()) {
            return Set.of();
        }
        Set<Subject> subjects = new HashSet<>(subjectRepository.findAllById(subjectIds));
        if (subjects.size() != subjectIds.size()) {
            throw new ResourceNotFoundException("One or more subjects not found");
        }
        return subjects;
    }

    @Override
    public List<Map<String, String>> getSortFields() {
        return SortFieldUtils.getSortFields("id", "firstName", "lastName", "email", "createdAt", "updatedAt");
    }

    /**
     * Returns sortable field keys exposed by {@link #getSortFields()}.
     */
    public List<String> getSortFieldKeys() {
        return getSortFields().stream().map(f -> f.get("key")).toList();
    }

    @Override
    public boolean existsById(Long id) {
        return teacherRepository.existsById(id);
    }

    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.TEACHER,
            description = "Viewed list of teachers",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Map<String, Object> getPaginated(Map<String, String> queryParams, String searchValue) {
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        Pageable pageable = PageRequest.of(params.page() - 1, params.pageSize(), sort);

        Specification<Teacher> spec = buildFilterSpecification(queryParams, searchValue);
        Page<Teacher> page = teacherRepository.findAll(spec, pageable);
        return PaginationUtils.formatPaginationResponse(page);
    }

    private Specification<Teacher> buildFilterSpecification(Map<String, String> queryParams, String searchValue) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchValue != null && !searchValue.trim().isEmpty()) {
                Specification<Teacher> searchSpec = SearchSpecificationUtils.buildMultiFieldLikeSpecification(
                        new String[]{"firstName", "lastName", "email"}, searchValue
                );
                predicates.add(searchSpec.toPredicate(root, query, cb));
            }

            parseLong(queryParams.get("schoolId"))
                    .ifPresent(id -> predicates.add(cb.equal(root.get("school").get("id"), id)));

            parseEmploymentStatus(queryParams.get("employmentStatus"))
                    .ifPresent(s -> predicates.add(cb.equal(root.get("employmentStatus"), s)));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Optional<Long> parseLong(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.parseLong(raw.trim()));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    private Optional<Teacher.EmploymentStatus> parseEmploymentStatus(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return Optional.empty();
        }
        String normalized = raw.trim().toUpperCase();
        for (Teacher.EmploymentStatus s : Teacher.EmploymentStatus.values()) {
            if (s.name().equals(normalized)) {
                return Optional.of(s);
            }
        }
        return Optional.empty();
    }

    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.TEACHER,
            description = "Viewed all teachers",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public List<TeacherResponseDto> getAll() {
        return teacherRepository.findAll().stream()
                .map(teacherMapper::toResponseDto)
                .toList();
    }

    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.TEACHER,
            description = "Viewed teacher by id",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Optional<TeacherResponseDto> getById(Long id) {
        return teacherRepository.findById(id).map(teacherMapper::toResponseDto);
    }

    @Audited(
            action = AuditLog.AuditAction.CREATE,
            entityName = AuditEntityNames.TEACHER,
            description = "Created new teacher",
            captureNewValue = true
    )
    @Override
    public TeacherResponseDto create(TeacherResponseDto dto) {
        throw new UnsupportedOperationException("Use createTeacher with TeacherCreateDto");
    }

    /**
     * Creates a new teacher and attaches subject qualifications.
     */
    @Audited(
            action = AuditLog.AuditAction.CREATE,
            entityName = AuditEntityNames.TEACHER,
            description = "Created new teacher",
            captureNewValue = true
    )
    public TeacherResponseDto createTeacher(TeacherCreateDto createDto) {
        if (teacherRepository.existsByEmail(createDto.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + createDto.getEmail());
        }

        School school = schoolRepository.findById(createDto.getSchoolId())
                .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + createDto.getSchoolId()));

        Teacher teacher = teacherMapper.toEntityCreate(createDto);
        teacher.setSchool(school);
        teacher.setSubjects(resolveSubjects(createDto.getSubjectIds()));

        Teacher saved = teacherRepository.save(teacher);
        return teacherMapper.toResponseDto(saved);
    }

    @Audited(
            action = AuditLog.AuditAction.UPDATE,
            entityName = AuditEntityNames.TEACHER,
            description = "Updated teacher",
            captureNewValue = true
    )
    @Override
    public TeacherResponseDto update(Long id, TeacherResponseDto dto) {
        throw new UnsupportedOperationException("Use updateTeacher with TeacherUpdateDto");
    }

    /**
     * Updates an existing teacher.
     */
    @Audited(
            action = AuditLog.AuditAction.UPDATE,
            entityName = AuditEntityNames.TEACHER,
            description = "Updated teacher",
            captureNewValue = true
    )
    public TeacherResponseDto updateTeacher(Long id, TeacherUpdateDto updateDto) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + id));

        String incomingEmail = updateDto.getEmail();
        if (incomingEmail != null && !incomingEmail.equals(teacher.getEmail())) {
            if (teacherRepository.existsByEmailAndIdNot(incomingEmail, id)) {
                throw new DuplicateResourceException("Email already exists: " + incomingEmail);
            }
        }

        Long incomingSchoolId = updateDto.getSchoolId();
        if (incomingSchoolId != null && !incomingSchoolId.equals(teacher.getSchool().getId())) {
            School newSchool = schoolRepository.findById(incomingSchoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + incomingSchoolId));
            teacher.setSchool(newSchool);
        }

        teacherMapper.updateEntityFromDto(updateDto, teacher);

        if (updateDto.getSubjectIds() != null) {
            teacher.setSubjects(resolveSubjects(updateDto.getSubjectIds()));
        }

        Teacher updated = teacherRepository.save(teacher);
        return teacherMapper.toResponseDto(updated);
    }

    /**
     * Updates a teacher's employment status.
     */
    @Audited(
            action = AuditLog.AuditAction.UPDATE,
            entityName = AuditEntityNames.TEACHER,
            description = "Updated teacher employment status",
            captureNewValue = true
    )
    public TeacherResponseDto updateEmploymentStatus(Long id, Teacher.EmploymentStatus employmentStatus) {
        Teacher existing = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + id));
        existing.setEmploymentStatus(employmentStatus);
        Teacher updated = teacherRepository.save(existing);
        return teacherMapper.toResponseDto(updated);
    }

    /**
     * Deletes a teacher by ID.
     */
    @Audited(
            action = AuditLog.AuditAction.DELETE,
            entityName = AuditEntityNames.TEACHER,
            description = "Deleted teacher",
            captureNewValue = false
    )
    @Override
    public void delete(Long id) {
        if (!teacherRepository.existsById(id)) {
            throw new ResourceNotFoundException("Teacher not found with id: " + id);
        }
        teacherRepository.deleteById(id);
    }

    /**
     * Deletes a teacher by ID (API compatibility method).
     * Does not call {@link #delete(Long)} to avoid transactional self-invocation warnings.
     */
    public void deleteTeacher(Long id) {
        if (!teacherRepository.existsById(id)) {
            throw new ResourceNotFoundException("Teacher not found with id: " + id);
        }
        teacherRepository.deleteById(id);
    }

    /**
     * Finds which emails already exist in the database from an input list.
     */
    @Transactional(readOnly = true)
    public Set<String> findExistingEmails(List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return Set.of();
        }
        Set<String> normalizedEmails = emails.stream()
                .map(e -> e.toLowerCase().trim())
                .collect(Collectors.toSet());
        return teacherRepository.findExistingEmails(normalizedEmails);
    }

    /**
     * Bulk imports teachers from an Excel file.
     * Delegates the heavy logic to {@link TeacherImportService} to keep this file under size threshold.
     */
    @Audited(
            action = AuditLog.AuditAction.IMPORT,
            entityName = AuditEntityNames.TEACHER,
            description = "Bulk imported teachers",
            captureNewValue = false
    )
    public BulkImportResponseDto bulkImportTeachers(MultipartFile file, boolean skipInvalidRows) throws IOException {
        return teacherImportService.bulkImportTeachers(file, skipInvalidRows);
    }
}
