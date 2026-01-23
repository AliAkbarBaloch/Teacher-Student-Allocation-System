package de.unipassau.allocationsystem.allocation;

import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.InternshipDemand;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.entity.SubjectCategory;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.TeacherAssignment;
import de.unipassau.allocationsystem.entity.TeacherAvailability;
import de.unipassau.allocationsystem.entity.TeacherQualification;
import de.unipassau.allocationsystem.entity.TeacherSubject;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the {@link TeacherAllocationService}.
 * <p>
 * This test class validates the teacher allocation functionality across different
 * internship types (SFP, ZSP, PDP1, PDP2) and ensures that allocation rules and
 * constraints are properly enforced during the allocation process.
 * </p>
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional
public class TeacherAllocationServiceIntegrationTest {

    /**
     * Simple dependency holder used to keep constructor injection while avoiding
     * constructors with too many parameters.
     */
    static final class Dependencies {

        private final TeacherAllocationService allocationService;
        private final AcademicYearRepository academicYearRepository;
        private final TeacherSubjectRepository teacherSubjectRepository;
        private final TeacherRepository teacherRepository;
        private final InternshipDemandRepository internshipDemandRepository;
        private final TeacherAssignmentRepository teacherAssignmentRepository;
        private final InternshipTypeRepository internshipTypeRepository;
        private final SubjectRepository subjectRepository;
        private final SchoolRepository schoolRepository;
        private final SubjectCategoryRepository subjectCategoryRepository;

        /**
         * Creates a new dependency holder.
         *
         * @param allocationService the allocation service under test
         * @param academicYearRepository repository for academic years
         * @param teacherSubjectRepository repository for teacher-subject mappings
         * @param teacherRepository repository for teachers
         * @param internshipDemandRepository repository for internship demands
         * @param teacherAssignmentRepository repository for teacher assignments
         * @param internshipTypeRepository repository for internship types
         * @param subjectRepository repository for subjects
         * @param schoolRepository repository for schools
         * @param subjectCategoryRepository repository for subject categories
         */
        @Autowired
        Dependencies(
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
         * @return the allocation service under test
         */
        TeacherAllocationService allocationService() {
            return allocationService;
        }

        /**
         * @return repository for academic years
         */
        AcademicYearRepository academicYearRepository() {
            return academicYearRepository;
        }

        /**
         * @return repository for teacher-subject mappings
         */
        TeacherSubjectRepository teacherSubjectRepository() {
            return teacherSubjectRepository;
        }

        /**
         * @return repository for teachers
         */
        TeacherRepository teacherRepository() {
            return teacherRepository;
        }

        /**
         * @return repository for internship demands
         */
        InternshipDemandRepository internshipDemandRepository() {
            return internshipDemandRepository;
        }

        /**
         * @return repository for teacher assignments
         */
        TeacherAssignmentRepository teacherAssignmentRepository() {
            return teacherAssignmentRepository;
        }

        /**
         * @return repository for internship types
         */
        InternshipTypeRepository internshipTypeRepository() {
            return internshipTypeRepository;
        }

        /**
         * @return repository for subjects
         */
        SubjectRepository subjectRepository() {
            return subjectRepository;
        }

        /**
         * @return repository for schools
         */
        SchoolRepository schoolRepository() {
            return schoolRepository;
        }

        /**
         * @return repository for subject categories
         */
        SubjectCategoryRepository subjectCategoryRepository() {
            return subjectCategoryRepository;
        }
    }

    private final Dependencies deps;

    private AcademicYear year;

    /**
     * Creates the integration test using constructor injection.
     *
     * @param deps all dependencies required by this test
     */
    @Autowired
    public TeacherAllocationServiceIntegrationTest(Dependencies deps) {
        this.deps = deps;
    }

    @BeforeEach
    void setup() {
        TestSetupData data = buildSetupData();
        persistSetupData(data);
    }

    @Test
    void testAllocateTeachersToSFP() {
        deps.allocationService().performAllocation(year.getId());

        List<TeacherAssignment> assignments = deps.teacherAssignmentRepository().findAll();
        assertThat(assignments)
                .anyMatch(a -> a.getInternshipType().getInternshipCode().startsWith("SFP"));
    }

    @Test
    void testAllocateTeachersToZSP() {
        deps.allocationService().performAllocation(year.getId());

        List<TeacherAssignment> assignments = deps.teacherAssignmentRepository().findAll();
        assertThat(assignments)
                .anyMatch(a -> a.getInternshipType().getInternshipCode().startsWith("ZSP"));
    }

