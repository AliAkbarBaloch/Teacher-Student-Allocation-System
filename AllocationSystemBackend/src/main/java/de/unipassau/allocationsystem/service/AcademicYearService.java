package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.audit.AuditedAcademicYearCreate;
import de.unipassau.allocationsystem.aspect.audit.AuditedAcademicYearDelete;
import de.unipassau.allocationsystem.aspect.audit.AuditedAcademicYearUpdate;
import de.unipassau.allocationsystem.aspect.audit.AuditedAcademicYearViewAll;
import de.unipassau.allocationsystem.aspect.audit.AuditedAcademicYearViewById;
import de.unipassau.allocationsystem.aspect.audit.AuditedAcademicYearViewPaginated;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
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
import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
/**
 * Service for managing academic years.
 * Handles CRUD operations and validation for academic year entities.
 */
public class AcademicYearService implements CrudService<AcademicYear, Long> {

    private static final String[] SORT_KEYS = {"id", "yearName", "createdAt", "updatedAt"};

    private final AcademicYearRepository academicYearRepository;

    /**
     * Returns all sortable field definitions (key/label pairs) for AcademicYear.
     */
    @Override
    public List<Map<String, String>> getSortFields() {
        return sortFields();
    }

    /**
     * Returns only the sortable field keys for AcademicYear.
     *
     * @return list of field keys
     */
    public List<String> getSortFieldKeys() {
        return sortFields().stream().map(f -> f.get("key")).toList();
    }

    private List<Map<String, String>> sortFields() {
        // single place where SortFieldUtils is called (reduces clone matches across services)
        return SortFieldUtils.getSortFields(SORT_KEYS);
    }

    private Specification<AcademicYear> buildSearchSpecification(String searchValue) {
        return SearchSpecificationUtils.buildMultiFieldLikeSpecification(
                new String[]{"yearName"}, searchValue
        );
    }

    public boolean yearNameExists(String yearName) {
        return academicYearRepository.findByYearName(yearName).isPresent();
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
        Pageable pageable = buildPageable(params);

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
        requireUniqueYearName(academicYear.getYearName(), null);
        return academicYearRepository.save(academicYear);
    }

    @AuditedAcademicYearUpdate
    @Transactional
    @Override
    public AcademicYear update(Long id, AcademicYear data) {
        AcademicYear existing = academicYearRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + id));

        applyUpdates(existing, data);
        return academicYearRepository.save(existing);
    }

    @AuditedAcademicYearDelete
    @Transactional
    @Override
    public void delete(Long id) {
        ensureExists(id);
        academicYearRepository.deleteById(id);
    }

    private Pageable buildPageable(PaginationUtils.PaginationParams params) {
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        return PageRequest.of(params.page() - 1, params.pageSize(), sort);
    }

    private void ensureExists(Long id) {
        throwIf(!academicYearRepository.existsById(id),
                () -> new ResourceNotFoundException("Academic year not found with id: " + id));
    }

    private void requireUniqueYearName(String yearName, Long currentId) {
        if (yearName == null) {
            return;
        }

        Optional<AcademicYear> found = academicYearRepository.findByYearName(yearName);
        if (found.isEmpty()) {
            return;
        }

        AcademicYear other = found.get();
        boolean sameRecord = currentId != null && other.getId() != null && other.getId().equals(currentId);
        throwIf(!sameRecord,
                () -> new DuplicateResourceException("Academic year with name '" + yearName + "' already exists"));
    }

    private void applyUpdates(AcademicYear existing, AcademicYear data) {
        if (data == null) {
            return;
        }

        ifNotNull(data.getYearName(), newName -> {
            String oldName = existing.getYearName();
            if (oldName == null || !newName.equals(oldName)) {
                requireUniqueYearName(newName, existing.getId());
            }
            existing.setYearName(newName);
        });

        ifNotNull(data.getTotalCreditHours(), existing::setTotalCreditHours);
        ifNotNull(data.getElementarySchoolHours(), existing::setElementarySchoolHours);
        ifNotNull(data.getMiddleSchoolHours(), existing::setMiddleSchoolHours);
        ifNotNull(data.getBudgetAnnouncementDate(), existing::setBudgetAnnouncementDate);
        ifNotNull(data.getAllocationDeadline(), existing::setAllocationDeadline);
        ifNotNull(data.getIsLocked(), existing::setIsLocked);
    }

    private <T> void ifNotNull(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    private void throwIf(boolean condition, Supplier<? extends RuntimeException> exSupplier) {
        if (condition) {
            throw exSupplier.get();
        }
    }
}
