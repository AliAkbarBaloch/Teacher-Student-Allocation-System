package de.unipassau.allocationsystem.allocation;

import de.unipassau.allocationsystem.entity.*;
import de.unipassau.allocationsystem.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Transactional
public class TeacherAllocationServiceIntegrationTest {


    @Autowired
    private TeacherAllocationService allocationService;
    @Autowired
    private AcademicYearRepository academicYearRepository;
    @Autowired
    private TeacherSubjectRepository teacherSubjectRepository;
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private InternshipDemandRepository internshipDemandRepository;
    @Autowired
    private TeacherAssignmentRepository teacherAssignmentRepository;
    @Autowired
    private InternshipTypeRepository internshipTypeRepository;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private SchoolRepository schoolRepository;
    @Autowired
    private SubjectCategoryRepository subjectCategoryRepository;

    private AcademicYear year;

    @BeforeEach
    void setup() {
        // Setup minimal dummy data for all scenarios
        year = new AcademicYear();
        year.setYearName("2025/2026-" + System.nanoTime()); // ensure unique year name
        year.setIsLocked(false);
        year.setTotalCreditHours(100); // required field
        year.setElementarySchoolHours(40); // required field
        year.setMiddleSchoolHours(60); // required field
        year.setBudgetAnnouncementDate(java.time.LocalDateTime.now()); // required field
        year.setAllocationDeadline(java.time.LocalDateTime.now().plusMonths(1)); // optional, but safe
        academicYearRepository.save(year);

        School school1 = new School();
        school1.setSchoolName("School 1");
        school1.setZoneNumber(1);
        school1.setSchoolType(School.SchoolType.PRIMARY); // Required field
        schoolRepository.save(school1);

        School school2 = new School();
        school2.setSchoolName("School 2");
        school2.setZoneNumber(2);
        school2.setSchoolType(School.SchoolType.MIDDLE); // Required field
        schoolRepository.save(school2);

        // Create and save a SubjectCategory
        SubjectCategory category = new SubjectCategory();
        category.setCategoryTitle("General Studies");
        subjectCategoryRepository.save(category);

        Subject english = new Subject();
        english.setSubjectCode("EN");
        english.setSubjectTitle("English");
        english.setSubjectCategory(category);
        english.setIsActive(true);
        subjectRepository.save(english);
        subjectRepository.flush();

        Subject social = new Subject();
        social.setSubjectCode("SO");
        social.setSubjectTitle("Social Studies");
        social.setSubjectCategory(category);
        social.setIsActive(true);
        subjectRepository.save(social);
        subjectRepository.flush();

        String uniqueSuffix = "-" + System.nanoTime();
        InternshipType sfp = new InternshipType();
        sfp.setInternshipCode("SFP" + uniqueSuffix);
        sfp.setFullName("Schulpraktikum SFP");
        sfp.setSemester(1);
        internshipTypeRepository.save(sfp);

        InternshipType zsp = new InternshipType();
        zsp.setInternshipCode("ZSP" + uniqueSuffix);
        zsp.setFullName("Zwischenpraktikum ZSP");
        zsp.setSemester(2);
        internshipTypeRepository.save(zsp);

        InternshipType pdp1 = new InternshipType();
        pdp1.setInternshipCode("PDP1" + uniqueSuffix);
        pdp1.setFullName("Pädagogisch-didaktisches Praktikum 1");
        pdp1.setSemester(3);
        internshipTypeRepository.save(pdp1);

        InternshipType pdp2 = new InternshipType();
        pdp2.setInternshipCode("PDP2" + uniqueSuffix);
        pdp2.setFullName("Pädagogisch-didaktisches Praktikum 2");
        pdp2.setSemester(4);
        internshipTypeRepository.save(pdp2);

        // Teachers for SFP
        Teacher t1 = new Teacher();
        t1.setFirstName("Alice");
        t1.setLastName("SFP");
        t1.setSchool(school1);
        t1.setEmploymentStatus(Teacher.EmploymentStatus.ACTIVE);
        t1.setEmail("alice.sfp." + System.nanoTime() + "@test.com");
        teacherRepository.save(t1);
        TeacherQualification tq1 = new TeacherQualification();
        tq1.setTeacher(t1); tq1.setSubject(english); tq1.setIsMainSubject(true);
        // Save qualification explicitly
        t1.getQualifications().add(tq1);
        // Teachers for ZSP
        Teacher t2 = new Teacher();
        t2.setFirstName("Bob");
        t2.setLastName("ZSP");
        t2.setSchool(school2);
        t2.setEmploymentStatus(Teacher.EmploymentStatus.ACTIVE);
        t2.setEmail("bob.zsp." + System.nanoTime() + "@test.com");
        teacherRepository.save(t2);
        TeacherQualification tq2 = new TeacherQualification();
        tq2.setTeacher(t2); tq2.setSubject(social); tq2.setIsMainSubject(true);
        t2.getQualifications().add(tq2);
        // Teachers for PDP
        Teacher t3 = new Teacher();
        t3.setFirstName("Carol");
        t3.setLastName("PDP");
        t3.setSchool(school2);
        t3.setEmploymentStatus(Teacher.EmploymentStatus.ACTIVE);
        t3.setEmail("carol.pdp." + System.nanoTime() + "@test.com");
        teacherRepository.save(t3);
        TeacherQualification tq3 = new TeacherQualification();
        tq3.setTeacher(t3); tq3.setSubject(english); tq3.setIsMainSubject(true);
        t3.getQualifications().add(tq3);

        // Save all TeacherQualifications explicitly (if not cascaded)
        // (No direct repository available, handled by teacherRepository if cascaded)

        // Add and save availabilities for each teacher and internship type
        TeacherAvailability ta1 = new TeacherAvailability();
        ta1.setTeacher(t1);
        ta1.setAcademicYear(year);
        ta1.setInternshipType(sfp);
        ta1.setStatus(TeacherAvailability.AvailabilityStatus.AVAILABLE);
        ta1.setIsAvailable(true);
        t1.getAvailabilities().add(ta1);

        TeacherAvailability ta2 = new TeacherAvailability();
        ta2.setTeacher(t2);
        ta2.setAcademicYear(year);
        ta2.setInternshipType(zsp);
        ta2.setStatus(TeacherAvailability.AvailabilityStatus.AVAILABLE);
        ta2.setIsAvailable(true);
        t2.getAvailabilities().add(ta2);

        TeacherAvailability ta3 = new TeacherAvailability();
        ta3.setTeacher(t3);
        ta3.setAcademicYear(year);
        ta3.setInternshipType(pdp1);
        ta3.setStatus(TeacherAvailability.AvailabilityStatus.AVAILABLE);
        ta3.setIsAvailable(true);
        t3.getAvailabilities().add(ta3);

        TeacherAvailability ta4 = new TeacherAvailability();
        ta4.setTeacher(t3);
        ta4.setAcademicYear(year);
        ta4.setInternshipType(pdp2);
        ta4.setStatus(TeacherAvailability.AvailabilityStatus.AVAILABLE);
        ta4.setIsAvailable(true);
        t3.getAvailabilities().add(ta4);

        // Save all teachers again to persist relationships
        teacherRepository.saveAll(List.of(t1, t2, t3));
        teacherRepository.flush();
        // (No direct TeacherAvailabilityRepository available)

        // Create and persist TeacherSubject entities for each teacher/subject/year
        TeacherSubject ts1 = new TeacherSubject();
        ts1.setAcademicYear(year);
        ts1.setTeacher(t1);
        ts1.setSubject(english);
        ts1.setAvailabilityStatus("AVAILABLE");
        TeacherSubject ts2 = new TeacherSubject();
        ts2.setAcademicYear(year);
        ts2.setTeacher(t2);
        ts2.setSubject(social);
        ts2.setAvailabilityStatus("AVAILABLE");
        TeacherSubject ts3 = new TeacherSubject();
        ts3.setAcademicYear(year);
        ts3.setTeacher(t3);
        ts3.setSubject(english);
        ts3.setAvailabilityStatus("AVAILABLE");
        teacherSubjectRepository.save(ts1);
        teacherSubjectRepository.save(ts2);
        teacherSubjectRepository.save(ts3);
        teacherSubjectRepository.flush();

        // Demands
        InternshipDemand d1 = new InternshipDemand();
        d1.setAcademicYear(year);
        d1.setInternshipType(sfp);
        d1.setSubject(english);
        d1.setRequiredTeachers(1);
        d1.setSchoolType(School.SchoolType.PRIMARY);
        internshipDemandRepository.save(d1);
        InternshipDemand d2 = new InternshipDemand();
        d2.setAcademicYear(year);
        d2.setInternshipType(zsp);
        d2.setSubject(social);
        d2.setRequiredTeachers(1);
        d2.setSchoolType(School.SchoolType.MIDDLE);
        internshipDemandRepository.save(d2);
        InternshipDemand d3 = new InternshipDemand();
        d3.setAcademicYear(year);
        d3.setInternshipType(pdp1);
        d3.setSubject(english);
        d3.setRequiredTeachers(1);
        d3.setSchoolType(School.SchoolType.MIDDLE);
        internshipDemandRepository.save(d3);
        InternshipDemand d4 = new InternshipDemand();
        d4.setAcademicYear(year);
        d4.setInternshipType(pdp2);
        d4.setSubject(english);
        d4.setRequiredTeachers(1);
        d4.setSchoolType(School.SchoolType.MIDDLE);
        internshipDemandRepository.save(d4);
    }

