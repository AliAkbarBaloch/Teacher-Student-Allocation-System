package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.entity.AuditLog;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.InternshipTypeRepository;
import de.unipassau.allocationsystem.utils.PaginationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InternshipTypeService {

    private final InternshipTypeRepository internshipTypeRepository;

    public List<Map<String, String>> getSortFields() {
        List<Map<String, String>> fields = new ArrayList<>();
        fields.add(Map.of("key", "id", "label", "ID"));
        fields.add(Map.of("key", "internshipCode", "label", "Code"));
        fields.add(Map.of("key", "fullName", "label", "Full Name"));
        fields.add(Map.of("key", "createdAt", "label", "Creation Date"));
        fields.add(Map.of("key", "updatedAt", "label", "Last Updated"));
        return fields;
    }

    private Specification<InternshipType> buildSearchSpecification(String searchValue) {
        if (searchValue == null || searchValue.trim().isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }
        String likePattern = "%" + searchValue.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("internshipCode")), likePattern),
                cb.like(cb.lower(root.get("fullName")), likePattern)
        );
    }

    @Transactional
    public Map<String, Object> getPaginated(Map<String, String> queryParams, boolean includeRelations, String searchValue) {
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        Pageable pageable = PageRequest.of(params.page() - 1, params.pageSize(), sort);

        Specification<InternshipType> spec = buildSearchSpecification(searchValue);
        Page<InternshipType> page = internshipTypeRepository.findAll(spec, pageable);

        return PaginationUtils.formatPaginationResponse(page);
    }

    public List<InternshipType> getAll() {
        return internshipTypeRepository.findAll();
    }

    public Optional<InternshipType> getById(Long id) {
        return internshipTypeRepository.findById(id);
    }

    @Audited(
            action = AuditLog.AuditAction.CREATE,
            entityName = "INTERNSHIP_TYPE",
            description = "Created new InternshipType",
            captureNewValue = true
    )
    @Transactional
    public InternshipType create(InternshipType internshipType) {
        if (internshipTypeRepository.findByInternshipCode(internshipType.getInternshipCode()).isPresent()) {
            throw new DuplicateResourceException("InternshipType with code '" + internshipType.getInternshipCode() + "' already exists");
        }
        return internshipTypeRepository.save(internshipType);
    }

    @Audited(
            action = AuditLog.AuditAction.UPDATE,
            entityName = "INTERNSHIP_TYPE",
            description = "Updated Internship Type information",
            captureNewValue = true
    )
    @Transactional
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
            action = AuditLog.AuditAction.DELETE,
            entityName = "INTERNSHIP_TYPE",
            description = "Deleted INTERNSHIP_TYPE status",
            captureNewValue = true
    )
    @Transactional
    public void delete(Long id) {
        if (!internshipTypeRepository.existsById(id)) {
            throw new ResourceNotFoundException("InternshipType not found with id: " + id);
        }
        internshipTypeRepository.deleteById(id);
    }
}
