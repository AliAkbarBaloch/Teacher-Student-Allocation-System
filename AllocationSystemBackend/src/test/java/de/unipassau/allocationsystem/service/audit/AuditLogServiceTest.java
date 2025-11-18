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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuditLogServiceTest {

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setFullName("Test User");
        testUser.setEnabled(true);
        testUser = userRepository.save(testUser);
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
    void testLogCreateAsync() throws InterruptedException {
        auditLogService.logCreate("User", "456", Map.of("email", "newuser@example.com", "name", "New User"));
        Thread.sleep(500); // Wait for async processing

        Page<AuditLog> logs = auditLogRepository.findByTargetEntityAndTargetRecordId(
                "User", "456", PageRequest.of(0, 10)
        );

        assertTrue(logs.getTotalElements() > 0);
        AuditLog log = logs.getContent().get(0);
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

}
