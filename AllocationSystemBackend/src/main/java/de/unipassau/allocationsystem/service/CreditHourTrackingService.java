package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.AuditLog;
import de.unipassau.allocationsystem.entity.CreditHourTracking;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.TeacherAssignment;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.CreditHourTrackingRepository;
import de.unipassau.allocationsystem.repository.TeacherAssignmentRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import de.unipassau.allocationsystem.utils.SearchSpecificationUtils;
import de.unipassau.allocationsystem.utils.SortFieldUtils;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
/**
 * Service for tracking teacher credit hours per academic year.
 * Manages credit hour allocation and utilization tracking.
 */
public class CreditHourTrackingService implements CrudService<CreditHourTracking, Long> {

    private final CreditHourTrackingRepository repository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final TeacherRepository teacherRepository;
    private final AcademicYearRepository academicYearRepository;

    /**
     * Returns the sortable fields metadata.
     * 
     * @return list of sort field metadata
     */
    public List<Map<String, String>> getSortFields() {
        return SortFieldUtils.getSortFields("id", "teacherId", "academicYearId", "assignmentsCount", 
            "creditHoursAllocated", "creditBalance", "createdAt", "updatedAt");
    }

    /**
     * Returns the list of sortable field keys.
     * 
     * @return list of field keys
     */
    public List<String> getSortFieldKeys() {
        return getSortFields().stream().map(f -> f.get("key")).toList();
    }

    private Specification<CreditHourTracking> buildSearchSpecification(String searchValue) {
        // Search across notes (extend fields if needed)
        return SearchSpecificationUtils.buildMultiFieldLikeSpecification(
            new String[]{"notes"}, searchValue
        );
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.CREDIT_HOUR_TRACKING,
            description = "Viewed list of credit hour tracking entries",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Map<String, Object> getPaginated(Map<String, String> queryParams, String searchValue) {
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        Pageable pageable = PageRequest.of(params.page() - 1, params.pageSize(), sort);

        Specification<CreditHourTracking> spec = buildFilterSpecification(queryParams, searchValue);
        Page<CreditHourTracking> page = repository.findAll(spec, pageable);

        return PaginationUtils.formatPaginationResponse(page);
    }

    private Specification<CreditHourTracking> buildFilterSpecification(Map<String, String> queryParams, String searchValue) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            // Search filter
            if (searchValue != null && !searchValue.trim().isEmpty()) {
                Specification<CreditHourTracking> searchSpec = buildSearchSpecification(searchValue);
                predicates.add(searchSpec.toPredicate(root, query, cb));
            }

            // Academic Year ID filter
            String academicYearIdParam = queryParams.get("academicYearId");
            if (academicYearIdParam != null && !academicYearIdParam.trim().isEmpty()) {
                try {
                    Long academicYearId = Long.parseLong(academicYearIdParam);
                    predicates.add(cb.equal(root.get("academicYear").get("id"), academicYearId));
                } catch (NumberFormatException e) {
                    // Invalid academic year ID, ignore filter
                }
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.CREDIT_HOUR_TRACKING,
            description = "Viewed all credit hour tracking entries",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public List<CreditHourTracking> getAll() {
        return repository.findAll();
    }

    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.CREDIT_HOUR_TRACKING,
            description = "Viewed credit hour tracking entry by id",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Optional<CreditHourTracking> getById(Long id) {
        return repository.findById(id);
    }

    @Audited(
            action = AuditLog.AuditAction.CREATE,
            entityName = AuditEntityNames.CREDIT_HOUR_TRACKING,
            description = "Created credit hour tracking entry",
            captureNewValue = true
    )
    @Transactional
    @Override
    public CreditHourTracking create(CreditHourTracking entity) {
        // Uniqueness check (example: teacherId + yearId must be unique)
        if (entity.getTeacher() != null && entity.getAcademicYear() != null) {
            Optional<CreditHourTracking> existing = repository.findByTeacherIdAndAcademicYearId(
                    entity.getTeacher().getId(), entity.getAcademicYear().getId());
            if (existing.isPresent()) {
                throw new DuplicateResourceException("Credit hour tracking entry already exists for this teacher and year");
            }
        }
        return repository.save(entity);
    }

    @Audited(
            action = AuditLog.AuditAction.UPDATE,
            entityName = AuditEntityNames.CREDIT_HOUR_TRACKING,
            description = "Updated credit hour tracking entry",
            captureNewValue = true
    )
    @Transactional
    @Override
    public CreditHourTracking update(Long id, CreditHourTracking data) {
        CreditHourTracking existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credit hour tracking not found with id: " + id));

        if (data.getTeacher() != null) {
            existing.setTeacher(data.getTeacher());
        }
        if (data.getAcademicYear() != null) {
            existing.setAcademicYear(data.getAcademicYear());
        }
        if (data.getAssignmentsCount() != null) {
            existing.setAssignmentsCount(data.getAssignmentsCount());
        }
        if (data.getCreditHoursAllocated() != null) {
            existing.setCreditHoursAllocated(data.getCreditHoursAllocated());
        }
        if (data.getCreditBalance() != null) {
            existing.setCreditBalance(data.getCreditBalance());
        }
        if (data.getNotes() != null) {
            existing.setNotes(data.getNotes());
        }

        return repository.save(existing);
    }

    @Audited(
            action = AuditLog.AuditAction.DELETE,
            entityName = AuditEntityNames.CREDIT_HOUR_TRACKING,
            description = "Deleted credit hour tracking entry",
            captureNewValue = false
    )
    @Transactional
    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Credit hour tracking not found with id: " + id);
        }
        repository.deleteById(id);
    }

