package de.unipassau.allocationsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.Subject;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.SubjectRepository;
import de.unipassau.allocationsystem.repository.SchoolRepository;
import de.unipassau.allocationsystem.repository.SubjectCategoryRepository;
import de.unipassau.allocationsystem.repository.TeacherRepository;
import de.unipassau.allocationsystem.repository.TeacherSubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("test")
@Transactional
class TeacherSubjectControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private SchoolRepository schoolRepository;

    @Autowired
    private SubjectCategoryRepository subjectCategoryRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private AcademicYearRepository academicYearRepository;

    @Autowired
    private TeacherSubjectRepository teacherSubjectRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RequestMappingHandlerMapping handlerMapping;

    private Teacher testTeacher;
    private Subject testSubject;
    private AcademicYear testYear;

    @BeforeEach
    void setUp() {
        // MockMvc is autoconfigured with security filters via @AutoConfigureMockMvc
        teacherSubjectRepository.deleteAll();

        // Diagnostic: print registered mappings to help debug NoHandlerFoundException
        System.out.println("--- Registered Request Mappings (diagnostic) ---");
        for (Map.Entry<RequestMappingInfo, org.springframework.web.method.HandlerMethod> entry : handlerMapping.getHandlerMethods().entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }

        // create and save a valid school required by Teacher
        var school = new de.unipassau.allocationsystem.entity.School();
        school.setSchoolName("Test School");
        school.setSchoolType(de.unipassau.allocationsystem.entity.School.SchoolType.PRIMARY);
        school.setZoneNumber(1);
        school = schoolRepository.save(school);

        testTeacher = new Teacher();
        testTeacher.setEmail("ts-controller@test.example");
        testTeacher.setFirstName("Test");
        testTeacher.setLastName("Subject");
        testTeacher.setEmploymentStatus(Teacher.EmploymentStatus.ACTIVE);

        testTeacher.setSchool(school);
        testTeacher = teacherRepository.save(testTeacher);

        // create subject category and subject with required fields
        var category = new de.unipassau.allocationsystem.entity.SubjectCategory();
        category.setCategoryTitle("Default");
        category = subjectCategoryRepository.save(category);

        testSubject = new Subject();
        testSubject.setSubjectCode("SUBJ-TS");
        testSubject.setSubjectTitle("Subject TS");
        testSubject.setSubjectCategory(category);
        testSubject = subjectRepository.save(testSubject);

        testYear = new AcademicYear();
        testYear.setYearName("2025/2026");
        testYear.setTotalCreditHours(100);
        testYear.setElementarySchoolHours(10);
        testYear.setMiddleSchoolHours(20);
        testYear.setBudgetAnnouncementDate(java.time.LocalDateTime.now());
        testYear = academicYearRepository.save(testYear);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAndListAsAdmin() throws Exception {
        var payload = objectMapper.createObjectNode();
        payload.put("yearId", testYear.getId());
        payload.put("teacherId", testTeacher.getId());
        payload.put("subjectId", testSubject.getId());
        payload.put("availabilityStatus", "AVAILABLE");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/teachers-subjects/" + testTeacher.getId() + "/subjects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/teachers-subjects/" + testTeacher.getId() + "/subjects")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void createAsAnonymousForbidden() throws Exception {
        var payload = objectMapper.createObjectNode();
        payload.put("yearId", testYear.getId());
        payload.put("teacherId", testTeacher.getId());
        payload.put("subjectId", testSubject.getId());
        payload.put("availabilityStatus", "AVAILABLE");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/teachers-subjects/" + testTeacher.getId() + "/subjects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isUnauthorized());
    }
}
