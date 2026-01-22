package de.unipassau.allocationsystem.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import de.unipassau.allocationsystem.utils.SortFieldUtils;
import de.unipassau.allocationsystem.utils.SearchSpecificationUtils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.entity.AuditLog;
import de.unipassau.allocationsystem.entity.TeacherAssignment;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.TeacherAssignmentRepository;
import de.unipassau.allocationsystem.utils.PaginationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
/**
 * Service for managing teacher assignments.
 * Handles CRUD operations and assignment tracking for teachers.
 */
public class TeacherAssignmentService implements CrudService<TeacherAssignment, Long> {

    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final CreditHourTrackingService creditHourTrackingService;

    /**
     * Returns the sortable fields metadata.
     * 
     * @return list of sort field metadata
     */
    public List<Map<String, String>> getSortFields() {
        return SortFieldUtils.getSortFields(
            "id", "planId", "teacherId", "internshipTypeId", "subjectId",
            "assignmentStatus", "studentGroupSize", "isManualOverride", "createdAt", "updatedAt"
        );
    }

    /**
     * Returns the list of sortable field keys.
     * 
     * @return list of field keys
     */
    public List<String> getSortFieldKeys() {
        return getSortFields().stream().map(f -> f.get("key")).toList();
    }

    private Specification<TeacherAssignment> buildSearchSpecification(String searchValue) {
        // Search across assignmentStatus and notes
        return SearchSpecificationUtils.buildMultiFieldLikeSpecification(
            new String[]{"assignmentStatus", "notes"}, searchValue
        );
    }

    /**
     * Validates the 4-part composite unique key for teacher assignments.
     * 
     * @param teacherId the teacher ID
     * @param planId the allocation plan ID
     * @param internshipTypeId the internship type ID
     * @param subjectId the subject ID
     * @throws DuplicateResourceException if a duplicate assignment exists
     */
    private void validateCompositeUniqueness(Long teacherId, Long planId, Long internshipTypeId, Long subjectId) {
        if (teacherAssignmentRepository.existsByAllocationPlanIdAndTeacherIdAndInternshipTypeIdAndSubjectId(
                planId, teacherId, internshipTypeId, subjectId)) {
            throw new DuplicateResourceException("Duplicate assignment for same plan/teacher/internship/subject");
        }
    }

    /**
     * Validates the 4-part composite unique key for updates, allowing self-updates.
     * 
     * @param teacherId the teacher ID
     * @param planId the allocation plan ID
     * @param internshipTypeId the internship type ID
     * @param subjectId the subject ID
     * @param existingId the ID of the existing assignment being updated
     * @throws DuplicateResourceException if a different assignment has the same composite key
     */
    private void validateCompositeUniquenessForUpdate(Long teacherId, Long planId, Long internshipTypeId, Long subjectId, Long existingId) {
        Optional<TeacherAssignment> existingAssignment = teacherAssignmentRepository.findByAllocationPlanIdAndTeacherIdAndInternshipTypeIdAndSubjectId(
                planId, teacherId, internshipTypeId, subjectId);
        
        if (existingAssignment.isPresent() && !existingAssignment.get().getId().equals(existingId)) {
            throw new DuplicateResourceException("Duplicate assignment for same plan/teacher/internship/subject");
        }
    }

