package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.entity.AllocationPlan;
import de.unipassau.allocationsystem.entity.PlanChangeLog;
import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.repository.AllocationPlanRepository;
import de.unipassau.allocationsystem.repository.PlanChangeLogRepository;
import de.unipassau.allocationsystem.repository.UserRepository;
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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.sql.init.mode=never")
@ActiveProfiles("test")
@Transactional
class PlanChangeLogControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AllocationPlanRepository allocationPlanRepository;

    @Autowired
    private de.unipassau.allocationsystem.repository.AcademicYearRepository academicYearRepository;

    @Autowired
    private PlanChangeLogRepository planChangeLogRepository;

    private User testUser;
    private AllocationPlan testPlan;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        planChangeLogRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("controller@test.example");
        testUser.setPassword("password");
        testUser.setFullName("Controller User");
        testUser.setEnabled(true);
        testUser = userRepository.save(testUser);

        var year = new de.unipassau.allocationsystem.entity.AcademicYear();
        year.setYearName("2025/2026");
        year.setTotalCreditHours(100);
        year.setElementarySchoolHours(20);
        year.setMiddleSchoolHours(30);
        year.setBudgetAnnouncementDate(java.time.LocalDateTime.now());
        year = academicYearRepository.save(year);

        testPlan = new AllocationPlan();
        testPlan.setPlanName("Controller Plan");
        testPlan.setPlanVersion("v1");
        testPlan.setStatus(AllocationPlan.PlanStatus.DRAFT);
        testPlan.setAcademicYear(year);
        testPlan.setCreatedByUser(testUser);
        testPlan = allocationPlanRepository.save(testPlan);

        PlanChangeLog log = PlanChangeLog.builder()
                .allocationPlan(testPlan)
                .user(testUser)
            .changeType(de.unipassau.allocationsystem.constant.PlanChangeTypes.CREATE)
                .entityType("PLAN_CHANGE_LOG")
                .entityId(testPlan.getId())
                .newValue("{\"planName\":\"Controller Plan\"}")
                .eventTimestamp(java.time.LocalDateTime.now())
                .reason("setup")
                .build();
        planChangeLogRepository.save(log);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetLogsForPlanAsAdmin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/plan-change-logs/plans/" + testPlan.getId() + "/change-logs")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
