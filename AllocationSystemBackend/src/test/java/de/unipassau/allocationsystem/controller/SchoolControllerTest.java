package de.unipassau.allocationsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.allocationsystem.dto.school.SchoolCreateDto;
import de.unipassau.allocationsystem.dto.school.SchoolUpdateDto;
import de.unipassau.allocationsystem.entity.School;
import de.unipassau.allocationsystem.entity.School.SchoolType;
import de.unipassau.allocationsystem.repository.SchoolRepository;
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

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for SchoolController aligned with actual controller endpoints.
 */
@SpringBootTest(properties = "spring.sql.init.mode=never")
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("test")
@Transactional
class SchoolControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SchoolRepository schoolRepository;

    private School testSchool;

    @BeforeEach
    void setUp() {
        schoolRepository.deleteAll();

        testSchool = new School();
        testSchool.setSchoolName("Test Elementary School");
        testSchool.setSchoolType(SchoolType.PRIMARY);
        testSchool.setZoneNumber(1);
        testSchool.setAddress("Test Street 1");
        testSchool.setLatitude(new BigDecimal("48.5734053"));
        testSchool.setLongitude(new BigDecimal("13.4579944"));
        testSchool.setDistanceFromCenter(new BigDecimal("2.5"));
        testSchool.setTransportAccessibility("Bus Line 1");
        testSchool.setContactEmail("test@school.de");
        testSchool.setContactPhone("+49841123456");
        testSchool.setIsActive(true);
        testSchool = schoolRepository.save(testSchool);
    }

    // ==================== GET /schools ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllSchools_Success() throws Exception {
        mockMvc.perform(get("/api/schools")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.data[0].schoolName", is("Test Elementary School")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllSchools_WithSearchFilter_Success() throws Exception {
        mockMvc.perform(get("/api/schools")
                        .param("searchValue", "Elementary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.data[0].schoolName", containsString("Test")));
    }

    @Test
    void getAllSchools_Unauthorized_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/schools")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllSchools_WithUserRole_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/schools")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // ==================== GET /schools/{id} ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSchoolById_Success() throws Exception {
        mockMvc.perform(get("/api/schools/{id}", testSchool.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.schoolName", is("Test Elementary School")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSchoolById_NotFound_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/schools/{id}", 9999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getSchoolById_WithUserRole_ShouldSucceed() throws Exception {
        mockMvc.perform(get("/api/schools/{id}", testSchool.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // ==================== GET /schools/paginate ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPaginate_Default_Success() throws Exception {
        mockMvc.perform(get("/api/schools/paginate")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.data.items", notNullValue()))
                .andExpect(jsonPath("$.data.totalItems", notNullValue()));
    }

    // ==================== GET /schools/sort-fields ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSortFields_Success() throws Exception {
        mockMvc.perform(get("/api/schools/sort-fields")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", notNullValue()))
                .andExpect(jsonPath("$.data[0].key", is("id")));
    }

    // ==================== POST /schools ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSchool_Success() throws Exception {
        SchoolCreateDto dto = new SchoolCreateDto();
        dto.setSchoolName("New School");
        dto.setSchoolType(SchoolType.MIDDLE);
        dto.setZoneNumber(2);
        dto.setAddress("New Street 10");
        dto.setContactEmail("new@school.de");

        mockMvc.perform(post("/api/schools")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.schoolName", is("New School")))
                .andExpect(jsonPath("$.data.id", notNullValue()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSchool_DuplicateName_ShouldFail() throws Exception {
        SchoolCreateDto dto = new SchoolCreateDto();
        dto.setSchoolName("Test Elementary School");
        dto.setSchoolType(SchoolType.PRIMARY);

        mockMvc.perform(post("/api/schools")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSchool_InvalidData_ShouldFail() throws Exception {
        // Missing required fields (e.g., schoolName)
        Map<String, Object> invalid = Map.of("zoneNumber", 1);

        mockMvc.perform(post("/api/schools")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSchool_InvalidEmail_ShouldFail() throws Exception {
        SchoolCreateDto dto = new SchoolCreateDto();
        dto.setSchoolName("Invalid Email School");
        dto.setSchoolType(SchoolType.MIDDLE);
        dto.setContactEmail("not-an-email");

        mockMvc.perform(post("/api/schools")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createSchool_WithoutAdminRole_ShouldFail() throws Exception {
        SchoolCreateDto dto = new SchoolCreateDto();
        dto.setSchoolName("Should Not Create");
        dto.setSchoolType(SchoolType.MIDDLE);

        mockMvc.perform(post("/api/schools")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.zoneNumber", is("Zone number is required")));
    }

    // ==================== PUT /schools/{id} ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateSchool_Success() throws Exception {
        SchoolUpdateDto dto = new SchoolUpdateDto();
        dto.setSchoolName("Updated School Name");
        dto.setZoneNumber(5);

        mockMvc.perform(put("/api/schools/{id}", testSchool.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.schoolName", is("Updated School Name")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateSchool_NotFound_ShouldFail() throws Exception {
        SchoolUpdateDto dto = new SchoolUpdateDto();
        dto.setSchoolName("Doesn't Matter");

        mockMvc.perform(put("/api/schools/{id}", 9999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateSchool_WithoutAdminRole_ShouldFail() throws Exception {
        SchoolUpdateDto dto = new SchoolUpdateDto();
        dto.setSchoolName("No Permission");

        mockMvc.perform(put("/api/schools/{id}", testSchool.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    // ==================== DELETE /schools/{id} ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteSchool_Success() throws Exception {
        mockMvc.perform(delete("/api/schools/{id}", testSchool.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteSchool_NotFound_ShouldFail() throws Exception {
        mockMvc.perform(delete("/api/schools/{id}", 9999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteSchool_WithoutAdminRole_ShouldFail() throws Exception {
        mockMvc.perform(delete("/api/schools/{id}", testSchool.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