    /**
     * Validates that a teacher assignment exists and returns it.
     * 
     * @param id the assignment ID
     * @return the existing TeacherAssignment
     * @throws ResourceNotFoundException if the assignment does not exist
     */
    private TeacherAssignment validateExistence(Long id) {
        return teacherAssignmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("TeacherAssignment not found with id: " + id));
    }

    /**
     * Applies field updates from data object to existing assignment.
     * Consolidates null-check-and-set for: studentGroupSize, assignmentStatus, isManualOverride, notes.
     * 
     * @param existing the existing TeacherAssignment to update
     * @param data the new data to apply
     * @return true if a recalculation is needed, false otherwise
     */
    private boolean applyFieldUpdates(TeacherAssignment existing, TeacherAssignment data) {
        boolean recalcNeeded = false;

        if (data.getStudentGroupSize() != null && !data.getStudentGroupSize().equals(existing.getStudentGroupSize())) {
            existing.setStudentGroupSize(data.getStudentGroupSize());
            recalcNeeded = true;
        }
        if (data.getAssignmentStatus() != null && !data.getAssignmentStatus().equals(existing.getAssignmentStatus())) {
            existing.setAssignmentStatus(data.getAssignmentStatus());
            recalcNeeded = true;
        }
        if (data.getIsManualOverride() != null) {
            existing.setIsManualOverride(data.getIsManualOverride());
        }
        if (data.getNotes() != null) {
            existing.setNotes(data.getNotes());
        }

        return recalcNeeded;
    }

    @Override
    public boolean existsById(Long id) {
        return teacherAssignmentRepository.existsById(id);
    }

    @Audited(
        action = AuditLog.AuditAction.VIEW,
        entityName = AuditEntityNames.TEACHER_ASSIGNMENT,
        description = "Viewed list of teacher assignments",
        captureNewValue = false
    )
    @Transactional
    @Override
    public Map<String, Object> getPaginated(Map<String, String> queryParams, String searchValue) {
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        Pageable pageable = PageRequest.of(params.page() - 1, params.pageSize(), sort);

        Specification<TeacherAssignment> spec = buildSearchSpecification(searchValue);
        Page<TeacherAssignment> page = teacherAssignmentRepository.findAll(spec, pageable);

        return PaginationUtils.formatPaginationResponse(page);
    }

    @Audited(
        action = AuditLog.AuditAction.VIEW,
        entityName = AuditEntityNames.TEACHER_ASSIGNMENT,
        description = "Viewed all teacher assignments",
        captureNewValue = false
    )
    @Transactional
    @Override
    public List<TeacherAssignment> getAll() {
        return teacherAssignmentRepository.findAll();
    }

    @Audited(
        action = AuditLog.AuditAction.VIEW,
        entityName = AuditEntityNames.TEACHER_ASSIGNMENT,
        description = "Viewed teacher assignment by id",
        captureNewValue = false
    )
    @Transactional
    @Override
    public Optional<TeacherAssignment> getById(Long id) {
        return teacherAssignmentRepository.findById(id);
    }

    @Audited(
        action = AuditLog.AuditAction.CREATE,
        entityName = AuditEntityNames.TEACHER_ASSIGNMENT,
        description = "Created teacher assignment",
        captureNewValue = true
    )
    @Transactional
    @Override
    public TeacherAssignment create(TeacherAssignment entity) {
        // Validate composite uniqueness
        validateCompositeUniqueness(
            entity.getTeacher().getId(),
            entity.getAllocationPlan().getId(),
            entity.getInternshipType().getId(),
            entity.getSubject().getId()
        );
        
        entity.setAssignedAt(java.time.LocalDateTime.now());
        TeacherAssignment saved = teacherAssignmentRepository.save(entity);

        // Update credit tracking
        Long yearId = saved.getAllocationPlan().getAcademicYear().getId();
        creditHourTrackingService.recalculateForTeacherAndYear(saved.getTeacher().getId(), yearId);

        return saved;
    }

    @Audited(
        action = AuditLog.AuditAction.UPDATE,
        entityName = AuditEntityNames.TEACHER_ASSIGNMENT,
        description = "Updated teacher assignment",
        captureNewValue = true
    )
    @Transactional
    @Override
    public TeacherAssignment update(Long id, TeacherAssignment data) {
        TeacherAssignment existing = validateExistence(id);

        // Validate composite uniqueness for update, allowing self-updates
        validateCompositeUniquenessForUpdate(
            data.getTeacher().getId(),
            data.getAllocationPlan().getId(),
            data.getInternshipType().getId(),
            data.getSubject().getId(),
            id
        );

        boolean recalcNeeded = applyFieldUpdates(existing, data);

        TeacherAssignment updated = teacherAssignmentRepository.save(existing);

        if (recalcNeeded) {
            Long yearId = updated.getAllocationPlan().getAcademicYear().getId();
            creditHourTrackingService.recalculateForTeacherAndYear(updated.getTeacher().getId(), yearId);
        }

        return updated;
    }

    @Audited(
        action = AuditLog.AuditAction.DELETE,
        entityName = AuditEntityNames.TEACHER_ASSIGNMENT,
        description = "Deleted teacher assignment",
        captureNewValue = false
    )
    @Transactional
    @Override
    public void delete(Long id) {
        TeacherAssignment existing = validateExistence(id);
        teacherAssignmentRepository.delete(existing);

        // Recalculate credit hours
        Long yearId = existing.getAllocationPlan().getAcademicYear().getId();
        creditHourTrackingService.recalculateForTeacherAndYear(existing.getTeacher().getId(), yearId);
    }
    
    /**
     * Lists all assignments for a specific teacher in a specific academic year.
     * 
     * @param teacherId the teacher ID
     * @param yearId the academic year ID
     * @return list of teacher assignments
     */
    public List<TeacherAssignment> listByTeacherAndYear(Long teacherId, Long yearId) {
        return teacherAssignmentRepository.findByTeacherIdAndYearId(teacherId, yearId);
    }
}
