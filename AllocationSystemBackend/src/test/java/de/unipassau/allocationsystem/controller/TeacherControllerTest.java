package de.unipassau.allocationsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.allocationsystem.dto.teacher.TeacherCreateDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherUpdateDto;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.entity.School.SchoolType;
import de.unipassau.allocationsystem.entity.Teacher;
import de.unipassau.allocationsystem.entity.Teacher.EmploymentStatus;
import de.unipassau.allocationsystem.entity.Teacher.UsageCycle;
import de.unipassau.allocationsystem.repository.SchoolRepository;
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

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for TeacherController (updated to match controller/service).
 */
@SpringBootTest(properties = "spring.sql.init.mode=never")
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("test")
@Transactional
class TeacherControllerTest {

    private static final String BASE_URL = "/api/teachers";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final TeacherRepository teacherRepository;
    private final SchoolRepository schoolRepository;

    private School testSchool;
    private Teacher testTeacher;

    @Autowired
    TeacherControllerTest(
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            TeacherRepository teacherRepository,
            SchoolRepository schoolRepository
    ) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.teacherRepository = teacherRepository;
        this.schoolRepository = schoolRepository;
    }

    @BeforeEach
    void setUp() {
        teacherRepository.deleteAll();
        schoolRepository.deleteAll();

        testSchool = schoolRepository.save(buildSchool());
        testTeacher = teacherRepository.save(buildTeacher(testSchool));
    }

    private School buildSchool() {
        School school = new School();
        school.setSchoolName("Test School");
        school.setSchoolType(SchoolType.PRIMARY);
        school.setZoneNumber(1);
        school.setIsActive(true);
        return school;
    }

    private Teacher buildTeacher(School school) {
        Teacher t = new Teacher();
        t.setSchool(school);
        t.setFirstName("John");
        t.setLastName("Doe");
        t.setEmail("john.doe@school.de");
        t.setPhone("+49841123456");
        t.setIsPartTime(false);
        t.setEmploymentStatus(EmploymentStatus.ACTIVE);
        t.setUsageCycle(UsageCycle.FLEXIBLE);
        return t;
    }

    // ==================== GET /api/teachers (non-paginated) ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllTeachersSuccess() throws Exception {
        performGet(BASE_URL)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Teachers retrieved successfully"))
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].firstName").exists())
                .andExpect(jsonPath("$.data[0].lastName").exists())
                .andExpect(jsonPath("$.data[0].email").exists());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllTeachersWithUserRoleShouldSucceed() throws Exception {
        performGet(BASE_URL)
                .andExpect(status().isOk());
    }

    @Test
    void getAllTeachersWithoutAuthShouldFail() throws Exception {
        performGet(BASE_URL)
                .andExpect(status().isUnauthorized());
    }

    // ==================== GET /api/teachers/paginate (paginated) ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTeachersPaginateSuccess() throws Exception {
        performGet(BASE_URL + "/paginate")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTeachersPaginateWithSearchFilterSuccess() throws Exception {
        mockMvc.perform(get(BASE_URL + "/paginate")
                        .param("searchValue", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray());
    }

    // ==================== GET /api/teachers/{id} ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTeacherByIdSuccess() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", testTeacher.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Teacher retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(testTeacher.getId()))
                .andExpect(jsonPath("$.data.firstName").value("John"))
                .andExpect(jsonPath("$.data.lastName").value("Doe"))
                .andExpect(jsonPath("$.data.email").value("john.doe@school.de"))
                .andExpect(jsonPath("$.data.schoolId").value(testSchool.getId()))
                .andExpect(jsonPath("$.data.schoolName").value(testSchool.getSchoolName()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTeacherByIdNotFoundShouldFail() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTeacherByIdWithUserRoleShouldSucceed() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", testTeacher.getId()))
                .andExpect(status().isOk());
    }

    // ==================== POST /api/teachers ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTeacherSuccess() throws Exception {
        TeacherCreateDto dto = new TeacherCreateDto();
        dto.setSchoolId(testSchool.getId());
        dto.setFirstName("Jane");
        dto.setLastName("Smith");
        dto.setEmail("jane.smith@school.de");
        dto.setPhone("+49841654321");
        dto.setIsPartTime(false);
        dto.setEmploymentStatus(EmploymentStatus.ACTIVE);
        dto.setUsageCycle(UsageCycle.FLEXIBLE);

        performPost(BASE_URL, dto)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Teacher created successfully"))
                .andExpect(jsonPath("$.data.firstName").value("Jane"))
                .andExpect(jsonPath("$.data.lastName").value("Smith"))
                .andExpect(jsonPath("$.data.email").value("jane.smith@school.de"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTeacherDuplicateEmailShouldFail() throws Exception {
        TeacherCreateDto dto = new TeacherCreateDto();
        dto.setSchoolId(testSchool.getId());
        dto.setFirstName("Another");
        dto.setLastName("Teacher");
        dto.setEmail(testTeacher.getEmail());
        dto.setIsPartTime(false);
        dto.setEmploymentStatus(EmploymentStatus.ACTIVE);

        performPost(BASE_URL, dto)
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTeacherMissingRequiredFieldsShouldFail() throws Exception {
        TeacherCreateDto dto = new TeacherCreateDto();

        performPost(BASE_URL, dto)
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createTeacherWithUserRoleShouldSucceed() throws Exception {
        TeacherCreateDto dto = new TeacherCreateDto();
        dto.setSchoolId(testSchool.getId());
        dto.setFirstName("Test");
        dto.setLastName("Teacher");
        dto.setEmail("test@school.de");
        dto.setIsPartTime(false);
        dto.setEmploymentStatus(EmploymentStatus.ACTIVE);

        performPost(BASE_URL, dto)
                .andExpect(status().is2xxSuccessful());
    }

    // ==================== PUT /api/teachers/{id} ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateTeacherSuccess() throws Exception {
        TeacherUpdateDto dto = new TeacherUpdateDto();
        dto.setFirstName("Updated");
        dto.setLastName("Name");
        dto.setEmail("updated@school.de");

        performPut(BASE_URL + "/{id}", testTeacher.getId(), dto)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Teacher updated successfully"))
                .andExpect(jsonPath("$.data.firstName").value("Updated"))
                .andExpect(jsonPath("$.data.lastName").value("Name"))
                .andExpect(jsonPath("$.data.email").value("updated@school.de"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateTeacherNotFoundShouldFail() throws Exception {
        TeacherUpdateDto dto = new TeacherUpdateDto();
        dto.setFirstName("Updated");

        performPut(BASE_URL + "/{id}", 99999L, dto)
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateTeacherDuplicateEmailShouldFail() throws Exception {
        teacherRepository.save(buildTeacherWithEmail(testSchool, "another@school.de"));

        TeacherUpdateDto dto = new TeacherUpdateDto();
        dto.setEmail("another@school.de");

        performPut(BASE_URL + "/{id}", testTeacher.getId(), dto)
                .andExpect(status().isConflict());
    }

    private Teacher buildTeacherWithEmail(School school, String email) {
        Teacher t = new Teacher();
        t.setSchool(school);
        t.setFirstName("Another");
        t.setLastName("Teacher");
        t.setEmail(email);
        t.setIsPartTime(false);
        t.setEmploymentStatus(EmploymentStatus.ACTIVE);
        return t;
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateTeacherWithUserRoleShouldSucceed() throws Exception {
        TeacherUpdateDto dto = new TeacherUpdateDto();
        dto.setFirstName("Updated");

        performPut(BASE_URL + "/{id}", testTeacher.getId(), dto)
                .andExpect(status().isOk());
    }

    // ==================== DELETE /api/teachers/{id} ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteTeacherSuccess() throws Exception {
        performDelete(BASE_URL + "/{id}", testTeacher.getId())
                .andExpect(status().isNoContent());

        boolean exists = teacherRepository.findById(testTeacher.getId()).isPresent();
        assert !exists;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteTeacherNotFoundShouldFail() throws Exception {
        performDelete(BASE_URL + "/{id}", 99999L)
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteTeacherWithUserRoleShouldSucceed() throws Exception {
        performDelete(BASE_URL + "/{id}", testTeacher.getId())
                .andExpect(status().isNoContent());
    }

    private ResultActions performGet(String url) throws Exception {
        return mockMvc.perform(get(url));
    }

    private ResultActions performPost(String url, Object body) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private ResultActions performPut(String url, Object pathVar, Object body) throws Exception {
        return mockMvc.perform(put(url, pathVar)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private ResultActions performDelete(String url, Object pathVar) throws Exception {
        return mockMvc.perform(delete(url, pathVar));
    }
}
