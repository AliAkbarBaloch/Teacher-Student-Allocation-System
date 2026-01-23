package de.unipassau.allocationsystem.allocation;

import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.InternshipDemandRepository;
import de.unipassau.allocationsystem.repository.InternshipTypeRepository;
import de.unipassau.allocationsystem.repository.SchoolRepository;
import de.unipassau.allocationsystem.repository.SubjectCategoryRepository;
import de.unipassau.allocationsystem.repository.SubjectRepository;
import de.unipassau.allocationsystem.repository.TeacherAssignmentRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import de.unipassau.allocationsystem.repository.TeacherSubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import static de.unipassau.allocationsystem.allocation.TeacherAllocationTestDataFactory.InternshipTypes;
import static de.unipassau.allocationsystem.allocation.TeacherAllocationTestDataFactory.Schools;
import static de.unipassau.allocationsystem.allocation.TeacherAllocationTestDataFactory.Subjects;
import static de.unipassau.allocationsystem.allocation.TeacherAllocationTestDataFactory.Teachers;
import static de.unipassau.allocationsystem.allocation.TeacherAllocationTestDataFactory.TestSetupData;
import static de.unipassau.allocationsystem.allocation.TeacherAllocationTestDataFactory.attachAvailabilities;
import static de.unipassau.allocationsystem.allocation.TeacherAllocationTestDataFactory.createAcademicYear;
import static de.unipassau.allocationsystem.allocation.TeacherAllocationTestDataFactory.createAndPersistDemands;
import static de.unipassau.allocationsystem.allocation.TeacherAllocationTestDataFactory.createAndPersistTeacherSubjects;
import static de.unipassau.allocationsystem.allocation.TeacherAllocationTestDataFactory.createInternshipTypes;
import static de.unipassau.allocationsystem.allocation.TeacherAllocationTestDataFactory.createSchools;
import static de.unipassau.allocationsystem.allocation.TeacherAllocationTestDataFactory.createSubjects;
import static de.unipassau.allocationsystem.allocation.TeacherAllocationTestDataFactory.createTeachersWithQualifications;

/**
 * Base integration test for {@link TeacherAllocationService} scenarios.
 * <p>
 * This class bootstraps the Spring context and persists a minimal, valid dataset
 * (academic year, schools, subjects, internship types, teachers, availabilities,
 * teacher-subject mappings, and internship demands). Concrete test classes extend
 * this base and focus on asserting allocation outcomes.
 * </p>
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional
abstract class TeacherAllocationServiceBaseIT {

    /** Allocation service under test. */
    @Autowired
    protected TeacherAllocationService allocationService;

    /** Repository for persisting and loading academic years. */
    @Autowired
    protected AcademicYearRepository academicYearRepository;

    /** Repository for teacher-subject mappings. */
    @Autowired
    protected TeacherSubjectRepository teacherSubjectRepository;

    /** Repository for teachers. */
    @Autowired
    protected TeacherRepository teacherRepository;

    /** Repository for internship demands. */
    @Autowired
    protected InternshipDemandRepository internshipDemandRepository;

    /** Repository for created teacher assignments. */
    @Autowired
    protected TeacherAssignmentRepository teacherAssignmentRepository;

    /** Repository for internship types. */
    @Autowired
    protected InternshipTypeRepository internshipTypeRepository;

    /** Repository for subjects. */
    @Autowired
    protected SubjectRepository subjectRepository;

    /** Repository for schools. */
    @Autowired
    protected SchoolRepository schoolRepository;

    /** Repository for subject categories. */
    @Autowired
    protected SubjectCategoryRepository subjectCategoryRepository;

    /** Persisted academic year used by each test run. */
    protected AcademicYear year;

    /**
     * Persists a fresh dataset before each test method.
     * <p>
     * The context is dirtied before each test, so each test starts from a clean state.
     * </p>
     */
    @BeforeEach
    void setup() {
        TestSetupData data = buildSetupData();
        persistSetupData(data);
    }

    /**
     * Builds the in-memory test dataset used by the allocation integration tests.
     *
     * @return composed setup data containing year, schools, subjects, internship types and teachers
     */
    private TestSetupData buildSetupData() {
        AcademicYear createdYear = createAcademicYear();

        Schools schools = createSchools();
        Subjects subjects = createSubjects();
        InternshipTypes internshipTypes = createInternshipTypes();

        Teachers teachers = createTeachersWithQualifications(schools, subjects);
        attachAvailabilities(createdYear, internshipTypes, teachers);

        return new TestSetupData(createdYear, schools, subjects, internshipTypes, teachers);
    }

    /**
     * Persists the given dataset to the database in the correct order to satisfy FK constraints.
     *
     * @param data dataset created by {@link #buildSetupData()}
     */
    private void persistSetupData(TestSetupData data) {
        this.year = academicYearRepository.save(data.year());

        schoolRepository.saveAll(java.util.List.of(data.schools().school1(), data.schools().school2()));
        subjectCategoryRepository.save(data.subjects().category());
        subjectRepository.saveAll(java.util.List.of(data.subjects().english(), data.subjects().social()));
        subjectRepository.flush();

        internshipTypeRepository.saveAll(java.util.List.of(
                data.internshipTypes().sfp(),
                data.internshipTypes().zsp(),
                data.internshipTypes().pdp1(),
                data.internshipTypes().pdp2()
        ));

        teacherRepository.saveAll(java.util.List.of(
                data.teachers().t1(),
                data.teachers().t2(),
                data.teachers().t3()
        ));
        teacherRepository.flush();

        createAndPersistTeacherSubjects(teacherSubjectRepository, data.year(), data.subjects(), data.teachers());
        createAndPersistDemands(internshipDemandRepository, data.year(), data.subjects(), data.internshipTypes());
    }
}
