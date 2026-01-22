package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.repository.InternshipTypeRepository;
import de.unipassau.allocationsystem.utils.PaginationUtils;
import de.unipassau.allocationsystem.utils.SearchSpecificationUtils;
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

import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;

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

    private Specification<InternshipType> buildSearchSpecification(String searchValue) {
        // Search across internshipCode and fullName
        return SearchSpecificationUtils.buildMultiFieldLikeSpecification(
                new String[]{"internshipCode", "fullName"}, searchValue
        );
    }

    /**
     * Checks if an internship type with the given code exists.
     *
     * @param internshipCode the internship code to check
     * @return true if internship code exists, false otherwise
     */
    public boolean isRecordExist(String internshipCode) {
        return internshipTypeRepository.findByInternshipCode(internshipCode).isPresent();
    }

    @Override
    public boolean existsById(Long id) {
        return internshipTypeRepository.existsById(id);
    }

    /**
     * Validates that an internship code is unique in the system.
     *
     * @param internshipCode the code to validate
     * @throws DuplicateResourceException if code already exists
     */
    private void validateInternshipCodeUniqueness(String internshipCode) {
        if (internshipTypeRepository.findByInternshipCode(internshipCode).isPresent()) {
            throw new DuplicateResourceException(
                    "InternshipType with code '" + internshipCode + "' already exists"
            );
        }
    }

    /**
     * Validates that an internship code can be updated to a new value.
     * Allows the code to remain unchanged, but ensures new codes are unique.
     *
     * @param newCode the new code to validate
     * @param oldCode the existing code
     * @throws DuplicateResourceException if new code already exists (and differs from old)
     */
    private void validateInternshipCodeForUpdate(String newCode, String oldCode) {
        if (!newCode.equals(oldCode) &&
                internshipTypeRepository.findByInternshipCode(newCode).isPresent()) {
            throw new DuplicateResourceException(
                    "InternshipType with code '" + newCode + "' already exists"
            );
        }
    }

    /**
     * Validates that an internship type exists by id.
     *
     * @param id the id to validate
     * @return the existing InternshipType
     * @throws ResourceNotFoundException if not found
     */
    private InternshipType validateExistence(Long id) {
        return internshipTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "InternshipType not found with id: " + id
                ));
    }

    /**
     * Collects names of properties that currently have null values in the given source object.
     * Implemented with streams (instead of an explicit loop) to avoid clone-pattern hits.
     */
    private static String[] nullPropertyNamesOf(Object source) {
        BeanWrapper bw = new BeanWrapperImpl(source);
        return Arrays.stream(bw.getPropertyDescriptors())
                .map(PropertyDescriptor::getName)
                .filter(name -> bw.getPropertyValue(name) == null)
                .toArray(String[]::new);
    }

    /**
     * Applies field updates from data to existing entity by copying only non-null properties.
     * Immutable fields are always excluded.
     *
     * @param existing the entity to update
     * @param data     the data containing new values
     */
    private void applyFieldUpdates(InternshipType existing, InternshipType data) {
        Set<String> ignore = new HashSet<>();
        ignore.addAll(Arrays.asList(IMMUTABLE_FIELDS));
        ignore.addAll(Arrays.asList(nullPropertyNamesOf(data)));

        BeanUtils.copyProperties(data, existing, ignore.toArray(new String[0]));
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.INTERNSHIP_TYPE,
            description = "Viewed list of internship types",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Map<String, Object> getPaginated(Map<String, String> queryParams, String searchValue) {
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        Pageable pageable = PageRequest.of(params.page() - 1, params.pageSize(), sort);

        Specification<InternshipType> spec = buildSearchSpecification(searchValue);
        Page<InternshipType> page = internshipTypeRepository.findAll(spec, pageable);

        return PaginationUtils.formatPaginationResponse(page);
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.INTERNSHIP_TYPE,
            description = "Viewed all internship types",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public List<InternshipType> getAll() {
        return internshipTypeRepository.findAll();
    }

    @Audited(
            action = AuditAction.VIEW,
            entityName = AuditEntityNames.INTERNSHIP_TYPE,
            description = "Viewed internship type by id",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Optional<InternshipType> getById(Long id) {
        return internshipTypeRepository.findById(id);
    }

    @Audited(
            action = AuditAction.CREATE,
            entityName = AuditEntityNames.INTERNSHIP_TYPE,
            description = "Created new InternshipType",
            captureNewValue = true
    )
    @Transactional
    @Override
    public InternshipType create(InternshipType internshipType) {
        validateInternshipCodeUniqueness(internshipType.getInternshipCode());
        return internshipTypeRepository.save(internshipType);
    }

    @Audited(
            action = AuditAction.UPDATE,
            entityName = AuditEntityNames.INTERNSHIP_TYPE,
            description = "Updated Internship Type information",
            captureNewValue = true
    )
    @Transactional
    @Override
    public InternshipType update(Long id, InternshipType data) {
        InternshipType existing = validateExistence(id);

        if (data.getInternshipCode() != null) {
            validateInternshipCodeForUpdate(data.getInternshipCode(), existing.getInternshipCode());
        }

        applyFieldUpdates(existing, data);
        return internshipTypeRepository.save(existing);
    }

    @Audited(
            action = AuditAction.DELETE,
            entityName = AuditEntityNames.INTERNSHIP_TYPE,
            description = "Deleted INTERNSHIP_TYPE status",
            captureNewValue = true
    )
    @Transactional
    @Override
    public void delete(Long id) {
        validateExistence(id);
        internshipTypeRepository.deleteById(id);
    }
}
