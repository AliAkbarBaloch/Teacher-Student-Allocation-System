package de.unipassau.allocationsystem.controller;

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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    private static final String AUDIT_LOGS_ENDPOINT = "/api/audit-logs";

    private final MockMvc mockMvc;
    private final AuditLogRepository auditLogRepository;
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;

    private User testUser;
    private User adminUser;

    @Autowired
    AuditLogControllerTest(
            @Autowired
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
        ResultActions result = getAuditLogsPage(0, 10);

        assertOkJson(result);
        assertHasContentArray(result);
        assertHasMinimumElements(result, 1);
        assertHasBasicAuditFields(result);
    }

    @Test
    void testGetAuditLogsWithoutAuthentication() throws Exception {
        mockMvc.perform(get(AUDIT_LOGS_ENDPOINT))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAuditLogsWithFilters() throws Exception {
        ResultActions result = getAuditLogsFiltered("CREATE", null, 0, 10);

        assertOkJson(result);
        assertHasContentArray(result);
        result.andExpect(jsonPath("$.data.content[*].action", everyItem(is("CREATE"))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAuditLogsWithTargetEntityFilter() throws Exception {
        ResultActions result = getAuditLogsFiltered(null, "User", 0, 10);

        assertOkJson(result);
        assertHasContentArray(result);
        result.andExpect(jsonPath("$.data.content[*].targetEntity", everyItem(is("User"))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAuditLogsWithDateRangeFilter() throws Exception {
        DateRange range = DateRange.aroundNowDays(1);

        ResultActions result = getAuditLogsWithDateRange(range, 0, 10);

        assertOkJson(result);
        assertHasContentArray(result);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAuditLogsWithSorting() throws Exception {
        ResultActions result = mockMvc.perform(get(AUDIT_LOGS_ENDPOINT)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "eventTimestamp")
                        .param("sortDirection", "ASC"));

        assertOkJson(result);
        assertHasContentArray(result);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAuditLogsForEntity() throws Exception {
        ResultActions result = mockMvc.perform(get(AUDIT_LOGS_ENDPOINT + "/entity/User/1")
                        .param("page", "0")
                        .param("size", "10"));

        assertOkJson(result);
        assertHasContentArray(result);
        result.andExpect(jsonPath("$.data.content[*].targetEntity", everyItem(is("User"))));
        result.andExpect(jsonPath("$.data.content[*].targetRecordId", everyItem(is("1"))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAuditLogsForUser() throws Exception {
        ResultActions result = mockMvc.perform(get(AUDIT_LOGS_ENDPOINT + "/user/" + testUser.getEmail())
                        .param("page", "0")
                        .param("size", "10"));

        assertOkJson(result);
        assertHasContentArray(result);
        result.andExpect(jsonPath("$.data.content[*].userIdentifier", everyItem(is(testUser.getEmail()))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetRecentAuditLogs() throws Exception {
        ResultActions result = mockMvc.perform(get(AUDIT_LOGS_ENDPOINT + "/recent"));

        assertOkJson(result);
        result.andExpect(jsonPath("$.data").isArray());
        result.andExpect(jsonPath("$.data", hasSize(lessThanOrEqualTo(100))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetStatistics() throws Exception {
        DateRange range = DateRange.aroundNowDays(1);

        ResultActions result = mockMvc.perform(get(AUDIT_LOGS_ENDPOINT + "/statistics")
                        .param("startDate", range.startIso())
                        .param("endDate", range.endIso()));

        assertOkJson(result);
        result.andExpect(jsonPath("$.data.actionStatistics").exists());
        result.andExpect(jsonPath("$.data.entityStatistics").exists());
        result.andExpect(jsonPath("$.data.userActivityStatistics").exists());
        result.andExpect(jsonPath("$.data.totalLogs").exists());
        result.andExpect(jsonPath("$.data.totalLogs").value(greaterThan(0)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testExportAuditLogs() throws Exception {
        DateRange range = DateRange.aroundNowDays(1);

        ResultActions result = mockMvc.perform(get(AUDIT_LOGS_ENDPOINT + "/export")
                        .param("startDate", range.startIso())
                        .param("endDate", range.endIso())
                        .param("maxRecords", "1000"));

        result.andExpect(status().isOk());
        result.andExpect(header().string("Content-Type", containsString("text/csv")));
        result.andExpect(header().exists("Content-Disposition"));
        result.andExpect(content().string(containsString("ID,User,Event Time,Action")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testExportAuditLogsWithActionFilter() throws Exception {
        ResultActions result = mockMvc.perform(get(AUDIT_LOGS_ENDPOINT + "/export")
                        .param("action", "CREATE")
                        .param("maxRecords", "1000"));

        result.andExpect(status().isOk());
        result.andExpect(header().string("Content-Type", containsString("text/csv")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testPaginationWorks() throws Exception {
        ResultActions firstPage = getAuditLogsPage(0, 2);

        assertOkJson(firstPage);
        firstPage.andExpect(jsonPath("$.data.content", hasSize(lessThanOrEqualTo(2))));
        firstPage.andExpect(jsonPath("$.data.number").value(0));
        firstPage.andExpect(jsonPath("$.data.size").value(2));

        ResultActions secondPage = getAuditLogsPage(1, 2);

        assertOkJson(secondPage);
        secondPage.andExpect(jsonPath("$.data.number").value(1));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAuditLogsWithMultipleFilters() throws Exception {
        ResultActions result = getAuditLogsFiltered("UPDATE", "User", 0, 10);

        assertOkJson(result);
        assertHasContentArray(result);
        result.andExpect(jsonPath("$.data.content[*].action", everyItem(is("UPDATE"))));
        result.andExpect(jsonPath("$.data.content[*].targetEntity", everyItem(is("User"))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAuditLogsForNonExistentEntity() throws Exception {
        ResultActions result = mockMvc.perform(get(AUDIT_LOGS_ENDPOINT + "/entity/NonExistent/999")
                        .param("page", "0")
                        .param("size", "10"));

        assertOkJson(result);
        result.andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAuditLogsForNonExistentUser() throws Exception {
        ResultActions result = mockMvc.perform(get(AUDIT_LOGS_ENDPOINT + "/user/nonexistent@example.com")
                        .param("page", "0")
                        .param("size", "10"));

        assertOkJson(result);
        result.andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testAuditLogContainsAllRequiredFields() throws Exception {
        ResultActions result = getAuditLogsPage(0, 1);

        assertOkJson(result);
        assertHasRequiredAuditFields(result);
    }

    private ResultActions getAuditLogsPage(int page, int size) throws Exception {
        return mockMvc.perform(get(AUDIT_LOGS_ENDPOINT)
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size)));
    }

    private ResultActions getAuditLogsFiltered(String action, String targetEntity, int page, int size) throws Exception {
        org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request =
                get(AUDIT_LOGS_ENDPOINT)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size));

        if (action != null) {
            request = request.param("action", action);
        }
        if (targetEntity != null) {
            request = request.param("targetEntity", targetEntity);
        }

        return mockMvc.perform(request);
    }

    private ResultActions getAuditLogsWithDateRange(DateRange range, int page, int size) throws Exception {
        return mockMvc.perform(get(AUDIT_LOGS_ENDPOINT)
                .param("startDate", range.startIso())
                .param("endDate", range.endIso())
                .param("page", String.valueOf(page))
                .param("size", String.valueOf(size)));
    }

    private void assertOkJson(ResultActions result) throws Exception {
        result.andExpect(status().isOk());
        result.andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    private void assertHasContentArray(ResultActions result) throws Exception {
        result.andExpect(jsonPath("$.data.content").isArray());
    }

    private void assertHasMinimumElements(ResultActions result, int minimum) throws Exception {
        result.andExpect(jsonPath("$.data.totalElements").value(greaterThanOrEqualTo(minimum)));
        result.andExpect(jsonPath("$.data.content", hasSize(greaterThanOrEqualTo(minimum))));
    }

    private void assertHasBasicAuditFields(ResultActions result) throws Exception {
        result.andExpect(jsonPath("$.data.content[0].id").exists());
        result.andExpect(jsonPath("$.data.content[0].userIdentifier").exists());
        result.andExpect(jsonPath("$.data.content[0].action").exists());
        result.andExpect(jsonPath("$.data.content[0].targetEntity").exists());
    }

    private void assertHasRequiredAuditFields(ResultActions result) throws Exception {
        result.andExpect(jsonPath("$.data.content[0].id").exists());
        result.andExpect(jsonPath("$.data.content[0].userIdentifier").exists());
        result.andExpect(jsonPath("$.data.content[0].eventTimestamp").exists());
        result.andExpect(jsonPath("$.data.content[0].action").exists());
        result.andExpect(jsonPath("$.data.content[0].targetEntity").exists());
        result.andExpect(jsonPath("$.data.content[0].createdAt").exists());
    }

    private record DateRange(LocalDateTime start, LocalDateTime end) {

        static DateRange aroundNowDays(int days) {
            LocalDateTime now = LocalDateTime.now();
            return new DateRange(now.minusDays(days), now.plusDays(days));
        }

        String startIso() {
            return start.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }

        String endIso() {
            return end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }
}
