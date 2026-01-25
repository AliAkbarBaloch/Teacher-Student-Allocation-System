package de.unipassau.allocationsystem.allocation;

import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.InternshipDemand;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.entity.SubjectCategory;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.TeacherAvailability;
import de.unipassau.allocationsystem.entity.TeacherQualification;
import de.unipassau.allocationsystem.entity.TeacherSubject;
import de.unipassau.allocationsystem.repository.InternshipDemandRepository;
import de.unipassau.allocationsystem.repository.TeacherSubjectRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Factory for building and persisting deterministic test data used by teacher allocation
 * integration tests.
 * <p>
 * The methods in this class create a minimal set of entities required for allocation:
 * academic year, schools, subjects and categories, internship types, teachers with
 * qualifications and availabilities, teacher-subject mappings, and internship demands.
 * </p>
 */
final class TeacherAllocationTestDataFactory {

    private TeacherAllocationTestDataFactory() {
    }

    /**
     * Creates a valid {@link AcademicYear} instance with required fields populated.
     *
     * @return a new academic year entity
     */
    static AcademicYear createAcademicYear() {
        AcademicYear createdYear = new AcademicYear();
        createdYear.setYearName("2025/2026-" + System.nanoTime());
        createdYear.setIsLocked(false);
        createdYear.setTotalCreditHours(100);
        createdYear.setElementarySchoolHours(40);
        createdYear.setMiddleSchoolHours(60);
        createdYear.setBudgetAnnouncementDate(LocalDateTime.now());
        createdYear.setAllocationDeadline(LocalDateTime.now().plusMonths(1));
        return createdYear;
    }

    /**
     * Creates two schools required for the test scenarios.
     *
     * @return container holding both schools
     */
    static Schools createSchools() {
        School school1 = new School();
        school1.setSchoolName("School 1");
        school1.setZoneNumber(1);
        school1.setSchoolType(School.SchoolType.PRIMARY);

        School school2 = new School();
        school2.setSchoolName("School 2");
        school2.setZoneNumber(2);
        school2.setSchoolType(School.SchoolType.MIDDLE);

        return new Schools(school1, school2);
    }

