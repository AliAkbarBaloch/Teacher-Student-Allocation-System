package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.repository.InternshipTypeRepository;
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
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
/**
 * Service for managing internship type records.
 * Handles CRUD operations and validation for internship types.
 */
public class InternshipTypeService implements CrudService<InternshipType, Long> {

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
        if (internshipTypeRepository.findByInternshipCode(internshipType.getInternshipCode()).isPresent()) {
            throw new DuplicateResourceException("InternshipType with code '" + internshipType.getInternshipCode() + "' already exists");
        }
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
        InternshipType existing = internshipTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InternshipType not found with id: " + id));

        if (data.getInternshipCode() != null && !data.getInternshipCode().equals(existing.getInternshipCode())) {
            if (internshipTypeRepository.findByInternshipCode(data.getInternshipCode()).isPresent()) {
                throw new DuplicateResourceException("InternshipType with code '" + data.getInternshipCode() + "' already exists");
            }
            existing.setInternshipCode(data.getInternshipCode());
        }
        if (data.getFullName() != null) {
            existing.setFullName(data.getFullName());
        }
        if (data.getTiming() != null) {
            existing.setTiming(data.getTiming());
        }
        if (data.getPeriodType() != null) {
            existing.setPeriodType(data.getPeriodType());
        }
        if (data.getSemester() != null) {
            existing.setSemester(data.getSemester());
        }
        if (data.getIsSubjectSpecific() != null) {
            existing.setIsSubjectSpecific(data.getIsSubjectSpecific());
        }
        if (data.getPriorityOrder() != null) {
            existing.setPriorityOrder(data.getPriorityOrder());
        }

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
        if (!internshipTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException("InternshipType not found with id: " + id);
        }
        internshipTypeRepository.deleteById(id);
    }
}