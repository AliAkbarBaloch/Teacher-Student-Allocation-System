package de.unipassau.allocationsystem.service;

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

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AuditLogService.
 */
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
        // Clean up audit logs before each test
        auditLogRepository.deleteAll();
        
        // Create test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setFullName("Test User");
        testUser.setEnabled(true);
        testUser = userRepository.save(testUser);
    }

    @Test
    void testLogAuditEntry() {
        // Given
        String entityName = "User";
        String recordId = "123";
        Object previousValue = Map.of("name", "Old Name");
        Object newValue = Map.of("name", "New Name");
        String description = "Updated user profile";

        // When
        AuditLog result = auditLogService.log(
            testUser,
            AuditAction.UPDATE,
            entityName,
            recordId,
            previousValue,
            newValue,
            description
        );

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(testUser.getEmail(), result.getUserIdentifier());
        assertEquals(AuditAction.UPDATE, result.getAction());
        assertEquals(entityName, result.getTargetEntity());
        assertEquals(recordId, result.getTargetRecordId());
        assertEquals(description, result.getDescription());
        assertNotNull(result.getPreviousValue());
        assertNotNull(result.getNewValue());
        assertNotNull(result.getEventTimestamp());
    }

    @Test
    void testLogCreate() {
        // Given
        String entityName = "User";
        String recordId = "456";
        Object newValue = Map.of("email", "newuser@example.com", "name", "New User");

        // When
        auditLogService.logCreate(entityName, recordId, newValue);

        // Then - wait a bit for async processing
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        Page<AuditLog> logs = auditLogRepository.findByTargetEntityAndTargetRecordId(
            entityName, recordId, PageRequest.of(0, 10)
        );

        assertTrue(logs.getTotalElements() > 0);
        AuditLog log = logs.getContent().get(0);
        assertEquals(AuditAction.CREATE, log.getAction());
        assertEquals(entityName, log.getTargetEntity());
        assertEquals(recordId, log.getTargetRecordId());
    }

    @Test
    void testGetAuditLogsWithFilters() {
        // Given - create multiple audit logs
        auditLogService.log(testUser, AuditAction.CREATE, "User", "1", null, 
            Map.of("name", "User 1"), "Created user 1");
        auditLogService.log(testUser, AuditAction.UPDATE, "User", "1", 
            Map.of("name", "User 1"), Map.of("name", "User 1 Updated"), "Updated user 1");
        auditLogService.log(testUser, AuditAction.DELETE, "Role", "2", 
            Map.of("name", "Role 1"), null, "Deleted role 2");

        // When - filter by action
        Page<AuditLog> updateLogs = auditLogService.getAuditLogs(
            null, AuditAction.UPDATE, null, null, null, PageRequest.of(0, 10)
        );

        // Then
        assertEquals(1, updateLogs.getTotalElements());
        assertEquals(AuditAction.UPDATE, updateLogs.getContent().get(0).getAction());

        // When - filter by entity
        Page<AuditLog> userLogs = auditLogService.getAuditLogs(
            null, null, "User", null, null, PageRequest.of(0, 10)
        );

        // Then
        assertEquals(2, userLogs.getTotalElements());
    }

    @Test
    void testGetActionStatistics() {
        // Given - create audit logs
        LocalDateTime startDate = LocalDateTime.now().minusHours(1);
        
        auditLogService.log(testUser, AuditAction.CREATE, "User", "1", null, null, "Created");
        auditLogService.log(testUser, AuditAction.CREATE, "User", "2", null, null, "Created");
        auditLogService.log(testUser, AuditAction.UPDATE, "User", "1", null, null, "Updated");
        auditLogService.log(testUser, AuditAction.DELETE, "User", "2", null, null, "Deleted");

        LocalDateTime endDate = LocalDateTime.now().plusHours(1);

        // When
        Map<String, Long> statistics = auditLogService.getActionStatistics(startDate, endDate);

        // Then
        assertNotNull(statistics);
        assertEquals(2L, statistics.get("CREATE"));
        assertEquals(1L, statistics.get("UPDATE"));
        assertEquals(1L, statistics.get("DELETE"));
    }

    @Test
    void testGetEntityStatistics() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusHours(1);
        
        auditLogService.log(testUser, AuditAction.CREATE, "User", "1", null, null, "Created user");
        auditLogService.log(testUser, AuditAction.CREATE, "User", "2", null, null, "Created user");
        auditLogService.log(testUser, AuditAction.CREATE, "Role", "1", null, null, "Created role");

        LocalDateTime endDate = LocalDateTime.now().plusHours(1);

        // When
        Map<String, Long> statistics = auditLogService.getEntityStatistics(startDate, endDate);

        // Then
        assertNotNull(statistics);
        assertEquals(2L, statistics.get("User"));
        assertEquals(1L, statistics.get("Role"));
    }

    @Test
    void testGetAuditLogsForEntity() {
        // Given
        String entityName = "User";
        String recordId = "123";
        
        auditLogService.log(testUser, AuditAction.CREATE, entityName, recordId, null, 
            Map.of("name", "Test"), "Created");
        auditLogService.log(testUser, AuditAction.UPDATE, entityName, recordId, 
            Map.of("name", "Test"), Map.of("name", "Test Updated"), "Updated");

        // When
        Page<AuditLog> logs = auditLogService.getAuditLogsForEntity(
            entityName, recordId, PageRequest.of(0, 10)
        );

        // Then
        assertEquals(2, logs.getTotalElements());
        logs.getContent().forEach(log -> {
            assertEquals(entityName, log.getTargetEntity());
            assertEquals(recordId, log.getTargetRecordId());
        });
    }
}
