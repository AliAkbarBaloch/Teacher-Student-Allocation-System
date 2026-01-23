package de.unipassau.allocationsystem.allocation;

import de.unipassau.allocationsystem.entity.AcademicYear;
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
 * Base integration test for {@link TeacherAllocationService}.
 * Persists a minimal valid dataset required for allocation before each test.
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional
abstract class TeacherAllocationServiceBaseTest {

    protected final TeacherAllocationTestDependencies deps;

    protected AcademicYear year;

    /**
     * Creates the base test with constructor injection.
     *
     * @param deps dependency wrapper for allocation tests
     */
    @Autowired
    protected TeacherAllocationServiceBaseTest(TeacherAllocationTestDependencies deps) {
        this.deps = deps;
    }

    @BeforeEach
    void setup() {
        TestSetupData data = buildSetupData();
        persistSetupData(data);
    }

    private TestSetupData buildSetupData() {
        AcademicYear createdYear = createAcademicYear();

        Schools schools = createSchools();
        Subjects subjects = createSubjects();
        InternshipTypes internshipTypes = createInternshipTypes();

        Teachers teachers = createTeachersWithQualifications(schools, subjects);
        attachAvailabilities(createdYear, internshipTypes, teachers);

        return new TestSetupData(createdYear, schools, subjects, internshipTypes, teachers);
    }

    private void persistSetupData(TestSetupData data) {
        this.year = deps.academicYearRepository().save(data.year());

        deps.schoolRepository().saveAll(java.util.List.of(data.schools().school1(), data.schools().school2()));
        deps.subjectCategoryRepository().save(data.subjects().category());
        deps.subjectRepository().saveAll(java.util.List.of(data.subjects().english(), data.subjects().social()));
        deps.subjectRepository().flush();

        deps.internshipTypeRepository().saveAll(java.util.List.of(
                data.internshipTypes().sfp(),
                data.internshipTypes().zsp(),
                data.internshipTypes().pdp1(),
                data.internshipTypes().pdp2()
        ));

        deps.teacherRepository().saveAll(java.util.List.of(
                data.teachers().t1(),
                data.teachers().t2(),
                data.teachers().t3()
        ));
        deps.teacherRepository().flush();

        createAndPersistTeacherSubjects(deps.teacherSubjectRepository(), data.year(), data.subjects(), data.teachers());
        createAndPersistDemands(deps.internshipDemandRepository(), data.year(), data.subjects(), data.internshipTypes());
    }
}
