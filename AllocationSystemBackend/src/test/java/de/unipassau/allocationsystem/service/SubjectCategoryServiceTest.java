package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.entity.SubjectCategory;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.InternshipDemandRepository;
import de.unipassau.allocationsystem.repository.SubjectCategoryRepository;
import de.unipassau.allocationsystem.repository.SubjectRepository;
import de.unipassau.allocationsystem.repository.TeacherAssignmentRepository;
import de.unipassau.allocationsystem.repository.TeacherSubjectRepository;
import jakarta.persistence.EntityManager;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link SubjectCategoryService}.
 * <p>
 * Validates subject category CRUD operations, pagination, and cascade deletion behavior.
 * </p>
 */
@SpringBootTest(properties = "spring.sql.init.mode=never")
@ActiveProfiles("test")
@Transactional
class SubjectCategoryServiceTest {

    private final SubjectCategoryService subjectCategoryService;
    private final SubjectCategoryRepository subjectCategoryRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;
    private final InternshipDemandRepository internshipDemandRepository;
    private final EntityManager entityManager;

    @Autowired
    SubjectCategoryServiceTest(SubjectCategoryService subjectCategoryService,
                              SubjectCategoryRepository subjectCategoryRepository,
                              SubjectRepository subjectRepository,
                              TeacherAssignmentRepository teacherAssignmentRepository,
                              TeacherSubjectRepository teacherSubjectRepository,
                              InternshipDemandRepository internshipDemandRepository,
                              EntityManager entityManager) {
        this.subjectCategoryService = subjectCategoryService;
        this.subjectCategoryRepository = subjectCategoryRepository;
        this.subjectRepository = subjectRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.teacherSubjectRepository = teacherSubjectRepository;
        this.internshipDemandRepository = internshipDemandRepository;
        this.entityManager = entityManager;
    }

    @BeforeEach
    void setUp() {
        clearTestData();
    }

    /**
     * Clears dependent tables in a safe order to avoid FK constraint violations.
     */
    private void clearTestData() {
        List<Runnable> cleanupSteps = List.of(
                teacherSubjectRepository::deleteAll,
                teacherAssignmentRepository::deleteAll,
                internshipDemandRepository::deleteAll,
                subjectRepository::deleteAll,
                subjectCategoryRepository::deleteAll
        );

        for (Runnable step : cleanupSteps) {
            step.run();
            entityManager.flush();
        }
    }

    private SubjectCategory createCategory(String title) {
        SubjectCategory category = new SubjectCategory();
        category.setCategoryTitle(title);
        return subjectCategoryRepository.save(category);
    }

    @Test
    void getSortFieldsShouldReturnConfiguredFields() {
        List<Map<String, String>> sortFields = subjectCategoryService.getSortFields();

        assertEquals(4, sortFields.size());
        assertEquals("id", sortFields.get(0).get("key"));
        assertEquals("categoryTitle", sortFields.get(1).get("key"));
    }

    @Test
    void categoryTitleExistsShouldDetectExistingTitle() {
        createCategory("Mathematics");

        assertTrue(subjectCategoryService.categoryTitleExists("Mathematics"));
        assertFalse(subjectCategoryService.categoryTitleExists("Physics"));
    }

    @Test
    void createShouldPersistNewCategory() {
        SubjectCategory category = new SubjectCategory();
        category.setCategoryTitle("Biology");

        SubjectCategory saved = subjectCategoryService.create(category);

        assertNotNull(saved.getId());
        assertEquals("Biology", saved.getCategoryTitle());
        assertTrue(subjectCategoryRepository.existsById(saved.getId()));
    }

    @Test
    void createDuplicateTitleShouldThrowException() {
        createCategory("History");

        SubjectCategory duplicate = new SubjectCategory();
        duplicate.setCategoryTitle("History");

        assertThrows(DuplicateResourceException.class, () -> subjectCategoryService.create(duplicate));
    }

    @Test
    void getAllShouldReturnEveryCategory() {
        createCategory("Chemistry");
        createCategory("Physics");

        List<SubjectCategory> categories = subjectCategoryService.getAll();

        assertEquals(2, categories.size());
    }

    @Test
    void getByIdShouldReturnCategoryWhenPresent() {
        SubjectCategory saved = createCategory("Geography");

        Optional<SubjectCategory> result = subjectCategoryService.getById(saved.getId());

        assertTrue(result.isPresent());
        assertEquals("Geography", result.get().getCategoryTitle());
    }

    @Test
    void updateShouldModifyCategoryTitle() {
        SubjectCategory saved = createCategory("Old Title");

        SubjectCategory updates = new SubjectCategory();
        updates.setCategoryTitle("New Title");

        SubjectCategory updated = subjectCategoryService.update(saved.getId(), updates);

        assertEquals("New Title", updated.getCategoryTitle());
    }

    @Test
    void updateDuplicateTitleShouldThrowException() {
        SubjectCategory first = createCategory("Arts");
        createCategory("Music");

        SubjectCategory updates = new SubjectCategory();
        updates.setCategoryTitle("Music");

        assertThrows(DuplicateResourceException.class, () -> subjectCategoryService.update(first.getId(), updates));
    }

    @Test
    void updateNotFoundShouldThrowException() {
        SubjectCategory updates = new SubjectCategory();
        updates.setCategoryTitle("Unknown");

        assertThrows(ResourceNotFoundException.class, () -> subjectCategoryService.update(999L, updates));
    }

    @Test
    void deleteShouldRemoveCategory() {
        SubjectCategory saved = createCategory("To Delete");

        subjectCategoryService.delete(saved.getId());

        assertFalse(subjectCategoryRepository.existsById(saved.getId()));
    }

    @Test
    void deleteNotFoundShouldThrowException() {
        assertThrows(ResourceNotFoundException.class, () -> subjectCategoryService.delete(999L));
    }

    @Test
    void getPaginatedShouldRespectPagingAndSearch() {
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
        assertTrue(items.stream().allMatch(item ->
                item.getCategoryTitle().toLowerCase().contains("alpha")
        ));
    }
}
