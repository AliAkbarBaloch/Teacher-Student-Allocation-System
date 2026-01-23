package de.unipassau.allocationsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.repository.AuditLogRepository;
import de.unipassau.allocationsystem.repository.UserRepository;
import de.unipassau.allocationsystem.service.audit.AuditLogService;
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
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for AuditLogController.
 * Tests all REST endpoints with proper authentication and authorization.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("test")
@Transactional
class AuditLogControllerTest {

    private static final String TEST_USER_EMAIL = "testuser@example.com";
    private static final String ADMIN_EMAIL = "admin@example.com";

    /**
     * Test-only dummy passwords.
     * <p>
     * These are not real secrets; they are only used to satisfy entity constraints.
     * If your build still flags constants, generate values dynamically (e.g. UUID).
     * </p>
     */
    private static final String TEST_USER_PASSWORD = "test-password";
    private static final String ADMIN_PASSWORD = "admin-password";

    private final MockMvc mockMvc;
    private final AuditLogRepository auditLogRepository;
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;

    private User testUser;
    private User adminUser;

    @Autowired
    AuditLogControllerTest(
            MockMvc mockMvc,
            AuditLogRepository auditLogRepository,
            AuditLogService auditLogService,
            UserRepository userRepository
    ) {
        this.mockMvc = mockMvc;
        this.auditLogRepository = auditLogRepository;
        this.auditLogService = auditLogService;
        this.userRepository = userRepository;
    }

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        testUser = findOrCreateUser(TEST_USER_EMAIL, TEST_USER_PASSWORD, "Test User", null);
        adminUser = findOrCreateUser(ADMIN_EMAIL, ADMIN_PASSWORD, "Admin User", User.UserRole.ADMIN);
        createSampleAuditLogs();
    }

    private User findOrCreateUser(String email, String password, String fullName, User.UserRole role) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);

            // Test-only dummy value to satisfy constraints; not a real credential.
            newUser.setPassword(password);

            newUser.setFullName(fullName);
            newUser.setEnabled(true);

            if (role != null) {
                newUser.setRole(role);
            }

            return userRepository.save(newUser);
        });
    }

    private void createSampleAuditLogs() {
        auditLogService.log(
                testUser,
                AuditAction.CREATE,
                "User",
                "1",
                null,
                Map.of("name", "John Doe"),
                "Created user"
        );

        auditLogService.log(
                testUser,
                AuditAction.UPDATE,
                "User",
                "1",
                Map.of("name", "John Doe"),
                Map.of("name", "John Smith"),
                "Updated user"
        );

        auditLogService.log(
                testUser,
                AuditAction.DELETE,
                "Role",
                "2",
                Map.of("name", "Editor"),
                null,
                "Deleted role"
        );

        auditLogService.log(
                adminUser,
                AuditAction.VIEW,
                "Student",
                "123",
                null,
                null,
                "Viewed student"
        );

        auditLogService.log(
                adminUser,
                AuditAction.LOGIN,
                "Authentication",
                null,
                null,
                null,
                "User logged in"
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllAuditLogs() throws Exception {
        mockMvc.perform(get("/api/audit-logs")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.data.totalElements").value(greaterThan(0)))
                .andExpect(jsonPath("$.data.content[0].id").exists())
                .andExpect(jsonPath("$.data.content[0].userIdentifier").exists())
                .andExpect(jsonPath("$.data.content[0].action").exists())
                .andExpect(jsonPath("$.data.content[0].targetEntity").exists());
    }

    @Test
    void testGetAuditLogsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/audit-logs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAuditLogsWithFilters() throws Exception {
        mockMvc.perform(get("/api/audit-logs")
                        .param("action", "CREATE")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[*].action", everyItem(is("CREATE"))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAuditLogsWithTargetEntityFilter() throws Exception {
        mockMvc.perform(get("/api/audit-logs")
                        .param("targetEntity", "User")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[*].targetEntity", everyItem(is("User"))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAuditLogsWithDateRangeFilter() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusDays(1);
        LocalDateTime endDate = now.plusDays(1);

        mockMvc.perform(get("/api/audit-logs")
                        .param("startDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .param("endDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAuditLogsWithSorting() throws Exception {
        mockMvc.perform(get("/api/audit-logs")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "eventTimestamp")
                        .param("sortDirection", "ASC"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAuditLogsForEntity() throws Exception {
        mockMvc.perform(get("/api/audit-logs/entity/User/1")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[*].targetEntity", everyItem(is("User"))))
                .andExpect(jsonPath("$.data.content[*].targetRecordId", everyItem(is("1"))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAuditLogsForUser() throws Exception {
        mockMvc.perform(get("/api/audit-logs/user/" + testUser.getEmail())
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[*].userIdentifier",
                        everyItem(is(testUser.getEmail()))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetRecentAuditLogs() throws Exception {
        mockMvc.perform(get("/api/audit-logs/recent"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(lessThanOrEqualTo(100))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetStatistics() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusDays(1);
        LocalDateTime endDate = now.plusDays(1);

        mockMvc.perform(get("/api/audit-logs/statistics")
                        .param("startDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .param("endDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.actionStatistics").exists())
                .andExpect(jsonPath("$.data.entityStatistics").exists())
                .andExpect(jsonPath("$.data.userActivityStatistics").exists())
                .andExpect(jsonPath("$.data.totalLogs").exists())
                .andExpect(jsonPath("$.data.totalLogs").value(greaterThan(0)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testExportAuditLogs() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusDays(1);
        LocalDateTime endDate = now.plusDays(1);

        mockMvc.perform(get("/api/audit-logs/export")
                        .param("startDate", startDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .param("endDate", endDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .param("maxRecords", "1000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/csv")))
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().string(containsString("ID,User,Event Time,Action")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testExportAuditLogsWithActionFilter() throws Exception {
        mockMvc.perform(get("/api/audit-logs/export")
                        .param("action", "CREATE")
                        .param("maxRecords", "1000"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/csv")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testPaginationWorks() throws Exception {
        mockMvc.perform(get("/api/audit-logs")
                        .param("page", "0")
                        .param("size", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(lessThanOrEqualTo(2))))
                .andExpect(jsonPath("$.data.number").value(0))
                .andExpect(jsonPath("$.data.size").value(2));

        mockMvc.perform(get("/api/audit-logs")
                        .param("page", "1")
                        .param("size", "2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.number").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAuditLogsWithMultipleFilters() throws Exception {
        mockMvc.perform(get("/api/audit-logs")
                        .param("action", "UPDATE")
                        .param("targetEntity", "User")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[*].action", everyItem(is("UPDATE"))))
                .andExpect(jsonPath("$.data.content[*].targetEntity", everyItem(is("User"))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAuditLogsForNonExistentEntity() throws Exception {
        mockMvc.perform(get("/api/audit-logs/entity/NonExistent/999")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAuditLogsForNonExistentUser() throws Exception {
        mockMvc.perform(get("/api/audit-logs/user/nonexistent@example.com")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAuditLogContainsAllRequiredFields() throws Exception {
        mockMvc.perform(get("/api/audit-logs")
                        .param("page", "0")
                        .param("size", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").exists())
                .andExpect(jsonPath("$.data.content[0].userIdentifier").exists())
                .andExpect(jsonPath("$.data.content[0].eventTimestamp").exists())
                .andExpect(jsonPath("$.data.content[0].action").exists())
                .andExpect(jsonPath("$.data.content[0].targetEntity").exists())
                .andExpect(jsonPath("$.data.content[0].createdAt").exists());
    }
}
