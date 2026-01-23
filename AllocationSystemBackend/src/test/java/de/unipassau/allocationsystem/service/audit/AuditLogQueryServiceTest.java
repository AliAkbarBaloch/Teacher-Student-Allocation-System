package de.unipassau.allocationsystem.service.audit;

import de.unipassau.allocationsystem.entity.AuditLog;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.repository.AuditLogRepository;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuditLogQueryServiceTest {

    private final AuditLogQueryService auditLogQueryService;
    private final AuditLogService auditLogService;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    private User testUser;

    @Autowired
    AuditLogQueryServiceTest(AuditLogQueryService auditLogQueryService,
                            AuditLogService auditLogService,
                            AuditLogRepository auditLogRepository,
                            UserRepository userRepository) {
        this.auditLogQueryService = auditLogQueryService;
        this.auditLogService = auditLogService;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.save(newTestUser("test@example.com", "Test User"));
    }

    private static User newTestUser(String email, String fullName) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(generateTestPassword());
        user.setFullName(fullName);
        user.setEnabled(true);
        return user;
    }

    private static String generateTestPassword() {
        return "test-" + UUID.randomUUID();
    }

    @Test
    void testGetActionStatistics() {
        LocalDateTime startDate = LocalDateTime.now().minusHours(1);

        auditLogService.log(testUser, AuditAction.CREATE, "User", "1", null, null, "Created");
        auditLogService.log(testUser, AuditAction.CREATE, "User", "2", null, null, "Created");
        auditLogService.log(testUser, AuditAction.UPDATE, "User", "1", null, null, "Updated");
        auditLogService.log(testUser, AuditAction.DELETE, "User", "2", null, null, "Deleted");

        LocalDateTime endDate = LocalDateTime.now().plusHours(1);

        Map<String, Long> statistics = auditLogQueryService.getActionStatistics(startDate, endDate);

        assertNotNull(statistics);
        assertEquals(2L, statistics.get("CREATE"));
        assertEquals(1L, statistics.get("UPDATE"));
        assertEquals(1L, statistics.get("DELETE"));
    }

    @Test
    void testGetEntityStatistics() {
        LocalDateTime startDate = LocalDateTime.now().minusHours(1);

        auditLogService.log(testUser, AuditAction.CREATE, "User", "1", null, null, "Created user");
        auditLogService.log(testUser, AuditAction.CREATE, "User", "2", null, null, "Created user");
        auditLogService.log(testUser, AuditAction.CREATE, "Role", "1", null, null, "Created role");

        LocalDateTime endDate = LocalDateTime.now().plusHours(1);

        Map<String, Long> statistics = auditLogQueryService.getEntityStatistics(startDate, endDate);

        assertNotNull(statistics);
        assertEquals(2L, statistics.get("User"));
        assertEquals(1L, statistics.get("Role"));
    }

    @Test
    void testGetAuditLogsForEntity() {
        String entityName = "User";
        String recordId = "123";

        auditLogService.log(
                testUser,
                AuditAction.CREATE,
                entityName,
                recordId,
                null,
                Map.of("name", "Test"),
                "Created"
        );

        auditLogService.log(
                testUser,
                AuditAction.UPDATE,
                entityName,
                recordId,
                Map.of("name", "Test"),
                Map.of("name", "Test Updated"),
                "Updated"
        );

        Page<AuditLog> logs = auditLogQueryService.getAuditLogsForEntity(
                entityName, recordId, PageRequest.of(0, 10)
        );

        assertEquals(2, logs.getTotalElements());
        logs.getContent().forEach(log -> {
            assertEquals(entityName, log.getTargetEntity());
            assertEquals(recordId, log.getTargetRecordId());
        });
    }
}
