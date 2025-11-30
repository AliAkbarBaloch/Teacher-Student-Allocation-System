package de.unipassau.allocationsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.allocationsystem.dto.auth.PasswordResetDto;
import de.unipassau.allocationsystem.dto.user.UserCreateDto;
import de.unipassau.allocationsystem.dto.user.UserUpdateDto;
import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.entity.User.AccountStatus;
import de.unipassau.allocationsystem.entity.User.UserRole;
import de.unipassau.allocationsystem.repository.UserRepository;
import de.unipassau.allocationsystem.service.UserService;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.sql.init.mode=never")
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        // Create a test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setFullName("Test User");
        testUser.setRole(UserRole.USER);
        testUser.setEnabled(true);
        testUser.setAccountStatus(AccountStatus.ACTIVE);
        testUser.setAccountLocked(false);
        testUser.setFailedLoginAttempts(0);
        testUser = userRepository.save(testUser);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_Success() throws Exception {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail("newuser@example.com");
        dto.setPassword("password123");
        dto.setFullName("New User");
        dto.setRole(UserRole.USER);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.data.fullName").value("New User"))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.accountStatus").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_DuplicateEmail_ShouldFail() throws Exception {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail("test@example.com"); // Duplicate email
        dto.setPassword("password123");
        dto.setFullName("Duplicate User");
        dto.setRole(UserRole.USER);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_InvalidEmail_ShouldFail() throws Exception {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail("invalid-email"); // Invalid email
        dto.setPassword("password123");
        dto.setFullName("Invalid User");
        dto.setRole(UserRole.USER);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUser_ShortPassword_ShouldFail() throws Exception {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail("newuser@example.com");
        dto.setPassword("short"); // Too short
        dto.setFullName("New User");
        dto.setRole(UserRole.USER);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createUser_WithoutAdminRole_ShouldFail() throws Exception {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail("newuser@example.com");
        dto.setPassword("password123");
        dto.setFullName("New User");
        dto.setRole(UserRole.USER);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_Success() throws Exception {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setFullName("Updated Name");
        dto.setPhoneNumber("+1234567890");

        mockMvc.perform(put("/api/users/" + testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Updated Name"))
                .andExpect(jsonPath("$.data.phoneNumber").value("+1234567890"))
                .andExpect(jsonPath("$.data.email").value(testUser.getEmail())); // Should remain unchanged
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_ChangeRole_Success() throws Exception {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setRole(UserRole.ADMIN);

        mockMvc.perform(put("/api/users/" + testUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUser_NotFound_ShouldFail() throws Exception {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setFullName("Updated Name");

        mockMvc.perform(put("/api/users/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_Success() throws Exception {
        mockMvc.perform(get("/api/users/" + testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testUser.getId()))
                .andExpect(jsonPath("$.data.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.data.fullName").value(testUser.getFullName()))
                .andExpect(jsonPath("$.data.password").doesNotExist()); // Should not expose password
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserById_NotFound_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/users/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_Success() throws Exception {
        // Create additional users
        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setPassword("password");
        user2.setFullName("User Two");
        user2.setRole(UserRole.ADMIN);
        user2.setEnabled(true);
        user2.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user2);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_FilterByRole_Success() throws Exception {
        // Create an admin user
        User adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("password");
        adminUser.setFullName("Admin User");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setEnabled(true);
        adminUser.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(adminUser);

        mockMvc.perform(get("/api/users")
                .param("role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].role").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_FilterByStatus_Success() throws Exception {
        // Create an inactive user
        User inactiveUser = new User();
        inactiveUser.setEmail("inactive@example.com");
        inactiveUser.setPassword("password");
        inactiveUser.setFullName("Inactive User");
        inactiveUser.setRole(UserRole.USER);
        inactiveUser.setEnabled(false);
        inactiveUser.setAccountStatus(AccountStatus.INACTIVE);
        userRepository.save(inactiveUser);

        mockMvc.perform(get("/api/users")
                .param("status", "INACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].accountStatus").value("INACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_FilterByEnabled_Success() throws Exception {
        mockMvc.perform(get("/api/users")
                .param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].enabled", everyItem(is(true))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_SearchByEmail_Success() throws Exception {
        mockMvc.perform(get("/api/users")
                .param("search", "test@example"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].email").value(testUser.getEmail()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_SearchByFullName_Success() throws Exception {
        mockMvc.perform(get("/api/users")
                .param("search", "Test User"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].fullName").value(testUser.getFullName()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_WithPagination_Success() throws Exception {
        // Create multiple users
        for (int i = 0; i < 5; i++) {
            User user = new User();
            user.setEmail("user" + i + "@example.com");
            user.setPassword("password");
            user.setFullName("User " + i);
            user.setRole(UserRole.USER);
            user.setEnabled(true);
            user.setAccountStatus(AccountStatus.ACTIVE);
            userRepository.save(user);
        }

        mockMvc.perform(get("/api/users")
                .param("page", "0")
                .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andExpect(jsonPath("$.data.totalElements").value(6))
                .andExpect(jsonPath("$.data.totalPages").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_WithSorting_Success() throws Exception {
        // Create another user
        User user2 = new User();
        user2.setEmail("aaa@example.com"); // Alphabetically before test@example.com
        user2.setPassword("password");
        user2.setFullName("AAA User");
        user2.setRole(UserRole.USER);
        user2.setEnabled(true);
        user2.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user2);

        mockMvc.perform(get("/api/users")
                .param("sortBy", "email")
                .param("sortDirection", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].email").value("aaa@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateUser_Success() throws Exception {
        // First deactivate the user
        testUser.setEnabled(false);
        testUser.setAccountStatus(AccountStatus.INACTIVE);
        userRepository.save(testUser);

        mockMvc.perform(patch("/api/users/" + testUser.getId() + "/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.accountStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.data.accountLocked").value(false))
                .andExpect(jsonPath("$.data.failedLoginAttempts").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateUser_NotFound_ShouldFail() throws Exception {
        mockMvc.perform(patch("/api/users/99999/activate"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateUser_Success() throws Exception {
        mockMvc.perform(patch("/api/users/" + testUser.getId() + "/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(false))
                .andExpect(jsonPath("$.data.accountStatus").value("INACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateUser_NotFound_ShouldFail() throws Exception {
        mockMvc.perform(patch("/api/users/99999/deactivate"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void resetPassword_Success() throws Exception {
        PasswordResetDto dto = new PasswordResetDto();
        dto.setNewPassword("newpassword123");

        mockMvc.perform(post("/api/users/" + testUser.getId() + "/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lastPasswordResetDate").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void resetPassword_ShortPassword_ShouldFail() throws Exception {
        PasswordResetDto dto = new PasswordResetDto();
        dto.setNewPassword("short");

        mockMvc.perform(post("/api/users/" + testUser.getId() + "/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void resetPassword_NotFound_ShouldFail() throws Exception {
        PasswordResetDto dto = new PasswordResetDto();
        dto.setNewPassword("newpassword123");

        mockMvc.perform(post("/api/users/99999/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_Success() throws Exception {
        mockMvc.perform(delete("/api/users/" + testUser.getId()))
                .andExpect(status().isNoContent());

        // Verify user is deleted
        mockMvc.perform(get("/api/users/" + testUser.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_NotFound_ShouldFail() throws Exception {
        mockMvc.perform(delete("/api/users/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserStatistics_Success() throws Exception {
        // Create users with different statuses
        User inactiveUser = new User();
        inactiveUser.setEmail("inactive@example.com");
        inactiveUser.setPassword("password");
        inactiveUser.setFullName("Inactive User");
        inactiveUser.setRole(UserRole.USER);
        inactiveUser.setEnabled(false);
        inactiveUser.setAccountStatus(AccountStatus.INACTIVE);
        userRepository.save(inactiveUser);

        User lockedUser = new User();
        lockedUser.setEmail("locked@example.com");
        lockedUser.setPassword("password");
        lockedUser.setFullName("Locked User");
        lockedUser.setRole(UserRole.USER);
        lockedUser.setEnabled(true);
        lockedUser.setAccountStatus(AccountStatus.ACTIVE);
        lockedUser.setAccountLocked(true);
        userRepository.save(lockedUser);

        User adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("password");
        adminUser.setFullName("Admin User");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setEnabled(true);
        adminUser.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(adminUser);

        mockMvc.perform(get("/api/users/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUsers").value(4))
                .andExpect(jsonPath("$.data.activeUsers").value(3))
                .andExpect(jsonPath("$.data.inactiveUsers").value(1))
                .andExpect(jsonPath("$.data.lockedUsers").value(1))
                .andExpect(jsonPath("$.data.adminUsers").value(1))
                .andExpect(jsonPath("$.data.regularUsers").value(3));
    }
}
