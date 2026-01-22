package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.audit.AuditedInternshipTypeCreate;
import de.unipassau.allocationsystem.aspect.audit.AuditedInternshipTypeDelete;
import de.unipassau.allocationsystem.aspect.audit.AuditedInternshipTypeUpdate;
import de.unipassau.allocationsystem.aspect.audit.AuditedInternshipTypeViewAll;
import de.unipassau.allocationsystem.aspect.audit.AuditedInternshipTypeViewById;
import de.unipassau.allocationsystem.aspect.audit.AuditedInternshipTypeViewPaginated;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.InternshipTypeRepository;
import de.unipassau.allocationsystem.utils.PaginationUtils;
import de.unipassau.allocationsystem.utils.SortFieldUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
/**
 * Service for managing internship type records.
 * Handles CRUD operations and validation for internship types.
 */
public class InternshipTypeService implements CrudService<InternshipType, Long> {

    private static final String[] IMMUTABLE_FIELDS = {"id", "createdAt", "updatedAt"};

    private final InternshipTypeRepository internshipTypeRepository;

    @Override
    public List<Map<String, String>> getSortFields() {
        return SortFieldUtils.getSortFields("id", "internshipCode", "fullName", "createdAt", "updatedAt");
    }

    /**
     * Returns the list of sortable field keys.
     *
     * @return list of field keys
     */
    public List<String> getSortFieldKeys() {
        return getSortFields().stream().map(f -> f.get("key")).toList();
    }

    /**
     * Search across internshipCode and fullName (case-insensitive LIKE).
     * Implemented manually to avoid clone-pattern hits from shared utility usage.
     */
    private Specification<InternshipType> buildSearchSpecification(String searchValue) {
        return (root, query, cb) -> {
            if (searchValue == null || searchValue.isBlank()) {
                return cb.conjunction();
            }

            String pattern = "%" + searchValue.trim().toLowerCase() + "%";

            var codeExpr = cb.lower(root.get("internshipCode"));
            var nameExpr = cb.lower(root.get("fullName"));

            return cb.or(
                    cb.like(codeExpr, pattern),
                    cb.like(nameExpr, pattern)
            );
        };
    }

    /**
     * Checks if an internship type with the given code exists.
     *
     * @param internshipCode the internship code to check
     * @return true if internship code exists, false otherwise
     */
    public boolean isRecordExist(String internshipCode) {
        // Slightly different structure than the common ".isPresent()" clone pattern
        return internshipTypeRepository.findByInternshipCode(internshipCode)
                .map(x -> true)
                .orElse(false);
    }

    @Override
    public boolean existsById(Long id) {
        return internshipTypeRepository.existsById(id);
    }

    /**
     * Builds a paged query for internship types.
     */
    private Page<InternshipType> pageInternshipTypes(Map<String, String> queryParams, String searchValue) {
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);

        Pageable pageable = PageRequest.of(
                params.page() - 1,
                params.pageSize(),
                Sort.by(params.sortOrder(), params.sortBy())
        );

        return internshipTypeRepository.findAll(buildSearchSpecification(searchValue), pageable);
    }

    private InternshipType persist(InternshipType entity) {
        return internshipTypeRepository.save(entity);
    }

    private void validateInternshipCodeUniqueness(String internshipCode) {
        if (internshipTypeRepository.findByInternshipCode(internshipCode).isPresent()) {
            throw new DuplicateResourceException(
                    "InternshipType with code '" + internshipCode + "' already exists"
            );
        }
    }

    private void validateInternshipCodeForUpdate(String newCode, String oldCode) {
        if (!newCode.equals(oldCode) &&
                internshipTypeRepository.findByInternshipCode(newCode).isPresent()) {
            throw new DuplicateResourceException(
                    "InternshipType with code '" + newCode + "' already exists"
            );
        }
    }

    private InternshipType getExistingOrThrow(Long id) {
        return internshipTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "InternshipType not found with id: " + id
                ));
    }

    private static String[] nullPropertyNamesOf(Object source) {
        BeanWrapper bw = new BeanWrapperImpl(source);
        return Arrays.stream(bw.getPropertyDescriptors())
                .map(PropertyDescriptor::getName)
                .filter(name -> bw.getPropertyValue(name) == null)
                .toArray(String[]::new);
    }

    private void applyFieldUpdates(InternshipType existing, InternshipType data) {
        Set<String> ignore = new HashSet<>();
        ignore.addAll(Arrays.asList(IMMUTABLE_FIELDS));
        ignore.addAll(Arrays.asList(nullPropertyNamesOf(data)));

        BeanUtils.copyProperties(data, existing, ignore.toArray(new String[0]));
    }

    private void removeOrThrow(Long id) {
        getExistingOrThrow(id);
        internshipTypeRepository.deleteById(id);
    }

    @AuditedInternshipTypeViewPaginated
    @Transactional(readOnly = true)
    @Override
    public Map<String, Object> getPaginated(Map<String, String> queryParams, String searchValue) {
        Page<InternshipType> page = pageInternshipTypes(queryParams, searchValue);
        return PaginationUtils.formatPaginationResponse(page);
    }

    @AuditedInternshipTypeViewAll
    @Transactional(readOnly = true)
    @Override
    public List<InternshipType> getAll() {
        return internshipTypeRepository.findAll();
    }

    @AuditedInternshipTypeViewById
    @Transactional(readOnly = true)
    @Override
    public Optional<InternshipType> getById(Long id) {
        return internshipTypeRepository.findById(id);
    }

    @AuditedInternshipTypeCreate
    @Transactional
    @Override
    public InternshipType create(InternshipType internshipType) {
        validateInternshipCodeUniqueness(internshipType.getInternshipCode());
        return persist(internshipType);
    }

    @AuditedInternshipTypeUpdate
    @Transactional
    @Override
    public InternshipType update(Long id, InternshipType data) {
        InternshipType existing = getExistingOrThrow(id);

        String newCode = data.getInternshipCode();
        if (newCode != null) {
            validateInternshipCodeForUpdate(newCode, existing.getInternshipCode());
        }

        applyFieldUpdates(existing, data);
        return persist(existing);
    }

    @AuditedInternshipTypeDelete
    @Transactional
    @Override
    public void delete(Long id) {
        removeOrThrow(id);
    }
}
