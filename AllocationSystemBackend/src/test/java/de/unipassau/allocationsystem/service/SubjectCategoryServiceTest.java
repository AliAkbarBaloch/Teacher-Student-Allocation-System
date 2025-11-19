package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.entity.SubjectCategory;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.SubjectCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SubjectCategoryServiceTest {

    @Autowired
    private SubjectCategoryService subjectCategoryService;

    @Autowired
    private SubjectCategoryRepository subjectCategoryRepository;

    @BeforeEach
    void setUp() {
        subjectCategoryRepository.deleteAll();
    }

    private SubjectCategory createCategory(String title) {
        SubjectCategory category = new SubjectCategory();
        category.setCategoryTitle(title);
        return subjectCategoryRepository.save(category);
    }

    @Test
    void getSortFields_ShouldReturnConfiguredFields() {
        List<Map<String, String>> sortFields = subjectCategoryService.getSortFields();

        assertEquals(4, sortFields.size());
        assertEquals("id", sortFields.get(0).get("key"));
        assertEquals("categoryTitle", sortFields.get(1).get("key"));
    }

    @Test
    void categoryTitleExists_ShouldDetectExistingTitle() {
        createCategory("Mathematics");

        assertTrue(subjectCategoryService.categoryTitleExists("Mathematics"));
        assertFalse(subjectCategoryService.categoryTitleExists("Physics"));
    }

    @Test
    void create_ShouldPersistNewCategory() {
        SubjectCategory category = new SubjectCategory();
        category.setCategoryTitle("Biology");

        SubjectCategory saved = subjectCategoryService.create(category);

        assertNotNull(saved.getId());
        assertEquals("Biology", saved.getCategoryTitle());
        assertTrue(subjectCategoryRepository.existsById(saved.getId()));
    }

    @Test
    void create_DuplicateTitle_ShouldThrowException() {
        createCategory("History");

        SubjectCategory duplicate = new SubjectCategory();
        duplicate.setCategoryTitle("History");

        assertThrows(DuplicateResourceException.class, () -> subjectCategoryService.create(duplicate));
    }

    @Test
    void getAll_ShouldReturnEveryCategory() {
        createCategory("Chemistry");
        createCategory("Physics");

        List<SubjectCategory> categories = subjectCategoryService.getAll();

        assertEquals(2, categories.size());
    }

    @Test
    void getById_ShouldReturnCategoryWhenPresent() {
        SubjectCategory saved = createCategory("Geography");

        Optional<SubjectCategory> result = subjectCategoryService.getById(saved.getId());

        assertTrue(result.isPresent());
        assertEquals("Geography", result.get().getCategoryTitle());
    }

    @Test
    void update_ShouldModifyCategoryTitle() {
        SubjectCategory saved = createCategory("Old Title");

        SubjectCategory updates = new SubjectCategory();
        updates.setCategoryTitle("New Title");

        SubjectCategory updated = subjectCategoryService.update(saved.getId(), updates);

        assertEquals("New Title", updated.getCategoryTitle());
    }

    @Test
    void update_DuplicateTitle_ShouldThrowException() {
        SubjectCategory first = createCategory("Arts");
        createCategory("Music");

        SubjectCategory updates = new SubjectCategory();
        updates.setCategoryTitle("Music");

        assertThrows(DuplicateResourceException.class, () -> subjectCategoryService.update(first.getId(), updates));
    }

    @Test
    void update_NotFound_ShouldThrowException() {
        SubjectCategory updates = new SubjectCategory();
        updates.setCategoryTitle("Unknown");

        assertThrows(ResourceNotFoundException.class, () -> subjectCategoryService.update(999L, updates));
    }

    @Test
    void delete_ShouldRemoveCategory() {
        SubjectCategory saved = createCategory("To Delete");

        subjectCategoryService.delete(saved.getId());

        assertFalse(subjectCategoryRepository.existsById(saved.getId()));
    }

    @Test
    void delete_NotFound_ShouldThrowException() {
        assertThrows(ResourceNotFoundException.class, () -> subjectCategoryService.delete(999L));
    }

    @Test
    void getPaginated_ShouldRespectPagingAndSearch() {
        createCategory("Alpha");
        createCategory("Beta");
        createCategory("Alphabet Soup");

        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        params.put("pageSize", "2");
        params.put("sortBy", "categoryTitle");
        params.put("sortOrder", "asc");

        Map<String, Object> result = subjectCategoryService.getPaginated(params, "Alpha");

        assertEquals(1, result.get("page"));
        assertEquals(2, result.get("pageSize"));

        @SuppressWarnings("unchecked")
        List<SubjectCategory> items = (List<SubjectCategory>) result.get("items");
        assertEquals(2, items.size());
        assertTrue(items.stream().allMatch(item -> item.getCategoryTitle().toLowerCase().contains("alpha")));
    }
}

