package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.entity.SubjectCategory;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link SubjectService}.
 * <p>
 * Validates subject CRUD operations, sorting, and cascade deletion behavior.
 * </p>
 */
@SpringBootTest(properties = "spring.sql.init.mode=never")
@ActiveProfiles("test")
@Transactional
class SubjectServiceTest {

    private final SubjectService subjectService;
    private final SubjectRepository subjectRepository;
    private final SubjectCategoryRepository subjectCategoryRepository;
    private final InternshipDemandRepository internshipDemandRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;
    private final EntityManager entityManager;

    private SubjectCategory testCategory;

    @Autowired
    SubjectServiceTest(SubjectService subjectService,
                       SubjectRepository subjectRepository,
                       SubjectCategoryRepository subjectCategoryRepository,
                       InternshipDemandRepository internshipDemandRepository,
                       TeacherAssignmentRepository teacherAssignmentRepository,
                       TeacherSubjectRepository teacherSubjectRepository,
                       EntityManager entityManager) {
        this.subjectService = subjectService;
        this.subjectRepository = subjectRepository;
        this.subjectCategoryRepository = subjectCategoryRepository;
        this.internshipDemandRepository = internshipDemandRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.teacherSubjectRepository = teacherSubjectRepository;
        this.entityManager = entityManager;
    }

    @BeforeEach
    void setUp() {
        teacherSubjectRepository.deleteAll();
        entityManager.flush();

        teacherAssignmentRepository.deleteAll();
        entityManager.flush();

        internshipDemandRepository.deleteAll();
        entityManager.flush();

        subjectRepository.deleteAll();
        entityManager.flush();

        subjectCategoryRepository.deleteAll();
        entityManager.flush();

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
    void getSortFieldsShouldReturnConfiguredFields() {
        List<Map<String, String>> sortFields = subjectService.getSortFields();

        assertTrue(sortFields.size() >= 4);
        assertEquals("id", sortFields.get(0).get("key"));
        assertEquals("subjectCode", sortFields.get(1).get("key"));
    }

    @Test
    void isRecordExistShouldDetectExistingCode() {
        createSubject("MATH101", "Mathematics");

        assertTrue(subjectService.isRecordExist("MATH101"));
        assertFalse(subjectService.isRecordExist("PHYS101"));
    }

    @Test
    void createShouldPersistNewSubject() {
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
    void createDuplicateCodeShouldThrowException() {
        createSubject("MATH101", "Mathematics");

        Subject duplicate = new Subject();
        duplicate.setSubjectCode("MATH101");
        duplicate.setSubjectTitle("Different Title");
        duplicate.setSubjectCategory(testCategory);

        assertThrows(DuplicateResourceException.class, () -> subjectService.create(duplicate));
    }

    @Test
    void getAllShouldReturnEverySubject() {
        createSubject("MATH101", "Mathematics");
        createSubject("PHYS101", "Physics");

        List<Subject> subjects = subjectService.getAll();

        assertEquals(2, subjects.size());
    }

    @Test
    void getByIdShouldReturnSubjectWhenPresent() {
        Subject saved = createSubject("MATH101", "Mathematics");

        Optional<Subject> result = subjectService.getById(saved.getId());

        assertTrue(result.isPresent());
        assertEquals(saved.getId(), result.get().getId());
    }
}
