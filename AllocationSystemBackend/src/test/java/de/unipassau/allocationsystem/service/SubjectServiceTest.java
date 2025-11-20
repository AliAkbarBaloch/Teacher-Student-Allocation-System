package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.entity.SubjectCategory;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.SubjectCategoryRepository;
import de.unipassau.allocationsystem.repository.SubjectRepository;
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
class SubjectServiceTest {

    @Autowired
    private SubjectService subjectService;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private SubjectCategoryRepository subjectCategoryRepository;

    private SubjectCategory testCategory;

    @BeforeEach
    void setUp() {
        subjectRepository.deleteAll();
        subjectCategoryRepository.deleteAll();
        
        testCategory = new SubjectCategory();
        testCategory.setCategoryTitle("Mathematics");
        testCategory = subjectCategoryRepository.save(testCategory);
    }

    private Subject createSubject(String code, String title) {
        Subject subject = new Subject();
        subject.setSubjectCode(code);
        subject.setSubjectTitle(title);
        subject.setSubjectCategory(testCategory);
        subject.setSchoolType("Elementary");
        subject.setIsActive(true);
        return subjectRepository.save(subject);
    }

    @Test
    void getSortFields_ShouldReturnConfiguredFields() {
        List<Map<String, String>> sortFields = subjectService.getSortFields();

        assertTrue(sortFields.size() >= 4);
        assertEquals("id", sortFields.get(0).get("key"));
        assertEquals("subjectCode", sortFields.get(1).get("key"));
    }

    @Test
    void isRecordExist_ShouldDetectExistingCode() {
        createSubject("MATH101", "Mathematics");

        assertTrue(subjectService.isRecordExist("MATH101"));
        assertFalse(subjectService.isRecordExist("PHYS101"));
    }

    @Test
    void create_ShouldPersistNewSubject() {
        Subject subject = new Subject();
        subject.setSubjectCode("MATH101");
        subject.setSubjectTitle("Mathematics");
        subject.setSubjectCategory(testCategory);
        subject.setSchoolType("Elementary");
        subject.setIsActive(true);

        Subject saved = subjectService.create(subject);

        assertNotNull(saved.getId());
        assertEquals("MATH101", saved.getSubjectCode());
        assertEquals("Mathematics", saved.getSubjectTitle());
        assertTrue(subjectRepository.existsById(saved.getId()));
    }

    @Test
    void create_DuplicateCode_ShouldThrowException() {
        createSubject("MATH101", "Mathematics");

        Subject duplicate = new Subject();
        duplicate.setSubjectCode("MATH101");
        duplicate.setSubjectTitle("Different Title");
        duplicate.setSubjectCategory(testCategory);

        assertThrows(DuplicateResourceException.class, () -> subjectService.create(duplicate));
    }

    @Test
    void getAll_ShouldReturnEverySubject() {
        createSubject("MATH101", "Mathematics");
        createSubject("PHYS101", "Physics");

        List<Subject> subjects = subjectService.getAll();

        assertEquals(2, subjects.size());
    }

    @Test
    void getById_ShouldReturnSubjectWhenPresent() {
        Subject saved = createSubject("MATH101", "Mathematics");

        Optional<Subject> result = subjectService.getById(saved.getId());

        assertTrue(result.isPresent());
        assertEquals("MATH101", result.get().getSubjectCode());
        assertEquals("Mathematics", result.get().getSubjectTitle());
    }

    @Test
    void update_ShouldModifySubjectFields() {
        Subject saved = createSubject("MATH101", "Old Title");

        Subject updates = new Subject();
        updates.setSubjectTitle("New Title");
        updates.setSchoolType("High School");
        updates.setIsActive(false);

        Subject updated = subjectService.update(saved.getId(), updates);

        assertEquals("New Title", updated.getSubjectTitle());
        assertEquals("High School", updated.getSchoolType());
        assertEquals(false, updated.getIsActive());
        // Code should remain unchanged if not provided
        assertEquals("MATH101", updated.getSubjectCode());
    }

    @Test
    void update_DuplicateCode_ShouldThrowException() {
        Subject first = createSubject("MATH101", "Mathematics");
        createSubject("PHYS101", "Physics");

        Subject updates = new Subject();
        updates.setSubjectCode("PHYS101");

        assertThrows(DuplicateResourceException.class, () -> subjectService.update(first.getId(), updates));
    }

    @Test
    void update_NotFound_ShouldThrowException() {
        Subject updates = new Subject();
        updates.setSubjectTitle("Unknown");

        assertThrows(ResourceNotFoundException.class, () -> subjectService.update(999L, updates));
    }

    @Test
    void delete_ShouldRemoveSubject() {
        Subject saved = createSubject("MATH101", "To Delete");

        subjectService.delete(saved.getId());

        assertFalse(subjectRepository.existsById(saved.getId()));
    }

    @Test
    void delete_NotFound_ShouldThrowException() {
        assertThrows(ResourceNotFoundException.class, () -> subjectService.delete(999L));
    }

    @Test
    void getPaginated_ShouldRespectPagingAndSearch() {
        createSubject("MATH101", "Mathematics");
        createSubject("PHYS101", "Physics");
        createSubject("CHEM101", "Chemistry");

        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        params.put("pageSize", "2");
        params.put("sortBy", "subjectCode");
        params.put("sortOrder", "asc");

        Map<String, Object> result = subjectService.getPaginated(params, "MATH");

        assertEquals(1, result.get("page"));
        assertEquals(2, result.get("pageSize"));

        @SuppressWarnings("unchecked")
        List<Subject> items = (List<Subject>) result.get("items");
        assertEquals(1, items.size());
        assertTrue(items.get(0).getSubjectCode().contains("MATH"));
    }

    @Test
    void getPaginated_ShouldSearchByTitle() {
        createSubject("MATH101", "Mathematics");
        createSubject("PHYS101", "Physics");
        createSubject("MATH201", "Advanced Mathematics");

        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        params.put("pageSize", "10");
        params.put("sortBy", "subjectTitle");
        params.put("sortOrder", "asc");

        Map<String, Object> result = subjectService.getPaginated(params, "Math");

        @SuppressWarnings("unchecked")
        List<Subject> items = (List<Subject>) result.get("items");
        assertTrue(items.size() >= 2);
        assertTrue(items.stream().anyMatch(s -> s.getSubjectTitle().contains("Mathematics")));
    }

    @Test
    void getPaginated_ShouldSearchBySchoolType() {
        createSubject("MATH101", "Mathematics");
        Subject phys = createSubject("PHYS101", "Physics");
        phys.setSchoolType("High School");
        subjectRepository.save(phys);

        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        params.put("pageSize", "10");
        params.put("sortBy", "subjectCode");
        params.put("sortOrder", "asc");

        Map<String, Object> result = subjectService.getPaginated(params, "High");

        @SuppressWarnings("unchecked")
        List<Subject> items = (List<Subject>) result.get("items");
        assertTrue(items.size() >= 1);
        assertTrue(items.stream().anyMatch(s -> s.getSchoolType() != null && s.getSchoolType().contains("High")));
    }
}