    @Test
    void testAllocateTeachersToPDP1AndPDP2() {
        deps.allocationService().performAllocation(year.getId());

        List<TeacherAssignment> assignments = deps.teacherAssignmentRepository().findAll();
        assertThat(assignments)
                .anyMatch(a -> a.getInternshipType().getInternshipCode().startsWith("PDP1"));
        assertThat(assignments)
                .anyMatch(a -> a.getInternshipType().getInternshipCode().startsWith("PDP2"));
    }

    @Test
    void testInternshipCombinationRules() {
        // This test should be extended to set up combination rules and check enforcement
        deps.allocationService().performAllocation(year.getId());

        List<TeacherAssignment> assignments = deps.teacherAssignmentRepository().findAll();
        assertThat(assignments).isNotNull();
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

        deps.schoolRepository().saveAll(List.of(data.schools().school1(), data.schools().school2()));
        deps.subjectCategoryRepository().save(data.subjects().category());
        deps.subjectRepository().saveAll(List.of(data.subjects().english(), data.subjects().social()));
        deps.subjectRepository().flush();

        deps.internshipTypeRepository().saveAll(List.of(
                data.internshipTypes().sfp(),
                data.internshipTypes().zsp(),
                data.internshipTypes().pdp1(),
                data.internshipTypes().pdp2()
        ));

        deps.teacherRepository().saveAll(List.of(
                data.teachers().t1(),
                data.teachers().t2(),
                data.teachers().t3()
        ));
        deps.teacherRepository().flush();

        createAndPersistTeacherSubjects(data.year(), data.subjects(), data.teachers());
        createAndPersistDemands(data.year(), data.subjects(), data.internshipTypes());
    }

    private AcademicYear createAcademicYear() {
        AcademicYear createdYear = new AcademicYear();
        createdYear.setYearName("2025/2026-" + System.nanoTime()); // ensure unique year name
        createdYear.setIsLocked(false);
        createdYear.setTotalCreditHours(100); // required field
        createdYear.setElementarySchoolHours(40); // required field
        createdYear.setMiddleSchoolHours(60); // required field
        createdYear.setBudgetAnnouncementDate(LocalDateTime.now()); // required field
        createdYear.setAllocationDeadline(LocalDateTime.now().plusMonths(1)); // optional, but safe
        return createdYear;
    }

    private Schools createSchools() {
        School school1 = new School();
        school1.setSchoolName("School 1");
        school1.setZoneNumber(1);
        school1.setSchoolType(School.SchoolType.PRIMARY); // Required field

        School school2 = new School();
        school2.setSchoolName("School 2");
        school2.setZoneNumber(2);
        school2.setSchoolType(School.SchoolType.MIDDLE); // Required field

        return new Schools(school1, school2);
    }

    private Subjects createSubjects() {
        SubjectCategory category = new SubjectCategory();
        category.setCategoryTitle("General Studies");

        Subject english = new Subject();
        english.setSubjectCode("EN");
        english.setSubjectTitle("English");
        english.setSubjectCategory(category);
        english.setIsActive(true);

        Subject social = new Subject();
        social.setSubjectCode("SO");
        social.setSubjectTitle("Social Studies");
        social.setSubjectCategory(category);
        social.setIsActive(true);

        return new Subjects(category, english, social);
    }

    private InternshipTypes createInternshipTypes() {
        String uniqueSuffix = "-" + System.nanoTime();

        InternshipType sfp = createInternshipType("SFP", uniqueSuffix, "Schulpraktikum SFP", 1);
        InternshipType zsp = createInternshipType("ZSP", uniqueSuffix, "Zwischenpraktikum ZSP", 2);
        InternshipType pdp1 = createInternshipType("PDP1", uniqueSuffix, "Pädagogisch-didaktisches Praktikum 1", 3);
        InternshipType pdp2 = createInternshipType("PDP2", uniqueSuffix, "Pädagogisch-didaktisches Praktikum 2", 4);

        return new InternshipTypes(sfp, zsp, pdp1, pdp2);
    }

    private InternshipType createInternshipType(String codePrefix, String suffix, String fullName, int semester) {
        InternshipType type = new InternshipType();
        type.setInternshipCode(codePrefix + suffix);
        type.setFullName(fullName);
        type.setSemester(semester);
        return type;
    }

    private Teachers createTeachersWithQualifications(Schools schools, Subjects subjects) {
        Teacher t1 = createTeacher("Alice", "SFP", schools.school1(), "alice.sfp");
        addQualification(t1, subjects.english(), true);

        Teacher t2 = createTeacher("Bob", "ZSP", schools.school2(), "bob.zsp");
        addQualification(t2, subjects.social(), true);

        Teacher t3 = createTeacher("Carol", "PDP", schools.school2(), "carol.pdp");
        addQualification(t3, subjects.english(), true);

        return new Teachers(t1, t2, t3);
    }