    /**
     * Creates a subject category and two active subjects (English and Social Studies).
     *
     * @return container holding the category and subjects
     */
    static Subjects createSubjects() {
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

    /**
     * Creates internship types for SFP, ZSP, PDP1 and PDP2, using a unique suffix
     * to avoid code collisions in database constraints.
     *
     * @return container holding all internship types
     */
    static InternshipTypes createInternshipTypes() {
        String uniqueSuffix = "-" + System.nanoTime();

        InternshipType sfp = createInternshipType("SFP", uniqueSuffix, "Schulpraktikum SFP", 1);
        InternshipType zsp = createInternshipType("ZSP", uniqueSuffix, "Zwischenpraktikum ZSP", 2);
        InternshipType pdp1 = createInternshipType("PDP1", uniqueSuffix, "Pädagogisch-didaktisches Praktikum 1", 3);
        InternshipType pdp2 = createInternshipType("PDP2", uniqueSuffix, "Pädagogisch-didaktisches Praktikum 2", 4);

        return new InternshipTypes(sfp, zsp, pdp1, pdp2);
    }

    /**
     * Creates a single {@link InternshipType}.
     *
     * @param codePrefix internship code prefix (e.g., SFP, ZSP)
     * @param suffix unique suffix appended to the internship code
     * @param fullName display name
     * @param semester semester number
     * @return internship type entity
     */
    static InternshipType createInternshipType(String codePrefix, String suffix, String fullName, int semester) {
        InternshipType type = new InternshipType();
        type.setInternshipCode(codePrefix + suffix);
        type.setFullName(fullName);
        type.setSemester(semester);
        return type;
    }

    /**
     * Creates teachers and attaches subject qualifications.
     *
     * @param schools schools used to assign teachers
     * @param subjects subjects used for qualifications
     * @return container holding all teachers
     */
    static Teachers createTeachersWithQualifications(Schools schools, Subjects subjects) {
        Teacher t1 = createTeacher("Alice", "SFP", schools.school1(), "alice.sfp");
        addQualification(t1, subjects.english(), true);

        Teacher t2 = createTeacher("Bob", "ZSP", schools.school2(), "bob.zsp");
        addQualification(t2, subjects.social(), true);

        Teacher t3 = createTeacher("Carol", "PDP", schools.school2(), "carol.pdp");
        addQualification(t3, subjects.english(), true);

        return new Teachers(t1, t2, t3);
    }

    /**
     * Creates an active teacher with a unique email address.
     *
     * @param firstName teacher first name
     * @param lastName teacher last name
     * @param school assigned school
     * @param emailPrefix prefix for unique email generation
     * @return teacher entity
     */
    static Teacher createTeacher(String firstName, String lastName, School school, String emailPrefix) {
        Teacher teacher = new Teacher();
        teacher.setFirstName(firstName);
        teacher.setLastName(lastName);
        teacher.setSchool(school);
        teacher.setEmploymentStatus(Teacher.EmploymentStatus.ACTIVE);
        teacher.setEmail(emailPrefix + "." + System.nanoTime() + "@test.com");
        return teacher;
    }

    /**
     * Adds a subject qualification to a teacher.
     *
     * @param teacher teacher to update
     * @param subject subject to add
     * @param isMainSubject whether this subject is the teacher's main subject
     */
    static void addQualification(Teacher teacher, Subject subject, boolean isMainSubject) {
        TeacherQualification qualification = new TeacherQualification();
        qualification.setTeacher(teacher);
        qualification.setSubject(subject);
        qualification.setIsMainSubject(isMainSubject);
        teacher.getQualifications().add(qualification);
    }

    /**
     * Attaches internship availabilities to the created teachers.
     *
     * @param createdYear academic year
     * @param internshipTypes available internship types
     * @param teachers created teachers
     */
    static void attachAvailabilities(AcademicYear createdYear, InternshipTypes internshipTypes, Teachers teachers) {
        teachers.t1().getAvailabilities().add(createAvailability(createdYear, internshipTypes.sfp(), teachers.t1()));
        teachers.t2().getAvailabilities().add(createAvailability(createdYear, internshipTypes.zsp(), teachers.t2()));

        teachers.t3().getAvailabilities().add(createAvailability(createdYear, internshipTypes.pdp1(), teachers.t3()));
        teachers.t3().getAvailabilities().add(createAvailability(createdYear, internshipTypes.pdp2(), teachers.t3()));
    }

    /**
     * Creates a single availability record for a teacher and internship type.
     *
     * @param createdYear academic year
     * @param type internship type
     * @param teacher teacher
     * @return availability entity
     */
    static TeacherAvailability createAvailability(AcademicYear createdYear, InternshipType type, Teacher teacher) {
        TeacherAvailability availability = new TeacherAvailability();
        availability.setTeacher(teacher);
        availability.setAcademicYear(createdYear);
        availability.setInternshipType(type);
        availability.setStatus(TeacherAvailability.AvailabilityStatus.AVAILABLE);
        availability.setIsAvailable(true);
        return availability;
    }

    /**
     * Creates and persists teacher-subject mappings for the given dataset.
     *
     * @param teacherSubjectRepository repository used for persistence
     * @param createdYear academic year
     * @param subjects subjects
     * @param teachers teachers
     */
    static void createAndPersistTeacherSubjects(
            TeacherSubjectRepository teacherSubjectRepository,
            AcademicYear createdYear,
            Subjects subjects,
            Teachers teachers
    ) {
        TeacherSubject ts1 = createTeacherSubject(createdYear, teachers.t1(), subjects.english());
        TeacherSubject ts2 = createTeacherSubject(createdYear, teachers.t2(), subjects.social());
        TeacherSubject ts3 = createTeacherSubject(createdYear, teachers.t3(), subjects.english());

        teacherSubjectRepository.saveAll(List.of(ts1, ts2, ts3));
        teacherSubjectRepository.flush();
    }

    /**
     * Creates a single teacher-subject mapping.
     *
     * @param createdYear academic year
     * @param teacher teacher
     * @param subject subject
     * @return teacher subject mapping
     */
    static TeacherSubject createTeacherSubject(AcademicYear createdYear, Teacher teacher, Subject subject) {
        TeacherSubject teacherSubject = new TeacherSubject();
        teacherSubject.setAcademicYear(createdYear);
        teacherSubject.setTeacher(teacher);
        teacherSubject.setSubject(subject);
        teacherSubject.setAvailabilityStatus("AVAILABLE");
        return teacherSubject;
    }

    /**
     * Creates and persists internship demands for the given dataset.
     *
     * @param internshipDemandRepository repository used for persistence
     * @param createdYear academic year
     * @param subjects subjects
     * @param types internship types
     */
    static void createAndPersistDemands(
            InternshipDemandRepository internshipDemandRepository,
            AcademicYear createdYear,
            Subjects subjects,
            InternshipTypes types
    ) {
        InternshipDemand d1 = createDemand(createdYear, types.sfp(), subjects.english(), 1, School.SchoolType.PRIMARY);
        InternshipDemand d2 = createDemand(createdYear, types.zsp(), subjects.social(), 1, School.SchoolType.MIDDLE);
        InternshipDemand d3 = createDemand(createdYear, types.pdp1(), subjects.english(), 1, School.SchoolType.MIDDLE);
        InternshipDemand d4 = createDemand(createdYear, types.pdp2(), subjects.english(), 1, School.SchoolType.MIDDLE);

        internshipDemandRepository.saveAll(List.of(d1, d2, d3, d4));
    }

    /**
     * Creates a single {@link InternshipDemand}.
     *
     * @param createdYear academic year
     * @param type internship type
     * @param subject subject
     * @param requiredTeachers number of required teachers
     * @param schoolType target school type
     * @return internship demand entity
     */
    static InternshipDemand createDemand(
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

    /**
     * Container for the schools created for allocation tests.
     *
     * @param school1 first school
     * @param school2 second school
     */
    record Schools(School school1, School school2) { }

    /**
     * Container for subjects and their shared category.
     *
     * @param category subject category
     * @param english English subject
     * @param social Social Studies subject
     */
    record Subjects(SubjectCategory category, Subject english, Subject social) { }

    /**
     * Container for internship types used in allocation tests.
     *
     * @param sfp SFP internship type
     * @param zsp ZSP internship type
     * @param pdp1 PDP1 internship type
     * @param pdp2 PDP2 internship type
     */
    record InternshipTypes(InternshipType sfp, InternshipType zsp, InternshipType pdp1, InternshipType pdp2) { }

    /**
     * Container for teachers used in allocation tests.
     *
     * @param t1 first teacher
     * @param t2 second teacher
     * @param t3 third teacher
     */
    record Teachers(Teacher t1, Teacher t2, Teacher t3) { }

    /**
     * Container grouping all entities required to persist the allocation test dataset.
     *
     * @param year academic year
     * @param schools schools
     * @param subjects subjects
     * @param internshipTypes internship types
     * @param teachers teachers
     */
    record TestSetupData(
            AcademicYear year,
            Schools schools,
            Subjects subjects,
            InternshipTypes internshipTypes,
            Teachers teachers
    ) { }
}
