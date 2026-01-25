package de.unipassau.allocationsystem.service;

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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link PlanChangeLogService}.
 * <p>
 * Validates plan change log creation and querying functionality.
 * </p>
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PlanChangeLogServiceTest {

    private final PlanChangeLogService service;
    private final PlanChangeLogRepository planChangeLogRepository;
    private final UserRepository userRepository;
    private final AllocationPlanRepository allocationPlanRepository;
    private final AcademicYearRepository academicYearRepository;

    private User testUser;
    private AllocationPlan testPlan;

    @Autowired
    PlanChangeLogServiceTest(PlanChangeLogService service,
                             PlanChangeLogRepository planChangeLogRepository,
                             UserRepository userRepository,
                             AllocationPlanRepository allocationPlanRepository,
                             AcademicYearRepository academicYearRepository) {
        this.service = service;
        this.planChangeLogRepository = planChangeLogRepository;
        this.userRepository = userRepository;
        this.allocationPlanRepository = allocationPlanRepository;
        this.academicYearRepository = academicYearRepository;
    }

    @BeforeEach
    void setUp() {
        planChangeLogRepository.deleteAll();
        allocationPlanRepository.deleteAll();
        academicYearRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("planchanges@test.example");
        testUser.setPassword(generateTestPassword());
        testUser.setFullName("Plan User");
        testUser.setEnabled(true);
        testUser = userRepository.save(testUser);

        AcademicYear year = new AcademicYear();
        year.setYearName("2024/2025");
        year.setTotalCreditHours(100);
        year.setElementarySchoolHours(20);
        year.setMiddleSchoolHours(30);
        year.setBudgetAnnouncementDate(LocalDateTime.now());
        year = academicYearRepository.save(year);

        testPlan = new AllocationPlan();
        testPlan.setPlanName("Test Plan");
        testPlan.setPlanVersion("v1");
        testPlan.setStatus(AllocationPlan.PlanStatus.DRAFT);
        testPlan.setAcademicYear(year);
        testPlan = allocationPlanRepository.save(testPlan);
    }

    private static String generateTestPassword() {
        return "test-" + UUID.randomUUID();
    }

    @Test
    void testLogPlanChangeAndQuery() {
        PlanChangeLog result = service.logPlanChange(
                testPlan.getId(),
                PlanChangeTypes.CREATE,
                "PLAN_CHANGE_LOG",
                testPlan.getId(),
                null,
                Map.of("planName", "Test Plan"),
                "initial create"
        );

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(testPlan.getId(), result.getAllocationPlan().getId());
        assertEquals(PlanChangeTypes.CREATE, result.getChangeType());

        Page<PlanChangeLog> page = planChangeLogRepository.findByFilters(
                testPlan.getId(), null, null, null, null, PageRequest.of(0, 10)
        );
        assertTrue(page.getTotalElements() > 0);
    }
}
