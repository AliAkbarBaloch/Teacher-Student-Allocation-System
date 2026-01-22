package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.audit.AuditedAcademicYearCreate;
import de.unipassau.allocationsystem.aspect.audit.AuditedAcademicYearDelete;
import de.unipassau.allocationsystem.aspect.audit.AuditedAcademicYearUpdate;
import de.unipassau.allocationsystem.aspect.audit.AuditedAcademicYearViewAll;
import de.unipassau.allocationsystem.aspect.audit.AuditedAcademicYearViewById;
import de.unipassau.allocationsystem.aspect.audit.AuditedAcademicYearViewPaginated;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.utils.PaginationUtils;
import de.unipassau.allocationsystem.utils.SearchSpecificationUtils;
import de.unipassau.allocationsystem.utils.SortFieldUtils;
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
/**
 * Service for managing academic years.
 * Handles CRUD operations and validation for academic year entities.
 */
public class AcademicYearService implements CrudService<AcademicYear, Long> {

    private final AcademicYearRepository academicYearRepository;

    @Override
    public List<Map<String, String>> getSortFields() {
        return SortFieldUtils.getSortFields("id", "yearName", "createdAt", "updatedAt");
    }

    /**
     * Returns the list of sortable field keys.
     * 
     * @return list of field keys
     */
    public List<String> getSortFieldKeys() {
        return getSortFields().stream().map(f -> f.get("key")).toList();
    }

    private Specification<AcademicYear> buildSearchSpecification(String searchValue) {
        // Search across yearName (extend fields if needed)
        return SearchSpecificationUtils.buildMultiFieldLikeSpecification(
            new String[]{"yearName"}, searchValue
        );
    }

    /**
     * Checks if an academic year with the given name exists.
     * 
     * @param yearName the year name to check
     * @return true if year name exists, false otherwise
     */
    public boolean yearNameExists(String yearName) {
        return academicYearRepository.findByYearName(yearName).isPresent();
    }

    /**
     * Validates that the year name is unique, throws exception if duplicate found.
     * 
     * @param yearName the year name to validate
     * @throws DuplicateResourceException if year name already exists
     */
    private void validateYearNameUniqueness(String yearName) {
        if (academicYearRepository.findByYearName(yearName).isPresent()) {
            throw new DuplicateResourceException("Academic year with name '" + yearName + "' already exists");
        }
    }

    /**
     * Validates that a new year name doesn't conflict with existing records (for updates).
     * Allows the same name if it's the current year being updated.
     * 
     * @param newName the new year name
     * @param oldName the old year name
     * @throws DuplicateResourceException if new name conflicts with another year's name
     */
    private void validateYearNameForUpdate(String newName, String oldName) {
        if (!newName.equals(oldName) && academicYearRepository.findByYearName(newName).isPresent()) {
            throw new DuplicateResourceException("Academic year with name '" + newName + "' already exists");
        }
    }

    /**
     * Validates that an academic year exists with the given ID.
     * 
     * @param id the academic year ID
     * @throws ResourceNotFoundException if not found
     */
    private void validateExistence(Long id) {
        if (!academicYearRepository.existsById(id)) {
            throw new ResourceNotFoundException("Academic year not found with id: " + id);
        }
    }

    /**
     * Applies field updates from source to target academic year.
     * Only updates fields that are non-null in the source.
     * 
     * @param existing the target academic year to update
     * @param data the source data with new values
     */
    private void applyFieldUpdates(AcademicYear existing, AcademicYear data) {
        if (data.getYearName() != null) {
            validateYearNameForUpdate(data.getYearName(), existing.getYearName());
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
    }

    @Override
    public boolean existsById(Long id) {
        return academicYearRepository.existsById(id);
    }

    @AuditedAcademicYearViewPaginated
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

    @AuditedAcademicYearViewAll
    @Transactional(readOnly = true)
    @Override
    public List<AcademicYear> getAll() {
        return academicYearRepository.findAll();
    }

    @AuditedAcademicYearViewById
    @Transactional(readOnly = true)
    @Override
    public Optional<AcademicYear> getById(Long id) {
        return academicYearRepository.findById(id);
    }

    @AuditedAcademicYearCreate
    @Transactional
    @Override
    public AcademicYear create(AcademicYear academicYear) {
        validateYearNameUniqueness(academicYear.getYearName());
        return academicYearRepository.save(academicYear);
    }

    @AuditedAcademicYearUpdate
    @Transactional
    @Override
    public AcademicYear update(Long id, AcademicYear data) {
        AcademicYear existing = academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + id));

        applyFieldUpdates(existing, data);
        return academicYearRepository.save(existing);
    }

    @AuditedAcademicYearDelete
    @Transactional
    @Override
    public void delete(Long id) {
        validateExistence(id);
        academicYearRepository.deleteById(id);
    }
}