package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.entity.AllocationPlan;
import de.unipassau.allocationsystem.entity.PlanChangeLog;
import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.repository.AllocationPlanRepository;
import de.unipassau.allocationsystem.repository.PlanChangeLogRepository;
import de.unipassau.allocationsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PlanChangeLogServiceTest {

    @Autowired
    private PlanChangeLogService service;

    @Autowired
    private PlanChangeLogRepository planChangeLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AllocationPlanRepository allocationPlanRepository;

    @Autowired
    private de.unipassau.allocationsystem.repository.AcademicYearRepository academicYearRepository;

    private User testUser;
    private AllocationPlan testPlan;

    @BeforeEach
    void setUp() {
        planChangeLogRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("planchanges@test.example");
        testUser.setPassword("password");
        testUser.setFullName("Plan User");
        testUser.setEnabled(true);
        testUser = userRepository.save(testUser);

        // Create and save a minimal valid AcademicYear for the plan
        var year = new de.unipassau.allocationsystem.entity.AcademicYear();
        year.setYearName("2024/2025");
        year.setTotalCreditHours(100);
        year.setElementarySchoolHours(20);
        year.setMiddleSchoolHours(30);
        year.setBudgetAnnouncementDate(java.time.LocalDateTime.now());
        year = academicYearRepository.save(year);
        testPlan = new AllocationPlan();
        testPlan.setPlanName("Test Plan");
        testPlan.setPlanVersion("v1");
        testPlan.setStatus(AllocationPlan.PlanStatus.DRAFT);
        testPlan.setAcademicYear(year);
        testPlan = allocationPlanRepository.save(testPlan);
    }

    @Test
    void testLogPlanChangeAndQuery() {
        PlanChangeLog result = service.logPlanChange(
            testPlan.getId(),
            testUser.getId(),
            de.unipassau.allocationsystem.constant.PlanChangeTypes.CREATE,
            "PLAN_CHANGE_LOG",
            testPlan.getId(),
            null,
            Map.of("planName", "Test Plan"),
            "initial create"
        );

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(testPlan.getId(), result.getAllocationPlan().getId());
        assertEquals(testUser.getId(), result.getUser().getId());
        assertEquals(de.unipassau.allocationsystem.constant.PlanChangeTypes.CREATE, result.getChangeType());

        Page<PlanChangeLog> page = planChangeLogRepository.findByFilters(
                testPlan.getId(), null, null, null, null, null, PageRequest.of(0, 10)
        );
        assertTrue(page.getTotalElements() > 0);
    }
}
