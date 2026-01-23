package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.entity.TeacherAvailability;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.TeacherAvailabilityRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import de.unipassau.allocationsystem.utils.SortFieldUtils;
import de.unipassau.allocationsystem.utils.SearchSpecificationUtils;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
/**
 * Service for managing teacher availability.
 * Handles CRUD operations for teacher availability records.
 */
public class TeacherAvailabilityService implements CrudService<TeacherAvailability, Long> {

    private final TeacherAvailabilityRepository teacherAvailabilityRepository;

    @Override
    public List<Map<String, String>> getSortFields() {
        return SortFieldUtils.getSortFields("id", "teacherId", "createdAt", "updatedAt");
    }

    /**
     * Returns the list of sortable field keys.
     * 
     * @return list of field keys
     */
    public List<String> getSortFieldKeys() {
        List<String> keys = new ArrayList<>();
        for (Map<String, String> field : getSortFields()) {
            keys.add(field.get("key"));
        }
        return keys;
    }

    private Specification<TeacherAvailability> buildSearchSpecification(String searchValue) {
        // Search across notes (extend fields if needed)
        return SearchSpecificationUtils.buildMultiFieldLikeSpecification(
            new String[]{"notes"}, searchValue
        );
    }

    /**
     * Validates the 3-part composite unique key for teacher availability.
     * 
     * @param teacherId the teacher ID
     * @param academicYearId the academic year ID
     * @param internshipTypeId the internship type ID
     * @throws DuplicateResourceException if a duplicate availability exists
     */
    private void validateCompositeUniqueness(Long teacherId, Long academicYearId, Long internshipTypeId) {
        if (teacherAvailabilityRepository.existsByTeacherIdAndAcademicYearIdAndInternshipTypeId(
                teacherId, academicYearId, internshipTypeId)) {
            throw new DuplicateResourceException("Teacher availability already exists for this teacher, year, and internship type");
        }
    }

    /**
     * Validates the 3-part composite unique key for updates, allowing self-updates.
     * 
     * @param teacherId the teacher ID
     * @param academicYearId the academic year ID
     * @param internshipTypeId the internship type ID
     * @param existingId the ID of the existing availability being updated
     * @throws DuplicateResourceException if a different availability has the same composite key
     */
    private void validateCompositeUniquenessForUpdate(Long teacherId, Long academicYearId, Long internshipTypeId, Long existingId) {
        if (teacherAvailabilityRepository.existsByTeacherIdAndYearIdAndInternshipTypeIdAndIdNot(
                teacherId, academicYearId, internshipTypeId, existingId)) {
            throw new DuplicateResourceException("Teacher availability already exists for this teacher, year, and internship type");
        }
    }

    /**
     * Validates that a teacher availability exists and returns it.
     * 
     * @param id the availability ID
     * @return the existing TeacherAvailability
     * @throws ResourceNotFoundException if the availability does not exist
     */
    private TeacherAvailability validateExistence(Long id) {
        return teacherAvailabilityRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Teacher availability not found with id: " + id));
    }

    /**
     * Applies field updates from data object to existing availability.
     * Consolidates null-check-and-set for: notes, status, preferenceRank, isAvailable.
     * 
     * @param existing the existing TeacherAvailability to update
     * @param data the new data to apply
     */
    private void applyFieldUpdates(TeacherAvailability existing, TeacherAvailability data) {
        if (data.getNotes() != null) {
            existing.setNotes(data.getNotes());
        }
        if (data.getStatus() != null) {
            existing.setStatus(data.getStatus());
        }
        if (data.getPreferenceRank() != null) {
            existing.setPreferenceRank(data.getPreferenceRank());
        }
        if (data.getIsAvailable() != null) {
            existing.setIsAvailable(data.getIsAvailable());
        }
    }

    @Override
    public boolean existsById(Long id) {
        return teacherAvailabilityRepository.findById(id).isPresent();
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.TEACHER_AVAILABILITY,
            description = "Viewed list of teacher availabilities",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Map<String, Object> getPaginated(Map<String, String> queryParams, String searchValue) {
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        Pageable pageable = PageRequest.of(params.page() - 1, params.pageSize(), sort);

        Specification<TeacherAvailability> spec = buildSearchSpecification(searchValue);
        Page<TeacherAvailability> page = teacherAvailabilityRepository.findAll(spec, pageable);

        return PaginationUtils.formatPaginationResponse(page);
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.TEACHER_AVAILABILITY,
            description = "Viewed all teacher availabilities",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public List<TeacherAvailability> getAll() {
        return getAllTeacherAvailabilities();
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.TEACHER_AVAILABILITY,
            description = "Viewed teacher availability by id",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Optional<TeacherAvailability> getById(Long id) {
        return getTeacherAvailabilityById(id);
    }

    private List<TeacherAvailability> getAllTeacherAvailabilities() {
        return teacherAvailabilityRepository.findAll();
    }

    private Optional<TeacherAvailability> getTeacherAvailabilityById(Long id) {
        return teacherAvailabilityRepository.findById(id);
    }

    @Audited(
            action = AuditAction.CREATE,
            entityName = AuditEntityNames.TEACHER_AVAILABILITY,
            description = "Created new teacher availability",
            captureNewValue = true
    )
    @Transactional
    @Override
    public TeacherAvailability create(TeacherAvailability availability) {
        // Validate composite uniqueness
        validateCompositeUniqueness(
            availability.getTeacher().getId(),
            availability.getAcademicYear().getId(),
            availability.getInternshipType().getId()
        );
        return teacherAvailabilityRepository.save(availability);
    }

    @Audited(
            action = AuditAction.UPDATE,
            entityName = AuditEntityNames.TEACHER_AVAILABILITY,
            description = "Updated teacher availability",
            captureNewValue = true
    )
    @Transactional
    @Override
    public TeacherAvailability update(Long id, TeacherAvailability data) {
        TeacherAvailability existing = validateExistence(id);

        // Validate composite uniqueness for update, allowing self-updates
        validateCompositeUniquenessForUpdate(
            data.getTeacher().getId(),
            data.getAcademicYear().getId(),
            data.getInternshipType().getId(),
            id
        );

        applyFieldUpdates(existing, data);

        return teacherAvailabilityRepository.save(existing);
    }

    @Audited(
            action = AuditAction.DELETE,
            entityName = AuditEntityNames.TEACHER_AVAILABILITY,
            description = "Deleted teacher availability",
            captureNewValue = false
    )
    @Transactional
    @Override
    public void delete(Long id) {
        validateExistence(id);
        teacherAvailabilityRepository.deleteById(id);
    }
}
