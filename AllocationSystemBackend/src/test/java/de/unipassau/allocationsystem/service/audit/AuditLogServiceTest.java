package de.unipassau.allocationsystem.service.audit;

import de.unipassau.allocationsystem.entity.AuditLog;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.repository.AuditLogRepository;
import de.unipassau.allocationsystem.repository.UserRepository;
import de.unipassau.allocationsystem.testutil.TestUserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuditLogServiceTest {

    private final AuditLogService auditLogService;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    private User testUser;

    @Autowired
    AuditLogServiceTest(AuditLogService auditLogService,
                        AuditLogRepository auditLogRepository,
                        UserRepository userRepository) {
        this.auditLogService = auditLogService;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        userRepository.deleteAll();

        testUser = userRepository.save(TestUserFactory.newEnabledUser("test@example.com", "Test User"));
    }

    @Test
    void testLogAuditEntry() {
        AuditLog result = auditLogService.log(
                testUser,
                AuditAction.UPDATE,
                "User",
                "123",
                Map.of("name", "Old Name"),
                Map.of("name", "New Name"),
                "Updated user profile"
        );

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(testUser.getEmail(), result.getUserIdentifier());
        assertEquals(AuditAction.UPDATE, result.getAction());
        assertEquals("User", result.getTargetEntity());
        assertEquals("123", result.getTargetRecordId());
        assertEquals("Updated user profile", result.getDescription());
        assertNotNull(result.getPreviousValue());
        assertNotNull(result.getNewValue());
        assertNotNull(result.getEventTimestamp());
    }

    @Test
    void testLogCreateAsync() {
        auditLogService.logCreate("User", "456", Map.of("email", "newuser@example.com", "name", "New User"));

        AuditLog log = awaitFirstAuditLogFor("User", "456", Duration.ofSeconds(5));

        assertNotNull(log);
        assertEquals(AuditAction.CREATE, log.getAction());
        assertEquals("User", log.getTargetEntity());
        assertEquals("456", log.getTargetRecordId());
    }

    @Test
    void testGetAuditLogsWithFilters() {
        auditLogService.log(testUser, AuditAction.CREATE, "User", "1", null,
                Map.of("name", "User 1"), "Created user 1");
        auditLogService.log(testUser, AuditAction.UPDATE, "User", "1",
                Map.of("name", "User 1"), Map.of("name", "User 1 Updated"), "Updated user 1");
        auditLogService.log(testUser, AuditAction.DELETE, "Role", "2",
                Map.of("name", "Role 1"), null, "Deleted role 2");

        Page<AuditLog> updateLogs = auditLogRepository.findByAction(
                AuditAction.UPDATE, PageRequest.of(0, 10)
        );
        assertEquals(1, updateLogs.getTotalElements());
        assertEquals(AuditAction.UPDATE, updateLogs.getContent().get(0).getAction());

        Page<AuditLog> userLogs = auditLogRepository.findByTargetEntity(
                "User", PageRequest.of(0, 10)
        );
        assertEquals(2, userLogs.getTotalElements());
    }

    private AuditLog awaitFirstAuditLogFor(String entityName, String recordId, Duration timeout) {
        long deadlineNanos = System.nanoTime() + timeout.toNanos();

        while (System.nanoTime() < deadlineNanos) {
            Page<AuditLog> logs = auditLogRepository.findByTargetEntityAndTargetRecordId(
                    entityName, recordId, PageRequest.of(0, 1)
            );
            if (logs.hasContent()) {
                return logs.getContent().get(0);
            }
            Thread.onSpinWait();
        }

        fail("Timed out waiting for async audit log entry for " + entityName + "/" + recordId);
        return null; // unreachable
    }
}
