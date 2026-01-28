package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.constant.PlanChangeTypes;
import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.AllocationPlan;
import de.unipassau.allocationsystem.entity.PlanChangeLog;
import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.AllocationPlanRepository;
import de.unipassau.allocationsystem.repository.PlanChangeLogRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the {@link PlanChangeLogController}.
 * <p>
 * This test class validates change log retrieval and filtering for allocation plans.
 * </p>
 */
@SpringBootTest(properties = "spring.sql.init.mode=never")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PlanChangeLogControllerTest {

    private static final String TEST_USER_EMAIL = "controller@test.example";

    private final MockMvc mockMvc;
    private final UserRepository userRepository;
    private final AllocationPlanRepository allocationPlanRepository;
    private final AcademicYearRepository academicYearRepository;
    private final PlanChangeLogRepository planChangeLogRepository;
    private AllocationPlan testPlan;
    private User testUser;

    @Autowired
    PlanChangeLogControllerTest(
            @Autowired
            MockMvc mockMvc,
            UserRepository userRepository,
            AllocationPlanRepository allocationPlanRepository,
            AcademicYearRepository academicYearRepository,
            PlanChangeLogRepository planChangeLogRepository
    ) {
        this.mockMvc = mockMvc;
        this.userRepository = userRepository;
        this.allocationPlanRepository = allocationPlanRepository;
        this.academicYearRepository = academicYearRepository;
        this.planChangeLogRepository = planChangeLogRepository;
    }

    @Autowired
    PlanChangeLogControllerTest(
            @Autowired
            MockMvc mockMvc,
            UserRepository userRepository,
            AllocationPlanRepository allocationPlanRepository,
            AcademicYearRepository academicYearRepository,
            PlanChangeLogRepository planChangeLogRepository
    ) {
        this.mockMvc = mockMvc;
        this.userRepository = userRepository;
        this.allocationPlanRepository = allocationPlanRepository;
        this.academicYearRepository = academicYearRepository;
        this.planChangeLogRepository = planChangeLogRepository;
    }

    @BeforeEach
    void setUp() {
        cleanRepositories();
        testUser = createAndPersistTestUser();
        AcademicYear year = createAndPersistYear();
        testPlan = createAndPersistPlan(year);
        createAndPersistChangeLog(testPlan);
    }

    private void cleanRepositories() {
        planChangeLogRepository.deleteAll();
    }

    private User createAndPersistTestUser() {
        User user = new User();
        user.setEmail(TEST_USER_EMAIL);

        // Test-only dummy value to satisfy constraints; not a real credential.
        user.setPassword("test-" + System.nanoTime());

        user.setFullName("Controller User");
        user.setEnabled(true);
        return userRepository.save(user);
    }

    private AcademicYear createAndPersistYear() {
        AcademicYear year = new AcademicYear();
        year.setYearName("2025/2026-" + System.nanoTime());
        year.setTotalCreditHours(100);
        year.setElementarySchoolHours(20);
        year.setMiddleSchoolHours(30);
        year.setBudgetAnnouncementDate(LocalDateTime.now());
        return academicYearRepository.save(year);
    }

    private AllocationPlan createAndPersistPlan(AcademicYear year) {
        AllocationPlan plan = new AllocationPlan();
        plan.setPlanName("Controller Plan");
        plan.setPlanVersion("v1");
        plan.setStatus(AllocationPlan.PlanStatus.DRAFT);
        plan.setAcademicYear(year);
        return allocationPlanRepository.save(plan);
    }

    private void createAndPersistChangeLog(AllocationPlan plan) {
        PlanChangeLog log = PlanChangeLog.builder()
                .allocationPlan(plan)
                .changeType(PlanChangeTypes.CREATE)
                .entityType("PLAN_CHANGE_LOG")
                .entityId(plan.getId())
                .newValue("{\"planName\":\"Controller Plan\"}")
                .createdAt(LocalDateTime.now())
                .reason("setup")
                .build();

        planChangeLogRepository.save(log);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetLogsForPlanAsAdmin() throws Exception {
        mockMvc.perform(get("/api/plan-change-logs/plans/" + testPlan.getId() + "/change-logs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
