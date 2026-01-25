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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the {@link AllocationPlanController}.
 * <p>
 * This test class validates CRUD operations, status transitions, and authorization
 * for allocation plan endpoints.
 * </p>
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AllocationPlanControllerTest {

    private static final String TEST_USER_EMAIL = "test@example.com";
    private static final String TEST_USER_PASSWORD = "test-password"; // test-only constant (no real credential)

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final AllocationPlanRepository allocationPlanRepository;
    private final AcademicYearRepository academicYearRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final UserRepository userRepository;

    private AcademicYear academicYear;
    private AllocationPlan testPlan;

    @Autowired
    AllocationPlanControllerTest(
            @Autowired
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            AllocationPlanRepository allocationPlanRepository,
            AcademicYearRepository academicYearRepository,
            TeacherAssignmentRepository teacherAssignmentRepository,
            UserRepository userRepository
    ) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.allocationPlanRepository = allocationPlanRepository;
        this.academicYearRepository = academicYearRepository;
        this.teacherAssignmentRepository = teacherAssignmentRepository;
        this.userRepository = userRepository;
    }

    @BeforeEach
    void setUp() {
        cleanRepositories();
        ensureAdminUserExists();
        academicYear = createAndPersistAcademicYear("Test-");
        testPlan = createAndPersistPlan(academicYear, "Test Plan", "1", PlanStatus.DRAFT, true);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllocationPlansSuccess() throws Exception {
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
    void testGetAllocationPlansWithFilters() throws Exception {
        mockMvc.perform(get("/api/allocation-plans")
                        .param("yearId", academicYear.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void testGetAllocationPlansUnauthorized() throws Exception {
        mockMvc.perform(get("/api/allocation-plans")
                        .param("yearId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetAllocationPlansForbidden() throws Exception {
        mockMvc.perform(get("/api/allocation-plans")
                        .param("yearId", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllocationPlanByIdSuccess() throws Exception {
        mockMvc.perform(get("/api/allocation-plans/" + testPlan.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Allocation plan retrieved successfully")))
                .andExpect(jsonPath("$.data.planName", is("Test Plan")))
                .andExpect(jsonPath("$.data.planVersion", is("1")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllocationPlanByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/allocation-plans/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateAllocationPlanSuccess() throws Exception {
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
    void testCreateAllocationPlanInvalidData() throws Exception {
        AllocationPlanCreateDto createDto = new AllocationPlanCreateDto();

        mockMvc.perform(post("/api/allocation-plans")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateAllocationPlanSuccess() throws Exception {
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
    void testUpdateAllocationPlanNotFound() throws Exception {
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
    void testSetCurrentPlanSuccess() throws Exception {
        AllocationPlan anotherPlan = createAndPersistPlan(academicYear, "Another Plan", "2", PlanStatus.APPROVED, false);

        mockMvc.perform(post("/api/allocation-plans/" + anotherPlan.getId() + "/current")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Allocation plan set as current successfully")))
                .andExpect(jsonPath("$.data.isCurrent", is(true)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSetCurrentPlanNotFound() throws Exception {
        mockMvc.perform(post("/api/allocation-plans/999/current")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testArchivePlanSuccess() throws Exception {
        mockMvc.perform(post("/api/allocation-plans/" + testPlan.getId() + "/archive")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Allocation plan archived successfully")))
                .andExpect(jsonPath("$.data.status", is("ARCHIVED")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testArchivePlanNotFound() throws Exception {
        mockMvc.perform(post("/api/allocation-plans/999/archive")
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetCurrentPlanForYearSuccess() throws Exception {
        mockMvc.perform(get("/api/allocation-plans/current")
                        .param("yearId", academicYear.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Current allocation plan retrieved successfully")))
                .andExpect(jsonPath("$.data.isCurrent", is(true)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetCurrentPlanForYearNotFound() throws Exception {
        AcademicYear newYear = createAndPersistAcademicYear("Test-NotFound-");

        mockMvc.perform(get("/api/allocation-plans/current")
                        .param("yearId", newYear.getId().toString()))
                .andExpect(status().isNotFound());
    }

    private void cleanRepositories() {
        // Delete in correct order (children first, then parents)
        teacherAssignmentRepository.deleteAll();
        allocationPlanRepository.deleteAll();
    }

    private void ensureAdminUserExists() {
        userRepository.findByEmail(TEST_USER_EMAIL).orElseGet(this::createAdminUser);
    }

    private User createAdminUser() {
        User newUser = new User();
        newUser.setEmail(TEST_USER_EMAIL);

        // This is not a real secret: tests typically use a dummy password.
        // If you have a PasswordEncoder requirement, encode it here instead.
        newUser.setPassword(TEST_USER_PASSWORD);

        newUser.setFullName("Test User");
        newUser.setRole(User.UserRole.ADMIN);
        newUser.setEnabled(true);
        newUser.setAccountStatus(User.AccountStatus.ACTIVE);
        newUser.setAccountLocked(false);
        newUser.setFailedLoginAttempts(0);
        return userRepository.save(newUser);
    }

    private AcademicYear createAndPersistAcademicYear(String prefix) {
        String yearName = prefix + System.currentTimeMillis();

        AcademicYear year = new AcademicYear();
        year.setYearName(yearName);
        year.setTotalCreditHours(100);
        year.setElementarySchoolHours(20);
        year.setMiddleSchoolHours(25);
        year.setBudgetAnnouncementDate(LocalDateTime.now());
        year.setIsLocked(false);

        return academicYearRepository.save(year);
    }

    private AllocationPlan createAndPersistPlan(
            AcademicYear year,
            String planName,
            String planVersion,
            PlanStatus status,
            boolean isCurrent
    ) {
        AllocationPlan plan = new AllocationPlan();
        plan.setAcademicYear(year);
        plan.setPlanName(planName);
        plan.setPlanVersion(planVersion);
        plan.setStatus(status);
        plan.setNotes(planName + " Description");
        plan.setIsCurrent(isCurrent);
        return allocationPlanRepository.save(plan);
    }
}
