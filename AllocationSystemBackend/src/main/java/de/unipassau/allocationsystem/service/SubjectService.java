package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.entity.AuditLog;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.SubjectRepository;
import de.unipassau.allocationsystem.utils.PaginationUtils;
import de.unipassau.allocationsystem.utils.SearchSpecificationUtils;
import de.unipassau.allocationsystem.utils.SortFieldUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
public class SubjectService implements CrudService<Subject, Long> {

    private final SubjectRepository subjectRepository;

    @Override
    public List<Map<String, String>> getSortFields() {
        return SortFieldUtils.getSortFields(
                "id", "subjectCode", "subjectTitle", "schoolType", "isActive", "createdAt", "updatedAt"
        );
    }

    public List<String> getSortFieldKeys() {
        return getSortFields().stream().map(x -> x.get("key")).toList();
    }

    private Specification<Subject> searchSpec(String searchValue) {
        return SearchSpecificationUtils.buildMultiFieldLikeSpecification(
                new String[]{"subjectCode", "subjectTitle", "schoolType"}, searchValue
        );
    }

    public boolean isRecordExist(String subjectCode) {
        return subjectRepository.findBySubjectCode(subjectCode).isPresent();
    }

    private void rejectDuplicateCodeOnCreate(String code) {
        if (code != null && subjectRepository.findBySubjectCode(code).isPresent()) {
            throw new DuplicateResourceException("Subject with code '" + code + "' already exists");
        }
    }

    private void rejectDuplicateCodeOnUpdate(Subject existing, String incomingCode) {
        if (incomingCode == null) return;
        if (incomingCode.equals(existing.getSubjectCode())) return;

        Optional<Subject> match = subjectRepository.findBySubjectCode(incomingCode);
        if (match.isPresent() && match.get().getId() != null && !match.get().getId().equals(existing.getId())) {
            throw new DuplicateResourceException("Subject with code '" + incomingCode + "' already exists");
        }
    }

    private Subject requireSubject(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + id));
    }

    @Override
    public boolean existsById(Long id) {
        return subjectRepository.existsById(id);
    }

    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.SUBJECT,
            description = "Viewed list of subjects",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Map<String, Object> getPaginated(Map<String, String> queryParams, String searchValue) {
        PaginationUtils.PaginationParams p = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(p.sortOrder(), p.sortBy());
        PageRequest req = PageRequest.of(p.page() - 1, p.pageSize(), sort);

        Page<Subject> page = subjectRepository.findAll(searchSpec(searchValue), req);
        return PaginationUtils.formatPaginationResponse(page);
    }

    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.SUBJECT,
            description = "Viewed all subjects",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public List<Subject> getAll() {
        return subjectRepository.findAll();
    }

    @Audited(
            action = AuditLog.AuditAction.VIEW,
            entityName = AuditEntityNames.SUBJECT,
            description = "Viewed subject by id",
            captureNewValue = false
    )
    @Transactional(readOnly = true)
    @Override
    public Optional<Subject> getById(Long id) {
        return subjectRepository.findById(id);
    }

    @Audited(
            action = AuditLog.AuditAction.CREATE,
            entityName = AuditEntityNames.SUBJECT,
            description = "Created new subject",
            captureNewValue = true
    )
    @Override
    public Subject create(Subject subject) {
        rejectDuplicateCodeOnCreate(subject.getSubjectCode());
        return subjectRepository.save(subject);
    }

    @Audited(
            action = AuditLog.AuditAction.UPDATE,
            entityName = AuditEntityNames.SUBJECT,
            description = "Updated subject information",
            captureNewValue = true
    )
    @Override
    public Subject update(Long id, Subject data) {
        Subject existing = requireSubject(id);

        String incomingCode = data.getSubjectCode();
        if (incomingCode != null) {
            rejectDuplicateCodeOnUpdate(existing, incomingCode);
            existing.setSubjectCode(incomingCode);
        }

        String incomingTitle = data.getSubjectTitle();
        if (incomingTitle != null) existing.setSubjectTitle(incomingTitle);
        if (data.getSubjectCategory() != null) existing.setSubjectCategory(data.getSubjectCategory());
        if (data.getSchoolType() != null) existing.setSchoolType(data.getSchoolType());
        if (data.getIsActive() != null) existing.setIsActive(data.getIsActive());

        return subjectRepository.save(existing);
    }

    @Audited(
            action = AuditLog.AuditAction.DELETE,
            entityName = AuditEntityNames.SUBJECT,
            description = "Deleted subject",
            captureNewValue = false
    )
    @Override
    public void delete(Long id) {
        // same behavior: 404 if missing, then delete
        Subject existing = requireSubject(id);
        subjectRepository.deleteById(existing.getId());
    }
}
