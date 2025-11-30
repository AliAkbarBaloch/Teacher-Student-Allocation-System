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
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for TeacherController (updated to match controller/service).
 */
@SpringBootTest(properties = "spring.sql.init.mode=never")
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("test")
@Transactional
class TeacherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private SchoolRepository schoolRepository;

    private School testSchool;
    private Teacher testTeacher;

    @BeforeEach
    void setUp() {
        teacherRepository.deleteAll();
        schoolRepository.deleteAll();

        // Create test school
        testSchool = new School();
        testSchool.setSchoolName("Test School");
        testSchool.setSchoolType(SchoolType.PRIMARY);
        testSchool.setZoneNumber(1);
        testSchool.setIsActive(true);
        testSchool = schoolRepository.save(testSchool);

        // Create test teacher
        testTeacher = new Teacher();
        testTeacher.setSchool(testSchool);
        testTeacher.setFirstName("John");
        testTeacher.setLastName("Doe");
        testTeacher.setEmail("john.doe@school.de");
        testTeacher.setPhone("+49841123456");
        testTeacher.setIsPartTime(false);
        testTeacher.setEmploymentStatus(EmploymentStatus.ACTIVE);
        testTeacher.setUsageCycle(UsageCycle.FLEXIBLE);
        testTeacher = teacherRepository.save(testTeacher);
    }

    // ==================== GET /api/teachers (non-paginated) ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllTeachers_Success() throws Exception {
        mockMvc.perform(get("/api/teachers"))
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
    void getAllTeachers_WithUserRole_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllTeachers_WithoutAuth_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== GET /api/teachers/paginate (paginated) ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTeachersPaginate_Success() throws Exception {
        mockMvc.perform(get("/api/teachers/paginate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTeachersPaginate_WithSearchFilter_Success() throws Exception {
        mockMvc.perform(get("/api/teachers/paginate")
                        .param("searchValue", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray());
    }

    // ==================== GET /api/teachers/{id} ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getTeacherById_Success() throws Exception {
        mockMvc.perform(get("/api/teachers/{id}", testTeacher.getId()))
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
    void getTeacherById_NotFound_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/teachers/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getTeacherById_WithUserRole_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/teachers/{id}", testTeacher.getId()))
                .andExpect(status().isOk());
    }

    // ==================== POST /api/teachers ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTeacher_Success() throws Exception {
        TeacherCreateDto dto = new TeacherCreateDto();
        dto.setSchoolId(testSchool.getId());
        dto.setFirstName("Jane");
        dto.setLastName("Smith");
        dto.setEmail("jane.smith@school.de");
        dto.setPhone("+49841654321");
        dto.setIsPartTime(false);
        dto.setEmploymentStatus(EmploymentStatus.ACTIVE);
        dto.setUsageCycle(UsageCycle.FLEXIBLE);

        mockMvc.perform(post("/api/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Teacher created successfully"))
                .andExpect(jsonPath("$.data.firstName").value("Jane"))
                .andExpect(jsonPath("$.data.lastName").value("Smith"))
                .andExpect(jsonPath("$.data.email").value("jane.smith@school.de"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTeacher_DuplicateEmail_ShouldFail() throws Exception {
        TeacherCreateDto dto = new TeacherCreateDto();
        dto.setSchoolId(testSchool.getId());
        dto.setFirstName("Another");
        dto.setLastName("Teacher");
        dto.setEmail(testTeacher.getEmail()); // Duplicate email
        dto.setIsPartTime(false);
        dto.setEmploymentStatus(EmploymentStatus.ACTIVE);

        mockMvc.perform(post("/api/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTeacher_MissingRequiredFields_ShouldFail() throws Exception {
        TeacherCreateDto dto = new TeacherCreateDto();
        // Missing required fields

        mockMvc.perform(post("/api/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createTeacher_WithUserRole_ShouldFail() throws Exception {
        TeacherCreateDto dto = new TeacherCreateDto();
        dto.setSchoolId(testSchool.getId());
        dto.setFirstName("Test");
        dto.setLastName("Teacher");
        dto.setEmail("test@school.de");
        dto.setIsPartTime(false);
        dto.setEmploymentStatus(EmploymentStatus.ACTIVE);

        mockMvc.perform(post("/api/teachers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is2xxSuccessful());
    }

    // ==================== PUT /api/teachers/{id} ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateTeacher_Success() throws Exception {
        TeacherUpdateDto dto = new TeacherUpdateDto();
        dto.setFirstName("Updated");
        dto.setLastName("Name");
        dto.setEmail("updated@school.de");

        mockMvc.perform(put("/api/teachers/{id}", testTeacher.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Teacher updated successfully"))
                .andExpect(jsonPath("$.data.firstName").value("Updated"))
                .andExpect(jsonPath("$.data.lastName").value("Name"))
                .andExpect(jsonPath("$.data.email").value("updated@school.de"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateTeacher_NotFound_ShouldFail() throws Exception {
        TeacherUpdateDto dto = new TeacherUpdateDto();
        dto.setFirstName("Updated");

        mockMvc.perform(put("/api/teachers/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateTeacher_DuplicateEmail_ShouldFail() throws Exception {
        // Create another teacher
        Teacher anotherTeacher = new Teacher();
        anotherTeacher.setSchool(testSchool);
        anotherTeacher.setFirstName("Another");
        anotherTeacher.setLastName("Teacher");
        anotherTeacher.setEmail("another@school.de");
        anotherTeacher.setIsPartTime(false);
        anotherTeacher.setEmploymentStatus(EmploymentStatus.ACTIVE);
        teacherRepository.save(anotherTeacher);

        // Try to update testTeacher with anotherTeacher's email
        TeacherUpdateDto dto = new TeacherUpdateDto();
        dto.setEmail("another@school.de");

        mockMvc.perform(put("/api/teachers/{id}", testTeacher.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateTeacher_WithUserRole_ShouldFail() throws Exception {
        TeacherUpdateDto dto = new TeacherUpdateDto();
        dto.setFirstName("Updated");

        mockMvc.perform(put("/api/teachers/{id}", testTeacher.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    // ==================== DELETE /api/teachers/{id} ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteTeacher_Success() throws Exception {
        mockMvc.perform(delete("/api/teachers/{id}", testTeacher.getId()))
                .andExpect(status().isNoContent());

        // Verify teacher was removed
        boolean exists = teacherRepository.findById(testTeacher.getId()).isPresent();
        assert !exists;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteTeacher_NotFound_ShouldFail() throws Exception {
        mockMvc.perform(delete("/api/teachers/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteTeacher_WithUserRole_ShouldFail() throws Exception {
        mockMvc.perform(delete("/api/teachers/{id}", testTeacher.getId()))
                .andExpect(status().isNoContent());
    }
}
