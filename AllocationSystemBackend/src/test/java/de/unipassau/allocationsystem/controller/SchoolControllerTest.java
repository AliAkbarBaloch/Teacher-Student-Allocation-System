package de.unipassau.allocationsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.allocationsystem.dto.SchoolCreateDto;
import de.unipassau.allocationsystem.dto.SchoolStatusUpdateDto;
import de.unipassau.allocationsystem.dto.SchoolUpdateDto;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for SchoolController.
 */
@SpringBootTest
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

        // Create a test school
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
        mockMvc.perform(get("/api/schools"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Schools retrieved successfully (paginated)"))
                .andExpect(jsonPath("$.data.items", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data.items[0].schoolName").exists())
                .andExpect(jsonPath("$.data.items[0].schoolType").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllSchools_WithSearchFilter_Success() throws Exception {
        mockMvc.perform(get("/api/schools")
                .param("search", "Elementary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllSchools_WithFilters_Success() throws Exception {
        mockMvc.perform(get("/api/schools")
                .param("schoolType", "PRIMARY")
                .param("zoneNumber", "1")
                .param("isActive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllSchools_WithAllFilters_Success() throws Exception {
        mockMvc.perform(get("/api/schools")
                .param("search", "Test")
                .param("schoolType", "PRIMARY")
                .param("zoneNumber", "1")
                .param("isActive", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray());
    }

    @Test
    void getAllSchools_Unauthorized_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/schools"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllSchools_WithUserRole_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/schools"))
                .andExpect(status().isForbidden());
    }

    // ==================== GET /schools/{id} ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSchoolById_Success() throws Exception {
        mockMvc.perform(get("/api/schools/{id}", testSchool.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("School retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(testSchool.getId()))
                .andExpect(jsonPath("$.data.schoolName").value("Test Elementary School"))
                .andExpect(jsonPath("$.data.schoolType").value("PRIMARY"))
                .andExpect(jsonPath("$.data.zoneNumber").value(1))
                .andExpect(jsonPath("$.data.isActive").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSchoolById_NotFound_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/schools/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getSchoolById_WithUserRole_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/schools/{id}", testSchool.getId()))
                .andExpect(status().isForbidden());
    }

    // ==================== POST /schools ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSchool_Success() throws Exception {
        SchoolCreateDto dto = new SchoolCreateDto();
        dto.setSchoolName("New Test School");
        dto.setSchoolType(SchoolType.MIDDLE);
        dto.setZoneNumber(2);
        dto.setAddress("New Street 10");
        dto.setLatitude(new BigDecimal("48.5800000"));
        dto.setLongitude(new BigDecimal("13.4600000"));
        dto.setContactEmail("new@school.de");
        dto.setContactPhone("+49841999999");
        dto.setIsActive(true);

        mockMvc.perform(post("/api/schools")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("School created successfully"))
                .andExpect(jsonPath("$.data.schoolName").value("New Test School"))
                .andExpect(jsonPath("$.data.schoolType").value("MIDDLE"))
                .andExpect(jsonPath("$.data.zoneNumber").value(2))
                .andExpect(jsonPath("$.data.contactEmail").value("new@school.de"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSchool_DuplicateName_ShouldFail() throws Exception {
        SchoolCreateDto dto = new SchoolCreateDto();
        dto.setSchoolName("Test Elementary School"); // Duplicate name
        dto.setSchoolType(SchoolType.PRIMARY);
        dto.setZoneNumber(1);

        mockMvc.perform(post("/api/schools")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSchool_InvalidData_ShouldFail() throws Exception {
        SchoolCreateDto dto = new SchoolCreateDto();
        dto.setSchoolName("AB"); // Too short (min 3)
        dto.setSchoolType(SchoolType.PRIMARY);
        dto.setZoneNumber(-1); // Negative zone number

        mockMvc.perform(post("/api/schools")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSchool_InvalidEmail_ShouldFail() throws Exception {
        SchoolCreateDto dto = new SchoolCreateDto();
        dto.setSchoolName("Email Test School");
        dto.setSchoolType(SchoolType.PRIMARY);
        dto.setZoneNumber(1);
        dto.setContactEmail("invalid-email"); // Invalid email format

        mockMvc.perform(post("/api/schools")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createSchool_WithoutAdminRole_ShouldFail() throws Exception {
        SchoolCreateDto dto = new SchoolCreateDto();
        dto.setSchoolName("Unauthorized School");
        dto.setSchoolType(SchoolType.PRIMARY);
        dto.setZoneNumber(1);

        mockMvc.perform(post("/api/schools")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    // ==================== PUT /api/schools/{id} ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateSchool_Success() throws Exception {
        SchoolUpdateDto dto = new SchoolUpdateDto();
        dto.setSchoolName("Updated School Name");
        dto.setAddress("Updated Address");
        dto.setContactEmail("updated@school.de");

        mockMvc.perform(put("/api/schools/{id}", testSchool.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("School updated successfully"))
                .andExpect(jsonPath("$.data.schoolName").value("Updated School Name"))
                .andExpect(jsonPath("$.data.address").value("Updated Address"))
                .andExpect(jsonPath("$.data.contactEmail").value("updated@school.de"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateSchool_NotFound_ShouldFail() throws Exception {
        SchoolUpdateDto dto = new SchoolUpdateDto();
        dto.setSchoolName("Updated Name");

        mockMvc.perform(put("/api/schools/{id}", 99999L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateSchool_WithoutAdminRole_ShouldFail() throws Exception {
        SchoolUpdateDto dto = new SchoolUpdateDto();
        dto.setSchoolName("Unauthorized Update");

        mockMvc.perform(put("/api/schools/{id}", testSchool.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    // ==================== PATCH /api/schools/{id}/status ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateSchoolStatus_Deactivate_Success() throws Exception {
        SchoolStatusUpdateDto dto = new SchoolStatusUpdateDto();
        dto.setIsActive(false);

        mockMvc.perform(patch("/api/schools/{id}/status", testSchool.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("School status updated successfully"))
                .andExpect(jsonPath("$.data.isActive").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateSchoolStatus_Activate_Success() throws Exception {
        // First deactivate
        testSchool.setIsActive(false);
        schoolRepository.save(testSchool);

        SchoolStatusUpdateDto dto = new SchoolStatusUpdateDto();
        dto.setIsActive(true);

        mockMvc.perform(patch("/api/schools/{id}/status", testSchool.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("School status updated successfully"))
                .andExpect(jsonPath("$.data.isActive").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void updateSchoolStatus_WithoutAdminRole_ShouldFail() throws Exception {
        SchoolStatusUpdateDto dto = new SchoolStatusUpdateDto();
        dto.setIsActive(false);

        mockMvc.perform(patch("/api/schools/{id}/status", testSchool.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    // ==================== DELETE /api/schools/{id} ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteSchool_Success() throws Exception {
        mockMvc.perform(delete("/api/schools/{id}", testSchool.getId()))
                .andExpect(status().isNoContent());

        // Verify school is deactivated
        School deletedSchool = schoolRepository.findById(testSchool.getId()).orElseThrow();
        assert !deletedSchool.getIsActive();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteSchool_NotFound_ShouldFail() throws Exception {
        mockMvc.perform(delete("/api/schools/{id}", 99999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteSchool_WithoutAdminRole_ShouldFail() throws Exception {
        mockMvc.perform(delete("/api/schools/{id}", testSchool.getId()))
                .andExpect(status().isForbidden());
    }
}
