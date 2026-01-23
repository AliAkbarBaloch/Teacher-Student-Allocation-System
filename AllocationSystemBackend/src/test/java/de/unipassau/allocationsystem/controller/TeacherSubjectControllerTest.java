package de.unipassau.allocationsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.entity.SubjectCategory;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.SchoolRepository;
import de.unipassau.allocationsystem.repository.SubjectCategoryRepository;
import de.unipassau.allocationsystem.repository.SubjectRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import de.unipassau.allocationsystem.repository.TeacherSubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the {@link TeacherSubjectController}.
 * <p>
 * This test class validates teacher-subject association operations and filtering.
 * </p>
 */
@SpringBootTest(properties = "spring.sql.init.mode=never")
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("test")
@Transactional
class TeacherSubjectControllerTest {

    private final MockMvc mockMvc;
    private final TeacherRepository teacherRepository;
    private final SchoolRepository schoolRepository;
    private final SubjectCategoryRepository subjectCategoryRepository;
    private final SubjectRepository subjectRepository;
    private final AcademicYearRepository academicYearRepository;
    private final TeacherSubjectRepository teacherSubjectRepository;
    private final ObjectMapper objectMapper;

    private Teacher testTeacher;
    private Subject testSubject;
    private AcademicYear testYear;

        @Autowired
        TeacherSubjectControllerTest(
            MockMvc mockMvc,
            TeacherRepository teacherRepository,
            SchoolRepository schoolRepository,
            SubjectCategoryRepository subjectCategoryRepository,
            SubjectRepository subjectRepository,
            AcademicYearRepository academicYearRepository,
            TeacherSubjectRepository teacherSubjectRepository,
            ObjectMapper objectMapper
    ) {
        this.mockMvc = mockMvc;
        this.teacherRepository = teacherRepository;
        this.schoolRepository = schoolRepository;
        this.subjectCategoryRepository = subjectCategoryRepository;
        this.subjectRepository = subjectRepository;
        this.academicYearRepository = academicYearRepository;
        this.teacherSubjectRepository = teacherSubjectRepository;
        this.objectMapper = objectMapper;
    }

    @BeforeEach
    void setUp() {
        teacherSubjectRepository.deleteAll();

        School school = schoolRepository.save(buildSchool());
        testTeacher = teacherRepository.save(buildTeacher(school));

        SubjectCategory category = subjectCategoryRepository.save(buildCategory());
        testSubject = subjectRepository.save(buildSubject(category));

        testYear = academicYearRepository.save(buildYear());
    }

    private School buildSchool() {
        School school = new School();
        school.setSchoolName("Test School");
        school.setSchoolType(School.SchoolType.PRIMARY);
        school.setZoneNumber(1);
        return school;
    }

    private Teacher buildTeacher(School school) {
        Teacher teacher = new Teacher();
        teacher.setEmail("ts-controller+" + System.nanoTime() + "@test.example");
        teacher.setFirstName("Test");
        teacher.setLastName("Subject");
        teacher.setEmploymentStatus(Teacher.EmploymentStatus.ACTIVE);
        teacher.setSchool(school);
        return teacher;
    }

    private SubjectCategory buildCategory() {
        SubjectCategory category = new SubjectCategory();
        category.setCategoryTitle("Default");
        return category;
    }

    private Subject buildSubject(SubjectCategory category) {
        Subject subject = new Subject();
        subject.setSubjectCode("SUBJ-TS-" + System.nanoTime());
        subject.setSubjectTitle("Subject TS");
        subject.setSubjectCategory(category);
        return subject;
    }

    private AcademicYear buildYear() {
        AcademicYear year = new AcademicYear();
        year.setYearName("2025/2026-" + System.nanoTime());
        year.setTotalCreditHours(100);
        year.setElementarySchoolHours(10);
        year.setMiddleSchoolHours(20);
        year.setBudgetAnnouncementDate(LocalDateTime.now());
        return year;
    }

    @Test
    void createAsAnonymousForbidden() throws Exception {
        var payload = objectMapper.createObjectNode();
        payload.put("yearId", testYear.getId());
        payload.put("teacherId", testTeacher.getId());
        payload.put("subjectId", testSubject.getId());
        payload.put("availabilityStatus", "AVAILABLE");

        mockMvc.perform(post("/api/teachers-subjects/{teacherId}/subjects", testTeacher.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized());
    }
}
