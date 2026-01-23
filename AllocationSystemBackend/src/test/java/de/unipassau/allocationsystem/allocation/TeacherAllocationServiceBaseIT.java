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
 * Persists a minimal valid dataset required for allocation. Concrete test classes extend this base
 * and assert allocation outcomes.
 * </p>
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional
abstract class TeacherAllocationServiceBaseIT {

    /** Allocation service under test. */
    protected final TeacherAllocationService allocationService;

    /** Repository for persisting and loading academic years. */
    protected final AcademicYearRepository academicYearRepository;

    /** Repository for teacher-subject mappings. */
    protected final TeacherSubjectRepository teacherSubjectRepository;

    /** Repository for teachers. */
    protected final TeacherRepository teacherRepository;

    /** Repository for internship demands. */
    protected final InternshipDemandRepository internshipDemandRepository;

    /** Repository for created teacher assignments. */
    protected final TeacherAssignmentRepository teacherAssignmentRepository;

    /** Repository for internship types. */
    protected final InternshipTypeRepository internshipTypeRepository;

    /** Repository for subjects. */
    protected final SubjectRepository subjectRepository;

    /** Repository for schools. */
    protected final SchoolRepository schoolRepository;

    /** Repository for subject categories. */
    protected final SubjectCategoryRepository subjectCategoryRepository;

    /** Persisted academic year used by each test run. */
    protected AcademicYear year;

    /**
     * Creates the base integration test using constructor injection.
     */
    @Autowired
    protected TeacherAllocationServiceBaseIT(
            TeacherAllocationService allocationService,
            AcademicYearRepository academicYearRepository,
            TeacherSubjectRepository teacherSubjectRepository,
            TeacherRepository teacherRepository,
            InternshipDemandRepository internshipDemandRepository,
            TeacherAssignmentRepository teacherAssignmentRepository,
            InternshipTypeRepository internshipTypeRepository,
            SubjectRepository subjectRepository,
            SchoolRepository schoolRepository,
            SubjectCategoryRepository subjectCategoryRepository
    ) {
        this.allocationService = allocationService;
        this.academicYearRepository = academicYearRepository;
        this.teacherSubjectRepository = teacherSubjectRepository;
        this.teacherRepository = teacherRepository;
        this.internshipDemandRepository = internshipDemandRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.internshipTypeRepository = internshipTypeRepository;
        this.subjectRepository = subjectRepository;
        this.schoolRepository = schoolRepository;
        this.subjectCategoryRepository = subjectCategoryRepository;
    }

    /**
     * Inserts a fresh dataset before each test method.
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
     * Persists the given dataset to the database in an order compatible with FK constraints.
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
