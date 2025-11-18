package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.dto.TeacherAvailabilityCreateDto;
import de.unipassau.allocationsystem.dto.TeacherAvailabilityResponseDto;
import de.unipassau.allocationsystem.dto.TeacherAvailabilityUpdateDto;
import de.unipassau.allocationsystem.entity.*;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.mapper.TeacherAvailabilityMapper;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.InternshipTypeRepository;
import de.unipassau.allocationsystem.repository.TeacherAvailabilityRepository;
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
 * Service for managing teacher availability records.
 * Includes validation, uniqueness checks, and audit logging.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TeacherAvailabilityService {

    private final TeacherAvailabilityRepository teacherAvailabilityRepository;
    private final TeacherRepository teacherRepository;
    private final AcademicYearRepository academicYearRepository;
    private final InternshipTypeRepository internshipTypeRepository;
    private final TeacherAvailabilityMapper teacherAvailabilityMapper;

    /**
     * Get all availability entries for a teacher with optional filters and pagination.
     */
    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.TEACHER_AVAILABILITY,
            description = "Viewed list of teacher availability entries",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    public Map<String, Object> getTeacherAvailability(
            Long teacherId, Long yearId, Long internshipTypeId, Map<String, String> queryParams) {
        log.info("Fetching teacher availability for teacher ID: {}", teacherId);

        // Validate teacher exists
        if (!teacherRepository.existsById(teacherId)) {
            throw new ResourceNotFoundException("Teacher not found with ID: " + teacherId);
        }

        // Validate and extract pagination parameters
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);

        // Build specification for filtering
        Specification<TeacherAvailability> spec = (root, query, cb) -> 
            cb.equal(root.get("teacher").get("id"), teacherId);

        // Optional filter by year
        if (yearId != null) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("academicYear").get("id"), yearId));
        }

        // Optional filter by internship type
        if (internshipTypeId != null) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("internshipType").get("id"), internshipTypeId));
        }

        // Create pageable with sorting (page is 1-based in params, 0-based in PageRequest)
        // Map "id" to actual ID field name "availabilityId"
        String sortField = "id".equals(params.sortBy()) ? "availabilityId" : params.sortBy();
        Sort sort = Sort.by(params.sortOrder(), sortField);
        Pageable pageable = PageRequest.of(params.page() - 1, params.pageSize(), sort);

        // Fetch paginated results
        Page<TeacherAvailability> page = teacherAvailabilityRepository.findAll(spec, pageable);
        log.info("Found {} availability entries (page {} of {})", 
                page.getNumberOfElements(), page.getNumber() + 1, page.getTotalPages());

        // Convert to DTOs
        Page<TeacherAvailabilityResponseDto> dtoPage = page.map(teacherAvailabilityMapper::toDto);

        // Return paginated response
        return PaginationUtils.formatPaginationResponse(dtoPage);
    }

    /**
     * Get a specific availability entry by ID.
     */
    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.TEACHER_AVAILABILITY,
            description = "Viewed teacher availability entry details",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    public TeacherAvailabilityResponseDto getAvailabilityById(Long teacherId, Long availabilityId) {
        log.info("Fetching availability ID: {} for teacher ID: {}", availabilityId, teacherId);

        TeacherAvailability availability = teacherAvailabilityRepository
                .findByAvailabilityIdAndTeacherId(availabilityId, teacherId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Availability entry not found with ID: " + availabilityId + " for teacher ID: " + teacherId));

        return teacherAvailabilityMapper.toDto(availability);
    }

    /**
     * Create a new availability entry.
     */
    @Audited(
            action = AuditLog.AuditAction.CREATE,
            entityName = AuditEntityNames.TEACHER_AVAILABILITY,
            description = "Created new teacher availability entry",
            captureNewValue = true
    )
    @Transactional
    public TeacherAvailabilityResponseDto createAvailability(Long teacherId, TeacherAvailabilityCreateDto createDto) {
        log.info("Creating availability entry for teacher ID: {}", teacherId);

        // Validate teacherId matches
        if (!teacherId.equals(createDto.getTeacherId())) {
            throw new IllegalArgumentException("Teacher ID in path and request body must match");
        }

        // Validate teacher exists and is active
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with ID: " + teacherId));

        if (!teacher.getIsActive()) {
            throw new IllegalArgumentException("Cannot create availability for inactive teacher: " +
                    teacher.getFirstName() + " " + teacher.getLastName());
        }

        // Validate academic year exists
        AcademicYear academicYear = academicYearRepository.findById(createDto.getYearId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with ID: " + createDto.getYearId()));

        // Validate internship type exists
        InternshipType internshipType = internshipTypeRepository.findById(createDto.getInternshipTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Internship type not found with ID: " + createDto.getInternshipTypeId()));

        // Note: Removed isActive check as new schema doesn't have is_active field
        // if (!internshipType.getIsActive()) {
        //     throw new IllegalArgumentException("Cannot create availability for inactive internship type: " + 
        //             internshipType.getFullName());
        // }

        // Validate preference rank logic first (before checking duplicates)
        validatePreferenceRank(createDto.getIsAvailable(), createDto.getPreferenceRank());

        // Check uniqueness
        if (teacherAvailabilityRepository.existsByTeacherIdAndAcademicYearIdAndInternshipTypeId(
                teacherId, createDto.getYearId(), createDto.getInternshipTypeId())) {
            throw new DuplicateResourceException(
                    "Availability entry already exists for this teacher, year, and internship type");
        }

        // Create availability
        TeacherAvailability availability = new TeacherAvailability();
        availability.setTeacher(teacher);
        availability.setAcademicYear(academicYear);
        availability.setInternshipType(internshipType);
        availability.setIsAvailable(createDto.getIsAvailable());
        availability.setPreferenceRank(createDto.getPreferenceRank());
        availability.setNotes(createDto.getNotes());

        TeacherAvailability saved = teacherAvailabilityRepository.save(availability);

        log.info("Availability entry created successfully with ID: {}", saved.getAvailabilityId());
        return teacherAvailabilityMapper.toDto(saved);
    }

    /**
     * Update an existing availability entry.
     */
    @Audited(
            action = AuditLog.AuditAction.UPDATE,
            entityName = AuditEntityNames.TEACHER_AVAILABILITY,
            description = "Updated teacher availability entry",
            captureNewValue = true
    )
    @Transactional
    public TeacherAvailabilityResponseDto updateAvailability(
            Long teacherId, Long availabilityId, TeacherAvailabilityUpdateDto updateDto) {
        log.info("Updating availability ID: {} for teacher ID: {}", availabilityId, teacherId);

        TeacherAvailability availability = teacherAvailabilityRepository
                .findByAvailabilityIdAndTeacherId(availabilityId, teacherId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Availability entry not found with ID: " + availabilityId + " for teacher ID: " + teacherId));

        // If year or internship type is being changed, validate uniqueness
        Long newYearId = updateDto.getYearId() != null ? updateDto.getYearId() : availability.getAcademicYear().getId();
        Long newInternshipTypeId = updateDto.getInternshipTypeId() != null ? 
                updateDto.getInternshipTypeId() : availability.getInternshipType().getId();

        if ((updateDto.getYearId() != null && !updateDto.getYearId().equals(availability.getAcademicYear().getId())) ||
            (updateDto.getInternshipTypeId() != null && !updateDto.getInternshipTypeId().equals(availability.getInternshipType().getId()))) {

            if (teacherAvailabilityRepository.existsByTeacherIdAndYearIdAndInternshipTypeIdAndIdNot(
                    teacherId, newYearId, newInternshipTypeId, availabilityId)) {
                throw new DuplicateResourceException(
                        "Availability entry already exists for this teacher, year, and internship type");
            }
        }

        // Update year if provided
        if (updateDto.getYearId() != null) {
            AcademicYear academicYear = academicYearRepository.findById(updateDto.getYearId())
                    .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with ID: " + updateDto.getYearId()));
            availability.setAcademicYear(academicYear);
        }

        // Update internship type if provided
        if (updateDto.getInternshipTypeId() != null) {
            InternshipType internshipType = internshipTypeRepository.findById(updateDto.getInternshipTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Internship type not found with ID: " + updateDto.getInternshipTypeId()));

            // Note: Removed isActive check as new schema doesn't have is_active field
            // if (!internshipType.getIsActive()) {
            //     throw new IllegalArgumentException("Cannot update to inactive internship type: " + internshipType.getFullName());
            // }
            availability.setInternshipType(internshipType);
        }

        // Update fields via mapper first
        teacherAvailabilityMapper.updateEntityFromDto(availability, updateDto);

        // Validate preference rank logic after update
        validatePreferenceRank(availability.getIsAvailable(), availability.getPreferenceRank());

        TeacherAvailability updated = teacherAvailabilityRepository.save(availability);

        log.info("Availability entry updated successfully with ID: {}", updated.getAvailabilityId());
        return teacherAvailabilityMapper.toDto(updated);
    }

    /**
     * Delete an availability entry.
     */
    @Audited(
            action = AuditLog.AuditAction.DELETE,
            entityName = AuditEntityNames.TEACHER_AVAILABILITY,
            description = "Deleted teacher availability entry",
            captureNewValue = false
    )
    @Transactional
    public void deleteAvailability(Long teacherId, Long availabilityId) {
        log.info("Deleting availability ID: {} for teacher ID: {}", availabilityId, teacherId);

        TeacherAvailability availability = teacherAvailabilityRepository
                .findByAvailabilityIdAndTeacherId(availabilityId, teacherId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Availability entry not found with ID: " + availabilityId + " for teacher ID: " + teacherId));

        // todo: Check if availability is used in final allocation before deleting
        // For now, we allow deletion

        teacherAvailabilityRepository.delete(availability);

        log.info("Availability entry deleted successfully with ID: {}", availabilityId);
    }

    /**
     * Validate preference rank logic.
     * If is_available = false, preference_rank should be null.
     */
    private void validatePreferenceRank(Boolean isAvailable, Integer preferenceRank) {
        if (Boolean.FALSE.equals(isAvailable) && preferenceRank != null) {
            throw new IllegalArgumentException(
                    "Preference rank should be null when teacher is not available");
        }
    }

}
