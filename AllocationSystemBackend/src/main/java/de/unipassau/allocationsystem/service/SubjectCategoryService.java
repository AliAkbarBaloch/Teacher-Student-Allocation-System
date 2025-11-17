package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.entity.SubjectCategory;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.SubjectCategoryRepository;
import de.unipassau.allocationsystem.utils.PaginationUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SubjectCategoryService {

    @Autowired
    private SubjectCategoryRepository subjectCategoryRepository;

    public boolean categoryTitleExists(String categoryTitle) {
        return subjectCategoryRepository.findByCategoryTitle(categoryTitle).isPresent();
    }

    public List<Map<String, String>> getSortFields() {
        List<Map<String, String>> fields = new ArrayList<>();
        fields.add(Map.of("key", "id", "label", "ID"));
        fields.add(Map.of("key", "categoryTitle", "label", "Category Title"));
        fields.add(Map.of("key", "createdAt", "label", "Creation Date"));
        fields.add(Map.of("key", "updatedAt", "label", "Last Updated"));
        return fields;
    }

    private Specification<SubjectCategory> buildSearchSpecification(String searchValue) {
        if (searchValue == null || searchValue.trim().isEmpty()) {
            return (root, query, cb) -> cb.conjunction();
        }
        String likePattern = "%" + searchValue.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.like(cb.lower(root.get("categoryTitle")), likePattern);
    }

    @Transactional
    public Map<String, Object> getPaginated(Map<String, String> queryParams, boolean includeRelations, String searchValue) {
        PaginationUtils.PaginationParams params = PaginationUtils.validatePaginationParams(queryParams);
        Sort sort = Sort.by(params.sortOrder(), params.sortBy());
        Pageable pageable = PageRequest.of(params.page() - 1, params.pageSize(), sort);

        Specification<SubjectCategory> spec = buildSearchSpecification(searchValue);
        Page<SubjectCategory> page = subjectCategoryRepository.findAll(spec, pageable);

        return PaginationUtils.formatPaginationResponse(page);
    }

    public List<SubjectCategory> getAll() {
        return subjectCategoryRepository.findAll();
    }

    public Optional<SubjectCategory> getById(Long id) {
        return subjectCategoryRepository.findById(id);
    }

    @Transactional
    public SubjectCategory create(SubjectCategory subjectCategory) {
        if (subjectCategoryRepository.findByCategoryTitle(subjectCategory.getCategoryTitle()).isPresent()) {
            throw new DuplicateResourceException("Subject category with title '" + subjectCategory.getCategoryTitle() + "' already exists");
        }
        return subjectCategoryRepository.save(subjectCategory);
    }

    @Transactional
    public SubjectCategory update(Long id, SubjectCategory data) {
        SubjectCategory existing = subjectCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject category not found with id: " + id));

        if (data.getCategoryTitle() != null && !data.getCategoryTitle().equals(existing.getCategoryTitle())) {
            if (subjectCategoryRepository.findByCategoryTitle(data.getCategoryTitle()).isPresent()) {
                throw new DuplicateResourceException("Subject category with title '" + data.getCategoryTitle() + "' already exists");
            }
            existing.setCategoryTitle(data.getCategoryTitle());
        }

        return subjectCategoryRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!subjectCategoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Subject category not found with id: " + id);
        }
        subjectCategoryRepository.deleteById(id);
    }
}

