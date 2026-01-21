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
import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.AllocationPlanRepository;
import de.unipassau.allocationsystem.repository.InternshipTypeRepository;
import de.unipassau.allocationsystem.repository.SchoolRepository;
import de.unipassau.allocationsystem.repository.SubjectCategoryRepository;
import de.unipassau.allocationsystem.repository.SubjectRepository;
import de.unipassau.allocationsystem.repository.TeacherAssignmentRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import de.unipassau.allocationsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AllocationPlanRepository allocationPlanRepository;

    @Autowired
    private AcademicYearRepository academicYearRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private InternshipTypeRepository internshipTypeRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private SubjectCategoryRepository subjectCategoryRepository;

    @Autowired
    private TeacherAssignmentRepository teacherAssignmentRepository;

    private AllocationPlan plan;
    private Teacher teacher;
    private InternshipType internshipType;
    private Subject subject;

    @BeforeEach
    void setUp() {
        teacherAssignmentRepository.deleteAll();
        allocationPlanRepository.deleteAll();
        academicYearRepository.deleteAll();
        teacherRepository.deleteAll();
        internshipTypeRepository.deleteAll();
        subjectRepository.deleteAll();
        schoolRepository.deleteAll();
        subjectCategoryRepository.deleteAll();
        userRepository.deleteAll();

        AcademicYear year = new AcademicYear();
        year.setYearName("2025");
        year.setTotalCreditHours(100);
        year.setElementarySchoolHours(40);
        year.setMiddleSchoolHours(60);
        year.setBudgetAnnouncementDate(java.time.LocalDateTime.now());
        year = academicYearRepository.save(year);

        // creator user
        User creator = new User();
        creator.setEmail("creator@example.com");
        creator.setPassword("password");
        creator.setFullName("Creator");
        creator = userRepository.save(creator);

        plan = new AllocationPlan();
        plan.setAcademicYear(year);
        plan.setPlanName("Test Plan");
        plan.setPlanVersion("v1");
        plan.setStatus(AllocationPlan.PlanStatus.DRAFT);
        plan = allocationPlanRepository.save(plan);

        // create and persist a school for the teacher
        School school = new School();
        school.setSchoolName("Test School");
        school.setZoneNumber(1);
        school.setSchoolType(School.SchoolType.PRIMARY);
        school.setIsActive(true);
        school = schoolRepository.save(school);

        teacher = new Teacher();
        teacher.setFirstName("John");
        teacher.setLastName("Doe");
        teacher.setEmail("john.doe@example.com");
        teacher.setEmploymentStatus(Teacher.EmploymentStatus.ACTIVE);
        teacher.setSchool(school);
        teacher = teacherRepository.save(teacher);

        internshipType = new InternshipType();
        internshipType.setInternshipCode("PRACT");
        internshipType.setFullName("Practical");
        internshipType.setSemester(1); // added to satisfy @NotNull validation
        internshipType = internshipTypeRepository.save(internshipType);

        SubjectCategory cat = new SubjectCategory();
        cat.setCategoryTitle("Default");
        cat = subjectCategoryRepository.save(cat);

        subject = new Subject();
        subject.setSubjectCode("S1");
        subject.setSubjectTitle("Subject 1");
        subject.setSubjectCategory(cat);
        subject.setIsActive(true);
        subject = subjectRepository.save(subject);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAndGetAndDeleteAssignment_Success() throws Exception {
        TeacherAssignmentCreateDto dto = new TeacherAssignmentCreateDto();
        dto.setPlanId(plan.getId()); // new controller expects planId in DTO
        dto.setTeacherId(teacher.getId());
        dto.setInternshipTypeId(internshipType.getId());
        dto.setSubjectId(subject.getId());
        dto.setStudentGroupSize(2);
        dto.setAssignmentStatus("PLANNED");

        var createResult = mockMvc.perform(post("/api/teacher-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn();

        String content = createResult.getResponse().getContentAsString();
        Long createdId = objectMapper.readTree(content).at("/data/id").asLong();

        mockMvc.perform(get("/api/teacher-assignments/{id}", createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(createdId));

        TeacherAssignmentUpdateDto update = new TeacherAssignmentUpdateDto();
        update.setStudentGroupSize(5);

        mockMvc.perform(put("/api/teacher-assignments/{id}", createdId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.studentGroupSize").value(5));

        mockMvc.perform(delete("/api/teacher-assignments/{id}", createdId))
                .andExpect(status().isNoContent());

        boolean exists = teacherAssignmentRepository.existsById(createdId);
        assertFalse(exists);
    }

    @Test
    void unauthorizedAccess_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/teacher-assignments"))
                .andExpect(status().isUnauthorized());
    }
}
