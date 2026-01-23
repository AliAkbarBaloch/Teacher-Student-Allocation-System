package de.unipassau.allocationsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.allocationsystem.dto.auth.PasswordResetDto;
import de.unipassau.allocationsystem.dto.user.UserCreateDto;
import de.unipassau.allocationsystem.dto.user.UserUpdateDto;
import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.entity.User.AccountStatus;
import de.unipassau.allocationsystem.entity.User.UserRole;
import de.unipassau.allocationsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for the {@link UserController}.
 * <p>
 * This test class validates comprehensive user management operations including CRUD,
 * filtering, pagination, sorting, user activation/deactivation, and password reset.
 * </p>
 */
@SpringBootTest(properties = "spring.sql.init.mode=never")
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

    private static final String BASE_URL = "/api/users";
    private static final String DEFAULT_PASSWORD = "password123";
    private static final String SIMPLE_PASSWORD = "password";
    private static final String NEW_PASSWORD = "newpassword123";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    private User testUser;

    UserControllerTest(MockMvc mockMvc, ObjectMapper objectMapper, UserRepository userRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        testUser = userRepository.save(buildUser("test@example.com", DEFAULT_PASSWORD, "Test User", UserRole.USER, true, AccountStatus.ACTIVE));
    }

    private User buildUser(String email, String password, String fullName, UserRole role, boolean enabled, AccountStatus status) {
        User u = new User();
        u.setEmail(email);
        u.setPassword(password);
        u.setFullName(fullName);
        u.setRole(role);
        u.setEnabled(enabled);
        u.setAccountStatus(status);
        u.setAccountLocked(false);
        u.setFailedLoginAttempts(0);
        return u;
    }

    private ResultActions createUser(UserCreateDto dto) throws Exception {
        return mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));
    }

    private ResultActions updateUser(long id, UserUpdateDto dto) throws Exception {
        return mockMvc.perform(put(BASE_URL + "/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));
    }

    private ResultActions resetPassword(long id, PasswordResetDto dto) throws Exception {
        return mockMvc.perform(post(BASE_URL + "/{id}/reset-password", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));
    }

    private ResultActions activate(long id) throws Exception {
        return mockMvc.perform(patch(BASE_URL + "/{id}/activate", id));
    }

    private ResultActions deactivate(long id) throws Exception {
        return mockMvc.perform(patch(BASE_URL + "/{id}/deactivate", id));
    }

    private ResultActions getById(long id) throws Exception {
        return mockMvc.perform(get(BASE_URL + "/{id}", id));
    }

    private ResultActions deleteById(long id) throws Exception {
        return mockMvc.perform(delete(BASE_URL + "/{id}", id));
    }

    // -------------------- CREATE --------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUserSuccess() throws Exception {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail("newuser@example.com");
        dto.setPassword(DEFAULT_PASSWORD);
        dto.setFullName("New User");
        dto.setRole(UserRole.USER);

        createUser(dto)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.data.fullName").value("New User"))
                .andExpect(jsonPath("$.data.role").value("USER"))
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.accountStatus").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUserDuplicateEmailShouldFail() throws Exception {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail(testUser.getEmail());
        dto.setPassword(DEFAULT_PASSWORD);
        dto.setFullName("Duplicate User");
        dto.setRole(UserRole.USER);

        createUser(dto).andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUserInvalidEmailShouldFail() throws Exception {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail("invalid-email");
        dto.setPassword(DEFAULT_PASSWORD);
        dto.setFullName("Invalid User");
        dto.setRole(UserRole.USER);

        createUser(dto).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUserShortPasswordShouldFail() throws Exception {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail("newuser@example.com");
        dto.setPassword("short");
        dto.setFullName("New User");
        dto.setRole(UserRole.USER);

        createUser(dto).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createUserWithoutAdminRoleShouldFail() throws Exception {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail("newuser@example.com");
        dto.setPassword(DEFAULT_PASSWORD);
        dto.setFullName("New User");
        dto.setRole(UserRole.USER);

        createUser(dto).andExpect(status().isForbidden());
    }

    // -------------------- UPDATE --------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserSuccess() throws Exception {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setFullName("Updated Name");
        dto.setPhoneNumber("+1234567890");

        updateUser(testUser.getId(), dto)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fullName").value("Updated Name"))
                .andExpect(jsonPath("$.data.phoneNumber").value("+1234567890"))
                .andExpect(jsonPath("$.data.email").value(testUser.getEmail()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserChangeRoleSuccess() throws Exception {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setRole(UserRole.ADMIN);

        updateUser(testUser.getId(), dto)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserNotFoundShouldFail() throws Exception {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setFullName("Updated Name");

        updateUser(99999L, dto).andExpect(status().isNotFound());
    }

    // -------------------- GET --------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserByIdSuccess() throws Exception {
        getById(testUser.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testUser.getId()))
                .andExpect(jsonPath("$.data.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.data.fullName").value(testUser.getFullName()))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserByIdNotFoundShouldFail() throws Exception {
        getById(99999L).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsersSuccess() throws Exception {
        userRepository.save(buildUser("user2@example.com", SIMPLE_PASSWORD, "User Two", UserRole.ADMIN, true, AccountStatus.ACTIVE));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsersFilterByRoleSuccess() throws Exception {
        userRepository.save(buildUser("admin@example.com", SIMPLE_PASSWORD, "Admin User", UserRole.ADMIN, true, AccountStatus.ACTIVE));

        mockMvc.perform(get(BASE_URL).param("role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].role").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsersFilterByStatusSuccess() throws Exception {
        userRepository.save(buildUser("inactive@example.com", SIMPLE_PASSWORD, "Inactive User", UserRole.USER, false, AccountStatus.INACTIVE));

        mockMvc.perform(get(BASE_URL).param("status", "INACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].accountStatus").value("INACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsersFilterByEnabledSuccess() throws Exception {
        mockMvc.perform(get(BASE_URL).param("enabled", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[*].enabled", everyItem(is(true))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsersSearchByEmailSuccess() throws Exception {
        mockMvc.perform(get(BASE_URL).param("search", "test@example"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].email").value(testUser.getEmail()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsersSearchByFullNameSuccess() throws Exception {
        mockMvc.perform(get(BASE_URL).param("search", "Test User"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].fullName").value(testUser.getFullName()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsersWithPaginationSuccess() throws Exception {
        for (int i = 0; i < 5; i++) {
            userRepository.save(buildUser("user" + i + "@example.com", SIMPLE_PASSWORD, "User " + i, UserRole.USER, true, AccountStatus.ACTIVE));
        }

        mockMvc.perform(get(BASE_URL).param("page", "0").param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andExpect(jsonPath("$.data.totalElements").value(6))
                .andExpect(jsonPath("$.data.totalPages").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsersWithSortingSuccess() throws Exception {
        userRepository.save(buildUser("aaa@example.com", SIMPLE_PASSWORD, "AAA User", UserRole.USER, true, AccountStatus.ACTIVE));

        mockMvc.perform(get(BASE_URL).param("sortBy", "email").param("sortDirection", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].email").value("aaa@example.com"));
    }

    // -------------------- ACTIVATE / DEACTIVATE --------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateUserSuccess() throws Exception {
        testUser.setEnabled(false);
        testUser.setAccountStatus(AccountStatus.INACTIVE);
        userRepository.save(testUser);

        activate(testUser.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(true))
                .andExpect(jsonPath("$.data.accountStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.data.accountLocked").value(false))
                .andExpect(jsonPath("$.data.failedLoginAttempts").value(0));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateUserNotFoundShouldFail() throws Exception {
        activate(99999L).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateUserSuccess() throws Exception {
        deactivate(testUser.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(false))
                .andExpect(jsonPath("$.data.accountStatus").value("INACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deactivateUserNotFoundShouldFail() throws Exception {
        deactivate(99999L).andExpect(status().isNotFound());
    }

    // -------------------- PASSWORD RESET --------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void resetPasswordSuccess() throws Exception {
        PasswordResetDto dto = new PasswordResetDto();
        dto.setNewPassword(NEW_PASSWORD);

        resetPassword(testUser.getId(), dto)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lastPasswordResetDate").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void resetPasswordShortPasswordShouldFail() throws Exception {
        PasswordResetDto dto = new PasswordResetDto();
        dto.setNewPassword("short");

        resetPassword(testUser.getId(), dto).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void resetPasswordNotFoundShouldFail() throws Exception {
        PasswordResetDto dto = new PasswordResetDto();
        dto.setNewPassword(NEW_PASSWORD);

        resetPassword(99999L, dto).andExpect(status().isNotFound());
    }

    // -------------------- DELETE --------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUserSuccess() throws Exception {
        deleteById(testUser.getId()).andExpect(status().isNoContent());
        getById(testUser.getId()).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUserNotFoundShouldFail() throws Exception {
        deleteById(99999L).andExpect(status().isNotFound());
    }

    // -------------------- STATISTICS --------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void getUserStatisticsSuccess() throws Exception {
        userRepository.save(buildUser("inactive@example.com", SIMPLE_PASSWORD, "Inactive User", UserRole.USER, false, AccountStatus.INACTIVE));

        User locked = buildUser("locked@example.com", SIMPLE_PASSWORD, "Locked User", UserRole.USER, true, AccountStatus.ACTIVE);
        locked.setAccountLocked(true);
        userRepository.save(locked);

        userRepository.save(buildUser("admin@example.com", SIMPLE_PASSWORD, "Admin User", UserRole.ADMIN, true, AccountStatus.ACTIVE));

        mockMvc.perform(get(BASE_URL + "/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalUsers").value(4))
                .andExpect(jsonPath("$.data.activeUsers").value(3))
                .andExpect(jsonPath("$.data.inactiveUsers").value(1))
                .andExpect(jsonPath("$.data.lockedUsers").value(1))
                .andExpect(jsonPath("$.data.adminUsers").value(1))
                .andExpect(jsonPath("$.data.regularUsers").value(3));
    }
}