    // other
    /**
     * Recalculates credit hours for a teacher in a specific academic year.
     * Updates assignment counts and credit balance based on active assignments.
     * 
     * @param teacherId the teacher ID
     * @param yearId the academic year ID
     */
    @Transactional
    public void recalculateForTeacherAndYear(Long teacherId, Long yearId) {
        // Validation of teacher and year existence
        Teacher teacher = teacherRepository.findById(teacherId).orElseThrow(() -> new NoSuchElementException("Teacher not found"));
        AcademicYear year = academicYearRepository.findById(yearId).orElseThrow(() -> new NoSuchElementException("AcademicYear not found"));

        List<TeacherAssignment> assignments = teacherAssignmentRepository.findByTeacherIdAndYearId(teacherId, yearId);
        // Count active assignments (PLANNED or CONFIRMED)
        long activeCount = assignments.stream()
                .filter(a -> a.getAssignmentStatus() == TeacherAssignment.AssignmentStatus.PLANNED || a.getAssignmentStatus() == TeacherAssignment.AssignmentStatus.CONFIRMED)
                .count();

        // Determine hours per assignment based on school's type
        double hoursPerAssignment = year.getTotalCreditHours();
        if (teacher.getSchool() != null && year != null) {
            School.SchoolType st = teacher.getSchool().getSchoolType();
            if (st == School.SchoolType.PRIMARY) {
                hoursPerAssignment = year.getElementarySchoolHours();
            } else if (st == School.SchoolType.MIDDLE) {
                hoursPerAssignment = year.getMiddleSchoolHours();
            } else {
                hoursPerAssignment = year.getTotalCreditHours();
            }
        }

        int assignmentsCount = (int) activeCount;
        double creditHoursAllocated = assignmentsCount * hoursPerAssignment;
        // For balance, use total credit hours minus allocated as a simple business rule
        double creditBalance = year.getTotalCreditHours() - creditHoursAllocated;

        CreditHourTracking record = repository.findByTeacherIdAndAcademicYearId(teacherId, yearId).orElseGet(() -> {
            CreditHourTracking c = new CreditHourTracking();
            c.setTeacher(teacher);
            c.setAcademicYear(year);
            return c;
        });

        record.setAssignmentsCount(assignmentsCount);
        record.setCreditHoursAllocated(creditHoursAllocated);
        record.setCreditBalance(creditBalance);

        repository.save(record);
    }
}