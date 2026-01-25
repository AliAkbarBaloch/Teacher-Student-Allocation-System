package de.unipassau.allocationsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.allocationsystem.dto.teacherassignment.TeacherAssignmentCreateDto;
import de.unipassau.allocationsystem.dto.teacherassignment.TeacherAssignmentUpdateDto;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.AllocationPlan;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.entity.SubjectCategory;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.AllocationPlanRepository;
import de.unipassau.allocationsystem.repository.InternshipTypeRepository;
import de.unipassau.allocationsystem.repository.SchoolRepository;
import de.unipassau.allocationsystem.repository.SubjectCategoryRepository;
import de.unipassau.allocationsystem.repository.SubjectRepository;
import de.unipassau.allocationsystem.repository.TeacherAssignmentRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the {@link TeacherAssignmentController}.
 * <p>
 * This test class validates CRUD operations and authorization for teacher assignment endpoints.
 * </p>
 */
@SpringBootTest(properties = "spring.sql.init.mode=never")
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("test")
@Transactional
class TeacherAssignmentControllerTest {

    private static final String BASE_URL = "/api/teacher-assignments";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final AllocationPlanRepository allocationPlanRepository;
    private final AcademicYearRepository academicYearRepository;
    private final TeacherRepository teacherRepository;
    private final InternshipTypeRepository internshipTypeRepository;
    private final SubjectRepository subjectRepository;
    private final SchoolRepository schoolRepository;
    private final SubjectCategoryRepository subjectCategoryRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;

    private AllocationPlan plan;
    private Teacher teacher;
    private InternshipType internshipType;
    private Subject subject;

    @Autowired
    TeacherAssignmentControllerTest(
            @Autowired
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            AllocationPlanRepository allocationPlanRepository,
            AcademicYearRepository academicYearRepository,
            TeacherRepository teacherRepository,
            InternshipTypeRepository internshipTypeRepository,
            SubjectRepository subjectRepository,
            SchoolRepository schoolRepository,
            SubjectCategoryRepository subjectCategoryRepository,
            TeacherAssignmentRepository teacherAssignmentRepository
    ) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.allocationPlanRepository = allocationPlanRepository;
        this.academicYearRepository = academicYearRepository;
        this.teacherRepository = teacherRepository;
        this.internshipTypeRepository = internshipTypeRepository;
        this.subjectRepository = subjectRepository;
        this.schoolRepository = schoolRepository;
        this.subjectCategoryRepository = subjectCategoryRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
    }

    @BeforeEach
    void setUp() {
        clearRepositories();

        AcademicYear year = createAndPersistYear();
        plan = createAndPersistPlan(year);

        School school = createAndPersistSchool();
        teacher = createAndPersistTeacher(school);

        internshipType = createAndPersistInternshipType();

        SubjectCategory cat = createAndPersistSubjectCategory();
        subject = createAndPersistSubject(cat);
    }

    private void clearRepositories() {
        teacherAssignmentRepository.deleteAll();
        allocationPlanRepository.deleteAll();
        academicYearRepository.deleteAll();
        teacherRepository.deleteAll();
        internshipTypeRepository.deleteAll();
        subjectRepository.deleteAll();
        schoolRepository.deleteAll();
        subjectCategoryRepository.deleteAll();
    }

    private AcademicYear createAndPersistYear() {
        AcademicYear year = new AcademicYear();
        year.setYearName("2025-" + System.nanoTime());
        year.setTotalCreditHours(100);
        year.setElementarySchoolHours(40);
        year.setMiddleSchoolHours(60);
        year.setBudgetAnnouncementDate(LocalDateTime.now());
        return academicYearRepository.save(year);
    }

    private AllocationPlan createAndPersistPlan(AcademicYear year) {
        AllocationPlan p = new AllocationPlan();
        p.setAcademicYear(year);
        p.setPlanName("Test Plan");
        p.setPlanVersion("v1");
        p.setStatus(AllocationPlan.PlanStatus.DRAFT);
        return allocationPlanRepository.save(p);
    }

    private School createAndPersistSchool() {
        School school = new School();
        school.setSchoolName("Test School");
        school.setZoneNumber(1);
        school.setSchoolType(School.SchoolType.PRIMARY);
        school.setIsActive(true);
        return schoolRepository.save(school);
    }

    private Teacher createAndPersistTeacher(School school) {
        Teacher t = new Teacher();
        t.setFirstName("John");
        t.setLastName("Doe");
        t.setEmail("john.doe+" + System.nanoTime() + "@example.com");
        t.setEmploymentStatus(Teacher.EmploymentStatus.ACTIVE);
        t.setSchool(school);
        return teacherRepository.save(t);
    }

    private InternshipType createAndPersistInternshipType() {
        InternshipType it = new InternshipType();
        it.setInternshipCode("PRACT-" + System.nanoTime());
        it.setFullName("Practical");
        it.setSemester(1);
        return internshipTypeRepository.save(it);
    }

    private SubjectCategory createAndPersistSubjectCategory() {
        SubjectCategory cat = new SubjectCategory();
        cat.setCategoryTitle("Default");
        return subjectCategoryRepository.save(cat);
    }

    private Subject createAndPersistSubject(SubjectCategory cat) {
        Subject s = new Subject();
        s.setSubjectCode("S1-" + System.nanoTime());
        s.setSubjectTitle("Subject 1");
        s.setSubjectCategory(cat);
        s.setIsActive(true);
        return subjectRepository.save(s);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createGetUpdateDeleteAssignmentSuccess() throws Exception {
        long createdId = createAssignmentAndReturnId();

        assertGetByIdOk(createdId);
        assertUpdateOk(createdId);
        assertDeleteOk(createdId);

        assertFalse(teacherAssignmentRepository.existsById(createdId));
    }

    @Test
    void unauthorizedAccessShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());
    }

    private long createAssignmentAndReturnId() throws Exception {
        TeacherAssignmentCreateDto dto = new TeacherAssignmentCreateDto();
        dto.setPlanId(plan.getId());
        dto.setTeacherId(teacher.getId());
        dto.setInternshipTypeId(internshipType.getId());
        dto.setSubjectId(subject.getId());
        dto.setStudentGroupSize(2);
        dto.setAssignmentStatus("PLANNED");

        ResultActions result = mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists());

        String content = result.andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(content).at("/data/id").asLong();
    }

    private void assertGetByIdOk(long id) throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(id));
    }

    private void assertUpdateOk(long id) throws Exception {
        TeacherAssignmentUpdateDto update = new TeacherAssignmentUpdateDto();
        update.setStudentGroupSize(5);

        mockMvc.perform(put(BASE_URL + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentGroupSize").value(5));
    }

    private void assertDeleteOk(long id) throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{id}", id))
                .andExpect(status().isNoContent());
    }
}
