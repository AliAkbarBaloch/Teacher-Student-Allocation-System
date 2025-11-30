package de.unipassau.allocationsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintCreateDto;
import de.unipassau.allocationsystem.dto.zoneconstraint.ZoneConstraintUpdateDto;
import de.unipassau.allocationsystem.entity.InternshipType;
import de.unipassau.allocationsystem.entity.ZoneConstraint;
import de.unipassau.allocationsystem.repository.InternshipTypeRepository;
import de.unipassau.allocationsystem.repository.ZoneConstraintRepository;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ZoneConstraintController.
 * Tests all REST endpoints with security, validation, and error scenarios.
 */
@SpringBootTest(properties = "spring.sql.init.mode=never")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ZoneConstraintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ZoneConstraintRepository zoneConstraintRepository;

    @Autowired
    private InternshipTypeRepository internshipTypeRepository;

    private InternshipType testInternshipType;
    private ZoneConstraint testConstraint;

    @BeforeEach
    void setUp() {
        // Clean up
        zoneConstraintRepository.deleteAll();

        // Create test internship type
        testInternshipType = new InternshipType();
        testInternshipType.setInternshipCode("TEST-INT-01");
        testInternshipType.setFullName("Test Internship Type");
        testInternshipType.setIsSubjectSpecific(false);
        testInternshipType.setSemester(1); // <-- required to satisfy @NotNull validation
        testInternshipType = internshipTypeRepository.save(testInternshipType);

        // Create test constraint
        testConstraint = new ZoneConstraint();
        testConstraint.setZoneNumber(1);
        testConstraint.setInternshipType(testInternshipType);
        testConstraint.setIsAllowed(true);
        testConstraint.setDescription("Test constraint for zone 1");
        testConstraint = zoneConstraintRepository.save(testConstraint);
    }

    // ========== GET ALL CONSTRAINTS TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllZoneConstraints_Success() throws Exception {
        mockMvc.perform(get("/api/zone-constraints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Zone constraints retrieved successfully")))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].zoneNumber", is(1)))
                .andExpect(jsonPath("$.data[0].isAllowed", is(true)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllZoneConstraints_Paginated() throws Exception {
        mockMvc.perform(get("/api/zone-constraints/paginate")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("sortBy", "id")
                        .param("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].zoneNumber", is(1)))
                .andExpect(jsonPath("$.data.totalItems", is(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllZoneConstraints_WithSearch() throws Exception {
        mockMvc.perform(get("/api/zone-constraints/paginate")
                        .param("page", "1")
                        .param("pageSize", "10")
                        .param("sortBy", "zoneNumber")
                        .param("sortOrder", "asc")
                        .param("searchValue", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].zoneNumber", is(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetSortFields_Success() throws Exception {
        mockMvc.perform(get("/api/zone-constraints/sort-fields"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testGetAllZoneConstraints_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/zone-constraints"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetAllZoneConstraints_UserRole() throws Exception {
        mockMvc.perform(get("/api/zone-constraints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    // ========== GET CONSTRAINT BY ID TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetZoneConstraintById_Success() throws Exception {
        mockMvc.perform(get("/api/zone-constraints/" + testConstraint.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Zone constraint retrieved successfully")))
                .andExpect(jsonPath("$.data.id", is(testConstraint.getId().intValue())))
                .andExpect(jsonPath("$.data.zoneNumber", is(1)))
                .andExpect(jsonPath("$.data.internshipTypeCode", is("TEST-INT-01")))
                .andExpect(jsonPath("$.data.isAllowed", is(true)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetZoneConstraintById_NotFound() throws Exception {
        mockMvc.perform(get("/api/zone-constraints/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetZoneConstraintById_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/zone-constraints/" + testConstraint.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetZoneConstraintById_UserRole() throws Exception {
        mockMvc.perform(get("/api/zone-constraints/" + testConstraint.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    // ========== CREATE CONSTRAINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateZoneConstraint_Success() throws Exception {
        ZoneConstraintCreateDto createDto = new ZoneConstraintCreateDto();
        createDto.setZoneNumber(2);
        createDto.setInternshipTypeId(testInternshipType.getId());
        createDto.setIsAllowed(false);
        createDto.setDescription("Zone 2 constraint");

        mockMvc.perform(post("/api/zone-constraints")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Zone constraint created successfully")))
                .andExpect(jsonPath("$.data.zoneNumber", is(2)))
                .andExpect(jsonPath("$.data.isAllowed", is(false)))
                .andExpect(jsonPath("$.data.description", is("Zone 2 constraint")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateZoneConstraint_InvalidData_MissingZoneNumber() throws Exception {
        ZoneConstraintCreateDto createDto = new ZoneConstraintCreateDto();
        // Missing zone number
        createDto.setInternshipTypeId(testInternshipType.getId());
        createDto.setIsAllowed(true);

        mockMvc.perform(post("/api/zone-constraints")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateZoneConstraint_InvalidData_NegativeZoneNumber() throws Exception {
        ZoneConstraintCreateDto createDto = new ZoneConstraintCreateDto();
        createDto.setZoneNumber(-1); // Invalid: must be positive
        createDto.setInternshipTypeId(testInternshipType.getId());
        createDto.setIsAllowed(true);

        mockMvc.perform(post("/api/zone-constraints")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateZoneConstraint_InvalidData_MissingInternshipTypeId() throws Exception {
        ZoneConstraintCreateDto createDto = new ZoneConstraintCreateDto();
        createDto.setZoneNumber(2);
        // Missing internship type ID
        createDto.setIsAllowed(true);

        mockMvc.perform(post("/api/zone-constraints")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateZoneConstraint_InvalidData_MissingIsAllowed() throws Exception {
        ZoneConstraintCreateDto createDto = new ZoneConstraintCreateDto();
        createDto.setZoneNumber(2);
        createDto.setInternshipTypeId(testInternshipType.getId());
        // Missing isAllowed flag

        mockMvc.perform(post("/api/zone-constraints")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateZoneConstraint_DuplicateConstraint() throws Exception {
        ZoneConstraintCreateDto createDto = new ZoneConstraintCreateDto();
        createDto.setZoneNumber(1); // Same as existing
        createDto.setInternshipTypeId(testInternshipType.getId()); // Same as existing
        createDto.setIsAllowed(false);

        mockMvc.perform(post("/api/zone-constraints")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateZoneConstraint_InvalidInternshipType() throws Exception {
        ZoneConstraintCreateDto createDto = new ZoneConstraintCreateDto();
        createDto.setZoneNumber(2);
        createDto.setInternshipTypeId(99999L); // Non-existent
        createDto.setIsAllowed(true);

        mockMvc.perform(post("/api/zone-constraints")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateZoneConstraint_Unauthorized() throws Exception {
        ZoneConstraintCreateDto createDto = new ZoneConstraintCreateDto();
        createDto.setZoneNumber(2);
        createDto.setInternshipTypeId(testInternshipType.getId());
        createDto.setIsAllowed(true);

        mockMvc.perform(post("/api/zone-constraints")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testCreateZoneConstraint_UserRole() throws Exception {
        ZoneConstraintCreateDto createDto = new ZoneConstraintCreateDto();
        createDto.setZoneNumber(2);
        createDto.setInternshipTypeId(testInternshipType.getId());
        createDto.setIsAllowed(true);

        mockMvc.perform(post("/api/zone-constraints")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)));
    }

    // ========== UPDATE CONSTRAINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateZoneConstraint_Success() throws Exception {
        ZoneConstraintUpdateDto updateDto = new ZoneConstraintUpdateDto();
        updateDto.setIsAllowed(false);
        updateDto.setDescription("Updated description");

        mockMvc.perform(put("/api/zone-constraints/" + testConstraint.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Zone constraint updated successfully")))
                .andExpect(jsonPath("$.data.isAllowed", is(false)))
                .andExpect(jsonPath("$.data.description", is("Updated description")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateZoneConstraint_PartialUpdate() throws Exception {
        ZoneConstraintUpdateDto updateDto = new ZoneConstraintUpdateDto();
        updateDto.setZoneNumber(5); // Only update zone number

        mockMvc.perform(put("/api/zone-constraints/" + testConstraint.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.zoneNumber", is(5)))
                .andExpect(jsonPath("$.data.isAllowed", is(true))); // Unchanged
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateZoneConstraint_NotFound() throws Exception {
        ZoneConstraintUpdateDto updateDto = new ZoneConstraintUpdateDto();
        updateDto.setIsAllowed(false);

        mockMvc.perform(put("/api/zone-constraints/99999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateZoneConstraint_Unauthorized() throws Exception {
        ZoneConstraintUpdateDto updateDto = new ZoneConstraintUpdateDto();
        updateDto.setIsAllowed(false);

        mockMvc.perform(put("/api/zone-constraints/" + testConstraint.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testUpdateZoneConstraint_UserRole() throws Exception {
        ZoneConstraintUpdateDto updateDto = new ZoneConstraintUpdateDto();
        updateDto.setIsAllowed(false);

        mockMvc.perform(put("/api/zone-constraints/" + testConstraint.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));
    }

    // ========== DELETE CONSTRAINT TESTS ==========

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteZoneConstraint_Success() throws Exception {
        mockMvc.perform(delete("/api/zone-constraints/" + testConstraint.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteZoneConstraint_NotFound() throws Exception {
        mockMvc.perform(delete("/api/zone-constraints/99999")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteZoneConstraint_Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/zone-constraints/" + testConstraint.getId())
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testDeleteZoneConstraint_UserRole() throws Exception {
        mockMvc.perform(delete("/api/zone-constraints/" + testConstraint.getId())
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }
}
