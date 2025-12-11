package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.entity.AuditLog;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.TeacherSubject;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.SubjectRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import de.unipassau.allocationsystem.repository.TeacherSubjectRepository;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import de.unipassau.allocationsystem.utils.SearchSpecificationUtils;
import de.unipassau.allocationsystem.utils.SortFieldUtils;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TeacherSubjectService implements CrudService<TeacherSubject, Long> {

    private final TeacherSubjectRepository teacherSubjectRepository;
    private final AcademicYearRepository academicYearRepository;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;

    public List<Map<String, String>> getSortFields() {
        return SortFieldUtils.getSortFields("id", "academicYearId", "teacherId", "subjectId", 
            "availabilityStatus", "gradeLevelFrom", "gradeLevelTo", "createdAt", "updatedAt");
    }

    public List<String> getSortFieldKeys() {
        return getSortFields().stream().map(f -> f.get("key")).toList();
    }

    private Specification<TeacherSubject> buildSearchSpecification(String searchValue) {
        // Search across availabilityStatus and notes
        return SearchSpecificationUtils.buildMultiFieldLikeSpecification(
            new String[]{"availabilityStatus", "notes"}, searchValue
        );
    }

    @Override
    public boolean existsById(Long id) {
        return teacherSubjectRepository.existsById(id);
    }

    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.TEACHER_SUBJECT,
            description = "Viewed list of teacher-subjects",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Map<String, Object> getPaginated(Map<String, String> queryParams, String searchValue) {
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        Pageable pageable = PageRequest.of(params.page() - 1, params.pageSize(), sort);

        Specification<TeacherSubject> spec = buildSearchSpecification(searchValue);
        Page<TeacherSubject> page = teacherSubjectRepository.findAll(spec, pageable);

        return PaginationUtils.formatPaginationResponse(page);
    }

    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.TEACHER_SUBJECT,
            description = "Viewed all teacher-subjects",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public List<TeacherSubject> getAll() {
        return teacherSubjectRepository.findAll();
    }

    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.TEACHER_SUBJECT,
            description = "Viewed teacher-subject by id",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Optional<TeacherSubject> getById(Long id) {
        return teacherSubjectRepository.findById(id);
    }

    @Audited(
            action = AuditLog.AuditAction.CREATE,
            entityName = AuditEntityNames.TEACHER_SUBJECT,
            description = "Created teacher-subject mapping",
            captureNewValue = true
    )
    @Transactional
    @Override
    public TeacherSubject create(TeacherSubject entity) {
        validateReferences(entity);
        // uniqueness
        if (teacherSubjectRepository.findByTeacherIdAndAcademicYearId(entity.getTeacher().getId(), entity.getAcademicYear().getId())
                .stream().anyMatch(ts -> ts.getSubject().getId().equals(entity.getSubject().getId()))) {
            throw new DuplicateResourceException("Teacher-Subject mapping already exists for this year");
        }
        return teacherSubjectRepository.save(entity);
    }

    @Audited(
            action = AuditLog.AuditAction.UPDATE,
            entityName = AuditEntityNames.TEACHER_SUBJECT,
            description = "Updated teacher-subject mapping",
            captureNewValue = true
    )
    @Transactional
    @Override
    public TeacherSubject update(Long id, TeacherSubject update) {
        TeacherSubject existing = teacherSubjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TeacherSubject not found with id: " + id));

        // validate references if changed
        if (update.getAcademicYear() != null && !update.getAcademicYear().getId().equals(existing.getAcademicYear().getId())) {
            if (!academicYearRepository.existsById(update.getAcademicYear().getId())) {
                throw new ResourceNotFoundException("Academic year not found with id: " + update.getAcademicYear().getId());
            }
            existing.setAcademicYear(update.getAcademicYear());
        }

        if (update.getTeacher() != null && !update.getTeacher().getId().equals(existing.getTeacher().getId())) {
            if (!teacherRepository.existsById(update.getTeacher().getId())) {
                throw new ResourceNotFoundException("Teacher not found with id: " + update.getTeacher().getId());
            }
            existing.setTeacher(update.getTeacher());
        }

        if (update.getSubject() != null && !update.getSubject().getId().equals(existing.getSubject().getId())) {
            if (!subjectRepository.existsById(update.getSubject().getId())) {
                throw new ResourceNotFoundException("Subject not found with id: " + update.getSubject().getId());
            }
            existing.setSubject(update.getSubject());
        }

        if (update.getAvailabilityStatus() != null) {
            existing.setAvailabilityStatus(update.getAvailabilityStatus());
        }
        if (update.getGradeLevelFrom() != null) {
            existing.setGradeLevelFrom(update.getGradeLevelFrom());
        }
        if (update.getGradeLevelTo() != null) {
            existing.setGradeLevelTo(update.getGradeLevelTo());
        }
        if (update.getNotes() != null) {
            existing.setNotes(update.getNotes());
        }

        // validate grade range
        if (existing.getGradeLevelFrom() != null && existing.getGradeLevelTo() != null && existing.getGradeLevelFrom() > existing.getGradeLevelTo()) {
            throw new IllegalArgumentException("gradeLevelFrom must be <= gradeLevelTo");
        }

        return teacherSubjectRepository.save(existing);
    }

    @Audited(
            action = AuditLog.AuditAction.DELETE,
            entityName = AuditEntityNames.TEACHER_SUBJECT,
            description = "Deleted teacher-subject mapping",
            captureNewValue = false
    )
    @Transactional
    @Override
    public void delete(Long id) {
        if (!teacherSubjectRepository.existsById(id)) {
            throw new ResourceNotFoundException("TeacherSubject not found with id: " + id);
        }
        teacherSubjectRepository.deleteById(id);
    }

    private void validateReferences(TeacherSubject entity) {
        if (entity.getAcademicYear() == null || entity.getAcademicYear().getId() == null) {
            throw new IllegalArgumentException("academicYearId is required");
        }
        if (entity.getTeacher() == null || entity.getTeacher().getId() == null) {
            throw new IllegalArgumentException("teacherId is required");
        }
        if (entity.getSubject() == null || entity.getSubject().getId() == null) {
            throw new IllegalArgumentException("subjectId is required");
        }

        AcademicYear year = academicYearRepository.findById(entity.getAcademicYear().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + entity.getAcademicYear().getId()));

        Teacher teacher = teacherRepository.findById(entity.getTeacher().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found with id: " + entity.getTeacher().getId()));
        if (!teacher.getEmploymentStatus().equals(Teacher.EmploymentStatus.ACTIVE)) {
            throw new IllegalArgumentException("Teacher is not active");
        }

        Subject subject = subjectRepository.findById(entity.getSubject().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + entity.getSubject().getId()));
        if (!subject.getIsActive()) {
            throw new IllegalArgumentException("Subject is not active");
        }

        // validate grade range
        if (entity.getGradeLevelFrom() != null && entity.getGradeLevelTo() != null && entity.getGradeLevelFrom() > entity.getGradeLevelTo()) {
            throw new IllegalArgumentException("gradeLevelFrom must be <= gradeLevelTo");
        }

        // validate availability
        if (entity.getAvailabilityStatus() == null) {
            throw new IllegalArgumentException("availabilityStatus is required");
        }
        try {
            TeacherSubject.AvailabilityStatus.valueOf(entity.getAvailabilityStatus());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid availabilityStatus value: " + entity.getAvailabilityStatus());
        }
    }
}