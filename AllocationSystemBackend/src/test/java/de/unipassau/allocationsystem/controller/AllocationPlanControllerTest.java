package de.unipassau.allocationsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.unipassau.allocationsystem.dto.allocationplan.AllocationPlanCreateDto;
import de.unipassau.allocationsystem.dto.allocationplan.AllocationPlanUpdateDto;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.AllocationPlan;
import de.unipassau.allocationsystem.entity.AllocationPlan.PlanStatus;
import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.AllocationPlanRepository;
import de.unipassau.allocationsystem.repository.TeacherAssignmentRepository;
import de.unipassau.allocationsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AllocationPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AllocationPlanRepository allocationPlanRepository;

    @Autowired
    private AcademicYearRepository academicYearRepository;

    @Autowired
    private TeacherAssignmentRepository teacherAssignmentRepository;

    @Autowired
    private UserRepository userRepository;

    private AcademicYear academicYear;
    private User user;
    private AllocationPlan testPlan;

    @BeforeEach
    void setUp() {
        // Delete in correct order (children first, then parents)
        teacherAssignmentRepository.deleteAll();
        allocationPlanRepository.deleteAll();
        
        // Try to find existing user or create new one
        user = userRepository.findByEmail("test@example.com").orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail("test@example.com");
            newUser.setPassword("password123");
            newUser.setFullName("Test User");
            newUser.setRole(User.UserRole.ADMIN);
            newUser.setEnabled(true);
            newUser.setAccountStatus(User.AccountStatus.ACTIVE);
            newUser.setAccountLocked(false);
            newUser.setFailedLoginAttempts(0);
            return userRepository.save(newUser);
        });

        // Create unique academic year for each test to avoid constraint violations
        String yearName = "Test-" + System.currentTimeMillis();
        academicYear = new AcademicYear();
        academicYear.setYearName(yearName);
        academicYear.setTotalCreditHours(100);
        academicYear.setElementarySchoolHours(20);
        academicYear.setMiddleSchoolHours(25);
        academicYear.setBudgetAnnouncementDate(LocalDateTime.now());
        academicYear.setIsLocked(false);
        academicYear = academicYearRepository.save(academicYear);

        testPlan = new AllocationPlan();
        testPlan.setAcademicYear(academicYear);
        testPlan.setPlanName("Test Plan");
        testPlan.setPlanVersion("1");
        testPlan.setStatus(PlanStatus.DRAFT);
        testPlan.setNotes("Test Description");
        testPlan.setIsCurrent(true);
        testPlan = allocationPlanRepository.save(testPlan);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllocationPlans_Success() throws Exception {
        mockMvc.perform(get("/api/allocation-plans")
                        .param("yearId", academicYear.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Allocation plans retrieved successfully")))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].planName", is("Test Plan")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllocationPlans_WithFilters() throws Exception {
        mockMvc.perform(get("/api/allocation-plans")
                        .param("yearId", academicYear.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void testGetAllocationPlans_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/allocation-plans")
                        .param("yearId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetAllocationPlans_Forbidden() throws Exception {
        mockMvc.perform(get("/api/allocation-plans")
                        .param("yearId", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllocationPlanById_Success() throws Exception {
        mockMvc.perform(get("/api/allocation-plans/" + testPlan.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Allocation plan retrieved successfully")))
                .andExpect(jsonPath("$.data.planName", is("Test Plan")))
                .andExpect(jsonPath("$.data.planVersion", is("1")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllocationPlanById_NotFound() throws Exception {
        mockMvc.perform(get("/api/allocation-plans/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateAllocationPlan_Success() throws Exception {
        AllocationPlanCreateDto createDto = new AllocationPlanCreateDto();
        createDto.setYearId(academicYear.getId());
        createDto.setPlanName("New Plan");
        createDto.setPlanVersion("2");
        createDto.setStatus(PlanStatus.DRAFT);
        createDto.setNotes("New Description");
        createDto.setIsCurrent(false);

        mockMvc.perform(post("/api/allocation-plans")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Allocation plan created successfully")))
                .andExpect(jsonPath("$.data.planName", is("New Plan")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateAllocationPlan_InvalidData() throws Exception {
        AllocationPlanCreateDto createDto = new AllocationPlanCreateDto();

        mockMvc.perform(post("/api/allocation-plans")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateAllocationPlan_Success() throws Exception {
        AllocationPlanUpdateDto updateDto = new AllocationPlanUpdateDto();
        updateDto.setPlanName("Updated Plan");
        updateDto.setNotes("Updated Description");

        mockMvc.perform(put("/api/allocation-plans/" + testPlan.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Allocation plan updated successfully")))
                .andExpect(jsonPath("$.data.planName", is("Updated Plan")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateAllocationPlan_NotFound() throws Exception {
        AllocationPlanUpdateDto updateDto = new AllocationPlanUpdateDto();
        updateDto.setPlanName("Updated Plan");

        mockMvc.perform(put("/api/allocation-plans/999")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSetCurrentPlan_Success() throws Exception {
        AllocationPlan anotherPlan = new AllocationPlan();
        anotherPlan.setAcademicYear(academicYear);
        anotherPlan.setPlanName("Another Plan");
        anotherPlan.setPlanVersion("2");
        anotherPlan.setStatus(PlanStatus.APPROVED);
        anotherPlan.setNotes("Another Description");
        anotherPlan.setIsCurrent(false);
        anotherPlan = allocationPlanRepository.save(anotherPlan);

        mockMvc.perform(post("/api/allocation-plans/" + anotherPlan.getId() + "/current")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Allocation plan set as current successfully")))
                .andExpect(jsonPath("$.data.isCurrent", is(true)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSetCurrentPlan_NotFound() throws Exception {
        mockMvc.perform(post("/api/allocation-plans/999/current")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testArchivePlan_Success() throws Exception {
        mockMvc.perform(post("/api/allocation-plans/" + testPlan.getId() + "/archive")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Allocation plan archived successfully")))
                .andExpect(jsonPath("$.data.status", is("ARCHIVED")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testArchivePlan_NotFound() throws Exception {
        mockMvc.perform(post("/api/allocation-plans/999/archive")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetCurrentPlanForYear_Success() throws Exception {
        mockMvc.perform(get("/api/allocation-plans/current")
                        .param("yearId", academicYear.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Current allocation plan retrieved successfully")))
                .andExpect(jsonPath("$.data.isCurrent", is(true)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetCurrentPlanForYear_NotFound() throws Exception {
        // Create unique academic year to avoid constraint violations
        String yearName = "Test-NotFound-" + System.currentTimeMillis();
        AcademicYear newYear = new AcademicYear();
        newYear.setYearName(yearName);
        newYear.setTotalCreditHours(100);
        newYear.setElementarySchoolHours(20);
        newYear.setMiddleSchoolHours(25);
        newYear.setBudgetAnnouncementDate(LocalDateTime.now());
        newYear.setIsLocked(false);
        newYear = academicYearRepository.save(newYear);

        mockMvc.perform(get("/api/allocation-plans/current")
                        .param("yearId", newYear.getId().toString()))
                .andExpect(status().isNotFound());
    }
}
