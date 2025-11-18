package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
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

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AcademicYearService implements CrudService<AcademicYear, Long> {

    private final AcademicYearRepository academicYearRepository;

    @Override
    public List<Map<String, String>> getSortFields() {
        List<Map<String, String>> fields = new ArrayList<>();
        fields.add(Map.of("key", "id", "label", "ID"));
        fields.add(Map.of("key", "yearName", "label", "Year Name"));
        fields.add(Map.of("key", "createdAt", "label", "Creation Date"));
        fields.add(Map.of("key", "updatedAt", "label", "Last Updated"));
        return fields;
    }

    public List<String> getSortFieldKeys() {
        List<String> keys = new ArrayList<>();
        for (Map<String, String> field : getSortFields()) {
            keys.add(field.get("key"));
        }
        return keys;
    }

    private Specification<AcademicYear> buildSearchSpecification(String searchValue) {
        if (searchValue == null || searchValue.trim().isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }

        String likePattern = "%" + searchValue.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("yearName")), likePattern);
    }

    public boolean yearNameExists(String yearName) {
        return academicYearRepository.findByYearName(yearName).isPresent();
    }

    @Override
    public boolean existsById(Long id) {
        return academicYearRepository.existsById(id);
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.ACADEMIC_YEAR,
            description = "Viewed list of academic years",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Map<String, Object> getPaginated(Map<String, String> queryParams, String searchValue) {
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        Pageable pageable = PageRequest.of(params.page() - 1, params.pageSize(), sort);

        Specification<AcademicYear> spec = buildSearchSpecification(searchValue);
        Page<AcademicYear> page = academicYearRepository.findAll(spec, pageable);

        return PaginationUtils.formatPaginationResponse(page);
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.ACADEMIC_YEAR,
            description = "Viewed all academic years",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public List<AcademicYear> getAll() {
        return academicYearRepository.findAll();
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.ACADEMIC_YEAR,
            description = "Viewed academic year by id",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Optional<AcademicYear> getById(Long id) {
        return academicYearRepository.findById(id);
    }

    @Audited(
            action = AuditAction.CREATE,
            entityName = AuditEntityNames.ACADEMIC_YEAR,
            description = "Created new academic year",
            captureNewValue = true
    )
    @Transactional
    @Override
    public AcademicYear create(AcademicYear academicYear) {
        if (academicYearRepository.findByYearName(academicYear.getYearName()).isPresent()) {
            throw new DuplicateResourceException("Academic year with name '" + academicYear.getYearName() + "' already exists");
        }
        return academicYearRepository.save(academicYear);
    }

    @Audited(
            action = AuditAction.UPDATE,
            entityName = AuditEntityNames.ACADEMIC_YEAR,
            description = "Updated academic year",
            captureNewValue = true
    )
    @Transactional
    @Override
    public AcademicYear update(Long id, AcademicYear data) {
        AcademicYear existing = academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + id));

        if (data.getYearName() != null && !data.getYearName().equals(existing.getYearName())) {
            if (academicYearRepository.findByYearName(data.getYearName()).isPresent()) {
                throw new DuplicateResourceException("Academic year with name '" + data.getYearName() + "' already exists");
            }
            existing.setYearName(data.getYearName());
        }
        if (data.getTotalCreditHours() != null) {
            existing.setTotalCreditHours(data.getTotalCreditHours());
        }
        if (data.getElementarySchoolHours() != null) {
            existing.setElementarySchoolHours(data.getElementarySchoolHours());
        }
        if (data.getMiddleSchoolHours() != null) {
            existing.setMiddleSchoolHours(data.getMiddleSchoolHours());
        }
        if (data.getBudgetAnnouncementDate() != null) {
            existing.setBudgetAnnouncementDate(data.getBudgetAnnouncementDate());
        }
        if (data.getAllocationDeadline() != null) {
            existing.setAllocationDeadline(data.getAllocationDeadline());
        }
        if (data.getIsLocked() != null) {
            existing.setIsLocked(data.getIsLocked());
        }

        return academicYearRepository.save(existing);
    }

    @Audited(
            action = AuditAction.DELETE,
            entityName = AuditEntityNames.ACADEMIC_YEAR,
            description = "Deleted academic year",
            captureNewValue = false
    )
    @Transactional
    @Override
    public void delete(Long id) {
        if (!academicYearRepository.existsById(id)) {
            throw new ResourceNotFoundException("Academic year not found with id: " + id);
        }
        academicYearRepository.deleteById(id);
    }
}