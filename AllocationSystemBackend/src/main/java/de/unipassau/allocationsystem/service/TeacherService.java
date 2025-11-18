package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.dto.teacher.TeacherCreateDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherResponseDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherUpdateDto;
import de.unipassau.allocationsystem.entity.AuditLog;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.Teacher.EmploymentStatus;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.TeacherMapper;
import de.unipassau.allocationsystem.repository.SchoolRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import de.unipassau.allocationsystem.utils.PaginationUtils;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing teachers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TeacherService implements CrudService<TeacherResponseDto, Long>{

    private final TeacherRepository teacherRepository;
    private final SchoolRepository schoolRepository;
    private final TeacherMapper teacherMapper;

    @Override
    public List<Map<String, String>> getSortFields() {
        List<Map<String, String>> fields = new ArrayList<>();
        fields.add(Map.of("key", "id", "label", "ID"));
        fields.add(Map.of("key", "firstName", "label", "First Name"));
        fields.add(Map.of("key", "lastName", "label", "Last Name"));
        fields.add(Map.of("key", "email", "label", "Email"));
        fields.add(Map.of("key", "createdAt", "label", "Creation Date"));
        fields.add(Map.of("key", "updatedAt", "label", "Last Updated"));
        return fields;
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
        log.debug("Getting all teachers with filters: {}", queryParams);

        // Validate pagination parameters
        PaginationUtils.validatePaginationParams(queryParams);

        // Extract parameters
        String schoolIdStr = queryParams.get("schoolId");
        String employmentStatusStr = queryParams.get("employmentStatus");
        String isActiveStr = queryParams.get("isActive");
        String searchTerm = queryParams.get("search");

        // Build specification
        Specification<Teacher> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by school
            if (schoolIdStr != null && !schoolIdStr.isBlank()) {
                try {
                    Long schoolId = Long.parseLong(schoolIdStr);
                    predicates.add(cb.equal(root.get("school").get("id"), schoolId));
                } catch (NumberFormatException e) {
                    log.warn("Invalid schoolId parameter: {}", schoolIdStr);
                }
            }

            // Filter by employment status
            if (employmentStatusStr != null && !employmentStatusStr.isBlank()) {
                try {
                    EmploymentStatus status = EmploymentStatus.valueOf(employmentStatusStr.toUpperCase());
                    predicates.add(cb.equal(root.get("employmentStatus"), status));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid employmentStatus parameter: {}", employmentStatusStr);
                }
            }

            // Filter by active status
            if (isActiveStr != null && !isActiveStr.isBlank()) {
                Boolean isActive = Boolean.parseBoolean(isActiveStr);
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            // Search by name or email
            if (searchTerm != null && !searchTerm.isBlank()) {
                String likePattern = "%" + searchTerm.toLowerCase() + "%";
                Predicate firstNameLike = cb.like(cb.lower(root.get("firstName")), likePattern);
                Predicate lastNameLike = cb.like(cb.lower(root.get("lastName")), likePattern);
                Predicate emailLike = cb.like(cb.lower(root.get("email")), likePattern);
                predicates.add(cb.or(firstNameLike, lastNameLike, emailLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // Create pageable
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Pageable pageable = PageRequest.of(
                params.page() - 1, // Convert to 0-based
                params.pageSize(),
                Sort.by(params.sortOrder(), params.sortBy())
        );

        // Execute query
        Page<Teacher> page = teacherRepository.findAll(spec, pageable);

        // Convert to DTOs
        List<TeacherResponseDto> items = page.getContent().stream()
                .map(teacherMapper::toResponseDto)
                .toList();

        return PaginationUtils.formatPaginationResponse(page);
    }

    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.TEACHER,
            description = "Viewed teacher details",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Optional<TeacherResponseDto> getById(Long id) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + id));
        return Optional.of(teacherMapper.toResponseDto(teacher));
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

    /**
     * Get all teachers with filtering and pagination.
     */
    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.TEACHER,
            description = "Viewed list of teachers",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    public Map<String, Object> getAllTeachers(Map<String, String> queryParams) {
        log.debug("Getting all teachers with filters: {}", queryParams);

        // Validate pagination parameters
        PaginationUtils.validatePaginationParams(queryParams);

        // Extract parameters
        String schoolIdStr = queryParams.get("schoolId");
        String employmentStatusStr = queryParams.get("employmentStatus");
        String isActiveStr = queryParams.get("isActive");
        String searchTerm = queryParams.get("search");

        // Build specification
        Specification<Teacher> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by school
            if (schoolIdStr != null && !schoolIdStr.isBlank()) {
                try {
                    Long schoolId = Long.parseLong(schoolIdStr);
                    predicates.add(cb.equal(root.get("school").get("id"), schoolId));
                } catch (NumberFormatException e) {
                    log.warn("Invalid schoolId parameter: {}", schoolIdStr);
                }
            }

            // Filter by employment status
            if (employmentStatusStr != null && !employmentStatusStr.isBlank()) {
                try {
                    EmploymentStatus status = EmploymentStatus.valueOf(employmentStatusStr.toUpperCase());
                    predicates.add(cb.equal(root.get("employmentStatus"), status));
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid employmentStatus parameter: {}", employmentStatusStr);
                }
            }

            // Filter by active status
            if (isActiveStr != null && !isActiveStr.isBlank()) {
                Boolean isActive = Boolean.parseBoolean(isActiveStr);
                predicates.add(cb.equal(root.get("isActive"), isActive));
            }

            // Search by name or email
            if (searchTerm != null && !searchTerm.isBlank()) {
                String likePattern = "%" + searchTerm.toLowerCase() + "%";
                Predicate firstNameLike = cb.like(cb.lower(root.get("firstName")), likePattern);
                Predicate lastNameLike = cb.like(cb.lower(root.get("lastName")), likePattern);
                Predicate emailLike = cb.like(cb.lower(root.get("email")), likePattern);
                predicates.add(cb.or(firstNameLike, lastNameLike, emailLike));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // Create pageable
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Pageable pageable = PageRequest.of(
                params.page() - 1, // Convert to 0-based
                params.pageSize(),
                Sort.by(params.sortOrder(), params.sortBy())
        );

        // Execute query
        Page<Teacher> page = teacherRepository.findAll(spec, pageable);

        // Convert to DTOs
        List<TeacherResponseDto> items = page.getContent().stream()
                .map(teacherMapper::toResponseDto)
                .toList();

        // Return paginated response
        return PaginationUtils.formatPaginationResponse(page);
    }

    @Audited(
            action = AuditLog.AuditAction.CREATE,
            entityName = AuditEntityNames.TEACHER,
            description = "Created new teacher",
            captureNewValue = true
    )
    @Transactional
    @Override
    public TeacherResponseDto create(TeacherResponseDto dto) {
        // You may adapt this to use DTO → Teacher mapping if needed
        throw new UnsupportedOperationException("Use createTeacher with TeacherCreateDto");
    }

    /**
     * Create a new teacher.
     */
    @Audited(
        action = AuditLog.AuditAction.CREATE,
        entityName = AuditEntityNames.TEACHER,
        description = "Created new teacher",
        captureNewValue = true
    )
    @Transactional
    public TeacherResponseDto createTeacher(TeacherCreateDto createDto) {
        log.info("Creating new teacher with email: {}", createDto.getEmail());

        // Validate email uniqueness
        if (teacherRepository.existsByEmail(createDto.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + createDto.getEmail());
        }

        // Validate school exists and is active
        School school = schoolRepository.findById(createDto.getSchoolId())
                .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + createDto.getSchoolId()));

        if (!school.getIsActive()) {
            throw new IllegalArgumentException("Cannot assign teacher to inactive school: " + school.getSchoolName());
        }

        // Validate part-time consistency
        validatePartTimeConsistency(createDto.getIsPartTime(), createDto.getEmploymentStatus());

        // Create teacher
        Teacher teacher = new Teacher();
        teacher.setSchool(school);
        teacher.setFirstName(createDto.getFirstName());
        teacher.setLastName(createDto.getLastName());
        teacher.setEmail(createDto.getEmail());
        teacher.setPhone(createDto.getPhone());
        teacher.setIsPartTime(createDto.getIsPartTime());
        teacher.setEmploymentStatus(createDto.getEmploymentStatus());
        teacher.setUsageCycle(createDto.getUsageCycle());

        Teacher saved = teacherRepository.save(teacher);

        log.info("Teacher created successfully with ID: {}", saved.getId());
        return teacherMapper.toResponseDto(saved);
    }

    /**
     * Update an existing teacher.
     */
    @Audited(
        action = AuditLog.AuditAction.UPDATE,
        entityName = AuditEntityNames.TEACHER,
        description = "Updated teacher information",
        captureNewValue = true
    )
    @Transactional
    public TeacherResponseDto updateTeacher(Long id, TeacherUpdateDto updateDto) {
        log.info("Updating teacher with ID: {}", id);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + id));

        // Validate email uniqueness if changed
        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(teacher.getEmail())) {
            if (teacherRepository.existsByEmailAndIdNot(updateDto.getEmail(), id)) {
                throw new DuplicateResourceException("Email already exists: " + updateDto.getEmail());
            }
        }

        // Validate school if changed
        if (updateDto.getSchoolId() != null && !updateDto.getSchoolId().equals(teacher.getSchool().getId())) {
            School newSchool = schoolRepository.findById(updateDto.getSchoolId())
                    .orElseThrow(() -> new ResourceNotFoundException("School not found with ID: " + updateDto.getSchoolId()));

            if (!newSchool.getIsActive()) {
                throw new IllegalArgumentException("Cannot assign teacher to inactive school: " + newSchool.getSchoolName());
            }
            teacher.setSchool(newSchool);
        }

        // Validate part-time consistency
        Boolean isPartTime = updateDto.getIsPartTime() != null ? updateDto.getIsPartTime() : teacher.getIsPartTime();
        EmploymentStatus status = updateDto.getEmploymentStatus() != null ? updateDto.getEmploymentStatus() : teacher.getEmploymentStatus();
        validatePartTimeConsistency(isPartTime, status);

        // Update fields
        teacherMapper.updateEntityFromDto(updateDto, teacher);

        Teacher updated = teacherRepository.save(teacher);

        log.info("Teacher updated successfully with ID: {}", id);
        return teacherMapper.toResponseDto(updated);
    }

    @Audited(
            action = AuditLog.AuditAction.UPDATE,
            entityName = AuditEntityNames.TEACHER,
            description = "Updated teacher information",
            captureNewValue = true
    )
    @Transactional
    @Override
    public TeacherResponseDto update(Long id, TeacherResponseDto dto) {
        throw new UnsupportedOperationException("Use updateTeacher with TeacherUpdateDto");
    }

    /**
     * Update teacher status (activate/deactivate).
     */
    @Audited(
        action = AuditLog.AuditAction.UPDATE,
        entityName = AuditEntityNames.TEACHER,
        description = "Updated teacher status",
        captureNewValue = true
    )
    @Transactional
    public TeacherResponseDto updateTeacherStatus(Long id, Boolean isActive) {
        log.info("Updating teacher status for ID: {} to {}", id, isActive);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + id));

        // Check if teacher has active assignments (future implementation)
        if (!isActive) {
            // NOTE: Check if teacher has assignments in current plan should be implemented
            // For now, just log a warning
            log.warn("Deactivating teacher with ID: {}. Check for active assignments should be implemented.", id);
        }

        teacher.setIsActive(isActive);

        Teacher updated = teacherRepository.save(teacher);

        log.info("Teacher status updated successfully for ID: {}", id);
        return teacherMapper.toResponseDto(updated);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        deleteTeacher(id);
    }

    /**
     * Soft delete teacher (deactivate).
     */
    @Audited(
        action = AuditLog.AuditAction.DELETE,
        entityName = AuditEntityNames.TEACHER,
        description = "Soft deleted teacher (deactivated)",
        captureNewValue = false
    )
    @Transactional
    public void deleteTeacher(Long id) {
        log.info("Soft deleting teacher with ID: {}", id);

        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + id));

        // Check if teacher has active assignments (future implementation)
        // NOTE: Implement check for active assignments

        teacher.setIsActive(false);
        teacherRepository.save(teacher);

        log.info("Teacher soft deleted successfully with ID: {}", id);
    }

    /**
     * Validate part-time consistency with employment status.
     */
    private void validatePartTimeConsistency(Boolean isPartTime, EmploymentStatus employmentStatus) {
        if (isPartTime && employmentStatus == EmploymentStatus.FULL_TIME) {
            throw new IllegalArgumentException("Part-time flag cannot be true for FULL_TIME employment status");
        }
        if (!isPartTime && employmentStatus == EmploymentStatus.PART_TIME) {
            throw new IllegalArgumentException("Part-time flag must be true for PART_TIME employment status");
        }
    }

}