    @Test
    void testAllocateTeachersToSFP() {
        allocationService.performAllocation(year.getId());
        List<TeacherAssignment> assignments = teacherAssignmentRepository.findAll();
        assertThat(assignments).anyMatch(a -> a.getInternshipType().getInternshipCode().startsWith("SFP"));
    }

    @Test
    void testAllocateTeachersToZSP() {
        allocationService.performAllocation(year.getId());
        List<TeacherAssignment> assignments = teacherAssignmentRepository.findAll();
        assertThat(assignments).anyMatch(a -> a.getInternshipType().getInternshipCode().startsWith("ZSP"));
    }

    @Test
    void testAllocateTeachersToPDP1AndPDP2() {
        allocationService.performAllocation(year.getId());
        List<TeacherAssignment> assignments = teacherAssignmentRepository.findAll();
        assertThat(assignments).anyMatch(a -> a.getInternshipType().getInternshipCode().startsWith("PDP1"));
        assertThat(assignments).anyMatch(a -> a.getInternshipType().getInternshipCode().startsWith("PDP2"));
    }

    @Test
    void testInternshipCombinationRules() {
        // This test should be extended to set up combination rules and check enforcement
        allocationService.performAllocation(year.getId());
        List<TeacherAssignment> assignments = teacherAssignmentRepository.findAll();
        // Example: check that no teacher is assigned to an invalid combination (customize as needed)
        // assertThat(...)
    }
}
