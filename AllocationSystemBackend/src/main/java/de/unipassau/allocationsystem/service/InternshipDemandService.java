package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.entity.InternshipDemand;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.AuditLog;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.InternshipDemandAggregation;
import de.unipassau.allocationsystem.repository.InternshipDemandRepository;
import de.unipassau.allocationsystem.repository.InternshipTypeRepository;
import de.unipassau.allocationsystem.repository.SubjectRepository;
import de.unipassau.allocationsystem.utils.PaginationUtils;
import de.unipassau.allocationsystem.utils.SortFieldUtils;
import de.unipassau.allocationsystem.utils.SearchSpecificationUtils;
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

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InternshipDemandService implements CrudService<InternshipDemand, Long> {

    private final InternshipDemandRepository repository;
    private final InternshipTypeRepository internshipTypeRepository;
    private final SubjectRepository subjectRepository;
    private final AcademicYearRepository academicYearRepository;

    public List<Map<String, String>> getSortFields() {
        return SortFieldUtils.getSortFields(
            "id", "yearId", "internshipTypeId", "schoolType", "subjectId", "isForecasted", "createdAt", "updatedAt"
        );
    }

    public List<String> getSortFieldKeys() {
        return getSortFields().stream().map(f -> f.get("key")).toList();
    }

    private Specification<InternshipDemand> buildSearchSpecification(String searchValue) {
        // Search across schoolType, internshipType.title, subject.subjectTitle
        return SearchSpecificationUtils.buildMultiFieldLikeSpecification(
            new String[]{"schoolType", "internshipType.title", "subject.subjectTitle"}, searchValue
        );
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.INTERNSHIP_DEMAND,
            description = "Viewed list of internship demands",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Map<String, Object> getPaginated(Map<String, String> queryParams, String searchValue) {
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        Pageable pageable = PageRequest.of(params.page() - 1, params.pageSize(), sort);

        Specification<InternshipDemand> spec = buildSearchSpecification(searchValue);
        Page<InternshipDemand> page = repository.findAll(spec, pageable);

        return PaginationUtils.formatPaginationResponse(page);
    }

    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.INTERNSHIP_DEMAND,
            description = "Viewed all internship demands",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public List<InternshipDemand> getAll() {
        return repository.findAll();
    }

    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.INTERNSHIP_DEMAND,
            description = "Viewed internship demand by id",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Optional<InternshipDemand> getById(Long id) {
        return repository.findById(id);
    }

    @Audited(
            action = AuditLog.AuditAction.CREATE,
            entityName = AuditEntityNames.INTERNSHIP_DEMAND,
            description = "Created internship demand",
            captureNewValue = true
    )
    @Transactional
    @Override
    public InternshipDemand create(InternshipDemand entity) {
        // Uniqueness check
        Specification<InternshipDemand> uniqueSpec = (root, query, cb) -> cb.and(
                cb.equal(root.get("academicYear").get("id"), entity.getAcademicYear().getId()),
                cb.equal(root.get("internshipType").get("id"), entity.getInternshipType().getId()),
                cb.equal(root.get("schoolType"), entity.getSchoolType()),
                cb.equal(root.get("subject").get("id"), entity.getSubject().getId()),
                cb.equal(root.get("isForecasted"), entity.getIsForecasted() != null ? entity.getIsForecasted() : Boolean.FALSE)
        );
        if (repository.count(uniqueSpec) > 0) {
            throw new DuplicateResourceException("Duplicate internship demand for the same dimensions");
        }
        return repository.save(entity);
    }

    @Audited(
            action = AuditLog.AuditAction.UPDATE,
            entityName = AuditEntityNames.INTERNSHIP_DEMAND,
            description = "Updated internship demand",
            captureNewValue = true
    )
    @Transactional
    @Override
    public InternshipDemand update(Long id, InternshipDemand update) {
        InternshipDemand existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InternshipDemand not found with id: " + id));

        if (update.getAcademicYear() != null && update.getAcademicYear().getId() != null) {
            AcademicYear year = academicYearRepository.findById(update.getAcademicYear().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("AcademicYear not found"));
            existing.setAcademicYear(year);
        }
        if (update.getInternshipType() != null && update.getInternshipType().getId() != null) {
            InternshipType it = internshipTypeRepository.findById(update.getInternshipType().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("InternshipType not found"));
            existing.setInternshipType(it);
        }
        if (update.getSubject() != null && update.getSubject().getId() != null) {
            Subject s = subjectRepository.findById(update.getSubject().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
            if (!Boolean.TRUE.equals(s.getIsActive())) {
                throw new IllegalStateException("Subject is not active");
            }
            existing.setSubject(s);
        }
        if (update.getSchoolType() != null) {
            existing.setSchoolType(update.getSchoolType());
        }
        if (update.getIsForecasted() != null) {
            existing.setIsForecasted(update.getIsForecasted());
        }
        // Add other updatable fields here as needed

        return repository.save(existing);
    }

    @Audited(
            action = AuditLog.AuditAction.DELETE,
            entityName = AuditEntityNames.INTERNSHIP_DEMAND,
            description = "Deleted internship demand",
            captureNewValue = false
    )
    @Transactional
    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("InternshipDemand not found with id: " + id);
        }
        repository.deleteById(id);
    }

    // other methods
    public Page<InternshipDemand> listByYearWithFilters(Long yearId, Long internshipTypeId, School.SchoolType schoolType, Long subjectId, Boolean isForecasted, Pageable pageable) {
        Specification<InternshipDemand> spec = (root, query, cb) -> cb.conjunction();
        spec = spec.and((root, query, cb) -> cb.equal(root.get("academicYear").get("id"), yearId));
        if (internshipTypeId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("internshipType").get("id"), internshipTypeId));
        }
        if (schoolType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("schoolType"), schoolType));
        }
        if (subjectId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("subject").get("id"), subjectId));
        }
        if (isForecasted != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isForecasted"), isForecasted));
        }
        return repository.findAll(spec, pageable);
    }

    public List<InternshipDemand> getAllByYear(Long yearId) {
        return repository.findAll((root, query, cb) -> cb.equal(root.get("academicYear").get("id"), yearId));
    }

    public List<InternshipDemandAggregation> aggregateByYear(Long yearId) {
        return repository.aggregateByYear(yearId);
    }

    /**
     * Return typed aggregation DTOs for a given academic year.
     */
    public List<InternshipDemandAggregation> getAggregationForYear(Long yearId) {
        return repository.aggregateByYear(yearId);
    }
}