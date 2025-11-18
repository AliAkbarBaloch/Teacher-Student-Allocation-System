package de.unipassau.allocationsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.allocationsystem.dto.teacher.TeacherCreateDto;
import de.unipassau.allocationsystem.dto.teacher.TeacherStatusUpdateDto;
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
 * Integration tests for TeacherController.
 */
@SpringBootTest
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
        testTeacher.setEmploymentStatus(EmploymentStatus.FULL_TIME);
        testTeacher.setUsageCycle(UsageCycle.FULL_YEAR);
        testTeacher.setIsActive(true);
        testTeacher = teacherRepository.save(testTeacher);
    }

    // ==================== GET /api/teachers ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllTeachers_Success() throws Exception {
        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Teachers retrieved successfully (paginated)"))
                .andExpect(jsonPath("$.data.items", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.items[0].firstName").exists())
                .andExpect(jsonPath("$.data.items[0].lastName").exists())
                .andExpect(jsonPath("$.data.items[0].email").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllTeachers_WithSearchFilter_Success() throws Exception {
        mockMvc.perform(get("/api/teachers")
                .param("search", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllTeachers_WithSchoolFilter_Success() throws Exception {
        mockMvc.perform(get("/api/teachers")
                .param("schoolId", testSchool.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllTeachers_WithEmploymentStatusFilter_Success() throws Exception {
        mockMvc.perform(get("/api/teachers")
                .param("employmentStatus", "FULL_TIME"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllTeachers_WithAllFilters_Success() throws Exception {
        mockMvc.perform(get("/api/teachers")
                .param("schoolId", testSchool.getId().toString())
                .param("employmentStatus", "FULL_TIME")
                .param("isActive", "true")
                .param("search", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllTeachers_WithUserRole_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllTeachers_WithoutAuth_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/teachers"))
                .andExpect(status().isUnauthorized());
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
                .andExpect(status().isForbidden());
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
        dto.setEmploymentStatus(EmploymentStatus.FULL_TIME);
        dto.setUsageCycle(UsageCycle.FULL_YEAR);

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
        dto.setEmploymentStatus(EmploymentStatus.FULL_TIME);

        mockMvc.perform(post("/api/teachers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTeacher_InactiveSchool_ShouldFail() throws Exception {
        testSchool.setIsActive(false);
        schoolRepository.save(testSchool);

        TeacherCreateDto dto = new TeacherCreateDto();
        dto.setSchoolId(testSchool.getId());
        dto.setFirstName("Test");
        dto.setLastName("Teacher");
        dto.setEmail("test@school.de");
        dto.setIsPartTime(false);
        dto.setEmploymentStatus(EmploymentStatus.FULL_TIME);

        mockMvc.perform(post("/api/teachers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createTeacher_InvalidPartTimeConsistency_ShouldFail() throws Exception {
        TeacherCreateDto dto = new TeacherCreateDto();
        dto.setSchoolId(testSchool.getId());
        dto.setFirstName("Test");
        dto.setLastName("Teacher");
        dto.setEmail("test@school.de");
        dto.setIsPartTime(true); // Part-time flag true
        dto.setEmploymentStatus(EmploymentStatus.FULL_TIME); // But employment status is FULL_TIME

        mockMvc.perform(post("/api/teachers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
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
        dto.setEmploymentStatus(EmploymentStatus.FULL_TIME);

        mockMvc.perform(post("/api/teachers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
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
        anotherTeacher.setEmploymentStatus(EmploymentStatus.FULL_TIME);
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
                .andExpect(status().isForbidden());
    }

    // ==================== PATCH /api/teachers/{id}/status ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateTeacherStatus_Deactivate_Success() throws Exception {
        TeacherStatusUpdateDto dto = new TeacherStatusUpdateDto();
        dto.setIsActive(false);

        mockMvc.perform(patch("/api/teachers/{id}/status", testTeacher.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Teacher status updated successfully"))
                .andExpect(jsonPath("$.data.isActive").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateTeacherStatus_Activate_Success() throws Exception {
        // First deactivate
        testTeacher.setIsActive(false);
        teacherRepository.save(testTeacher);

        TeacherStatusUpdateDto dto = new TeacherStatusUpdateDto();
        dto.setIsActive(true);

        mockMvc.perform(patch("/api/teachers/{id}/status", testTeacher.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Teacher status updated successfully"))
                .andExpect(jsonPath("$.data.isActive").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateTeacherStatus_WithUserRole_ShouldFail() throws Exception {
        TeacherStatusUpdateDto dto = new TeacherStatusUpdateDto();
        dto.setIsActive(false);

        mockMvc.perform(patch("/api/teachers/{id}/status", testTeacher.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    // ==================== DELETE /api/teachers/{id} ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteTeacher_Success() throws Exception {
        mockMvc.perform(delete("/api/teachers/{id}", testTeacher.getId()))
                .andExpect(status().isNoContent());

        // Verify teacher is deactivated
        Teacher deleted = teacherRepository.findById(testTeacher.getId()).orElseThrow();
        assert !deleted.getIsActive();
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
                .andExpect(status().isForbidden());
    }
}