    private Teacher createTeacher(String firstName, String lastName, School school, String emailPrefix) {
        Teacher teacher = new Teacher();
        teacher.setFirstName(firstName);
        teacher.setLastName(lastName);
        teacher.setSchool(school);
        teacher.setEmploymentStatus(Teacher.EmploymentStatus.ACTIVE);
        teacher.setEmail(emailPrefix + "." + System.nanoTime() + "@test.com");
        return teacher;
    }

    private void addQualification(Teacher teacher, Subject subject, boolean isMainSubject) {
        TeacherQualification qualification = new TeacherQualification();
        qualification.setTeacher(teacher);
        qualification.setSubject(subject);
        qualification.setIsMainSubject(isMainSubject);

        teacher.getQualifications().add(qualification);
    }

    private void attachAvailabilities(AcademicYear createdYear, InternshipTypes internshipTypes, Teachers teachers) {
        teachers.t1().getAvailabilities().add(createAvailability(createdYear, internshipTypes.sfp(), teachers.t1()));
        teachers.t2().getAvailabilities().add(createAvailability(createdYear, internshipTypes.zsp(), teachers.t2()));

        teachers.t3().getAvailabilities().add(createAvailability(createdYear, internshipTypes.pdp1(), teachers.t3()));
        teachers.t3().getAvailabilities().add(createAvailability(createdYear, internshipTypes.pdp2(), teachers.t3()));
    }

    private TeacherAvailability createAvailability(AcademicYear createdYear, InternshipType type, Teacher teacher) {
        TeacherAvailability availability = new TeacherAvailability();
        availability.setTeacher(teacher);
        availability.setAcademicYear(createdYear);
        availability.setInternshipType(type);
        availability.setStatus(TeacherAvailability.AvailabilityStatus.AVAILABLE);
        availability.setIsAvailable(true);
        return availability;
    }

    private void createAndPersistTeacherSubjects(AcademicYear createdYear, Subjects subjects, Teachers teachers) {
        TeacherSubject ts1 = createTeacherSubject(createdYear, teachers.t1(), subjects.english());
        TeacherSubject ts2 = createTeacherSubject(createdYear, teachers.t2(), subjects.social());
        TeacherSubject ts3 = createTeacherSubject(createdYear, teachers.t3(), subjects.english());

        deps.teacherSubjectRepository().saveAll(List.of(ts1, ts2, ts3));
        deps.teacherSubjectRepository().flush();
    }

    private TeacherSubject createTeacherSubject(AcademicYear createdYear, Teacher teacher, Subject subject) {
        TeacherSubject teacherSubject = new TeacherSubject();
        teacherSubject.setAcademicYear(createdYear);
        teacherSubject.setTeacher(teacher);
        teacherSubject.setSubject(subject);
        teacherSubject.setAvailabilityStatus("AVAILABLE");
        return teacherSubject;
    }

    private void createAndPersistDemands(AcademicYear createdYear, Subjects subjects, InternshipTypes types) {
        InternshipDemand d1 = createDemand(createdYear, types.sfp(), subjects.english(), 1, School.SchoolType.PRIMARY);
        InternshipDemand d2 = createDemand(createdYear, types.zsp(), subjects.social(), 1, School.SchoolType.MIDDLE);
        InternshipDemand d3 = createDemand(createdYear, types.pdp1(), subjects.english(), 1, School.SchoolType.MIDDLE);
        InternshipDemand d4 = createDemand(createdYear, types.pdp2(), subjects.english(), 1, School.SchoolType.MIDDLE);

        deps.internshipDemandRepository().saveAll(List.of(d1, d2, d3, d4));
    }

    private InternshipDemand createDemand(
            AcademicYear createdYear,
            InternshipType type,
            Subject subject,
            int requiredTeachers,
            School.SchoolType schoolType
    ) {
        InternshipDemand demand = new InternshipDemand();
        demand.setAcademicYear(createdYear);
        demand.setInternshipType(type);
        demand.setSubject(subject);
        demand.setRequiredTeachers(requiredTeachers);
        demand.setSchoolType(schoolType);
        return demand;
    }

    private record Schools(School school1, School school2) { }

    private record Subjects(SubjectCategory category, Subject english, Subject social) { }

    private record InternshipTypes(InternshipType sfp, InternshipType zsp, InternshipType pdp1, InternshipType pdp2) { }

    private record Teachers(Teacher t1, Teacher t2, Teacher t3) { }

    private record TestSetupData(
            AcademicYear year,
            Schools schools,
            Subjects subjects,
            InternshipTypes internshipTypes,
            Teachers teachers
    ) { }
}
