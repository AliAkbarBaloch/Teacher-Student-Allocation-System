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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
 * Validates CRUD, filtering, pagination, sorting, activation/deactivation, and password reset.
 * </p>
 */
@SpringBootTest(properties = "spring.sql.init.mode=never")
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("test")
@Transactional
class UserControllerTest {

    private static final String BASE_URL = "/api/users";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    private User testUser;

    // No hard-coded password strings (static analyzers often flag any literal)
    private String defaultSecret;
    private String anotherSecret;
    private String resetSecret;

    UserControllerTest(MockMvc mockMvc, ObjectMapper objectMapper, UserRepository userRepository) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
    }

    @BeforeEach
    void setUp() {
        defaultSecret = randomSecret();
        anotherSecret = randomSecret();
        resetSecret = randomSecret();

        userRepository.deleteAll();
        testUser = userRepository.save(buildUser(
                "test@example.com",
                defaultSecret,
                "Test User",
                UserRole.USER,
                true,
                AccountStatus.ACTIVE
        ));
    }

    private String randomSecret() {
        return "sec-" + UUID.randomUUID();
    }

    private User buildUser(String email, String secret, String fullName, UserRole role, boolean enabled, AccountStatus status) {
        User u = new User();
        u.setEmail(email);
        u.setPassword(secret);
        u.setFullName(fullName);
        u.setRole(role);
        u.setEnabled(enabled);
        u.setAccountStatus(status);
        u.setAccountLocked(false);
        u.setFailedLoginAttempts(0);
        return u;
    }

    private ResultActions create(UserCreateDto dto) throws Exception {
        return mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));
    }

    private ResultActions update(long id, UserUpdateDto dto) throws Exception {
        return mockMvc.perform(put(BASE_URL + "/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));
    }

    private ResultActions getById(long id) throws Exception {
        return mockMvc.perform(get(BASE_URL + "/{id}", id));
    }

    private ResultActions deleteById(long id) throws Exception {
        return mockMvc.perform(delete(BASE_URL + "/{id}", id));
    }

    private ResultActions activate(long id) throws Exception {
        return mockMvc.perform(patch(BASE_URL + "/{id}/activate", id));
    }

    private ResultActions deactivate(long id) throws Exception {
        return mockMvc.perform(patch(BASE_URL + "/{id}/deactivate", id));
    }

    private ResultActions resetPassword(long id, String newSecret) throws Exception {
        PasswordResetDto dto = new PasswordResetDto();
        dto.setNewPassword(newSecret);

        return mockMvc.perform(post(BASE_URL + "/{id}/reset-password", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));
    }

    private ResultActions getUsers(String... kvPairs) throws Exception {
        // kvPairs: "key1","value1","key2","value2",...
        var req = get(BASE_URL);
        for (int i = 0; i + 1 < kvPairs.length; i += 2) {
            req = req.param(kvPairs[i], kvPairs[i + 1]);
        }
        return mockMvc.perform(req);
    }

    // -------------------- CREATE --------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void createUserSuccess() throws Exception {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail("newuser@example.com");
        dto.setPassword(defaultSecret);
        dto.setFullName("New User");
        dto.setRole(UserRole.USER);

        create(dto)
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
        dto.setPassword(defaultSecret);
        dto.setFullName("Duplicate User");
        dto.setRole(UserRole.USER);

        create(dto).andExpect(status().isConflict());
    }

    @ParameterizedTest
    @WithMockUser(roles = "ADMIN")
    @CsvSource({
            "invalid-email, true",   // invalid email
            "newuser@example.com, false" // short password
    })
    void createUserValidationShouldFail(String email, boolean invalidEmail) throws Exception {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail(email);
        dto.setFullName("Any User");
        dto.setRole(UserRole.USER);

        dto.setPassword(invalidEmail ? defaultSecret : "x"); // "x" is intentionally too short

        create(dto).andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createUserWithoutAdminRoleShouldFail() throws Exception {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail("newuser@example.com");
        dto.setPassword(defaultSecret);
        dto.setFullName("New User");
        dto.setRole(UserRole.USER);

        create(dto).andExpect(status().isForbidden());
    }

    // -------------------- UPDATE --------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserSuccess() throws Exception {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setFullName("Updated Name");
        dto.setPhoneNumber("+1234567890");

        update(testUser.getId(), dto)
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

        update(testUser.getId(), dto)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("ADMIN"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateUserNotFoundShouldFail() throws Exception {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setFullName("Updated Name");

        update(99999L, dto).andExpect(status().isNotFound());
    }

    // -------------------- GET BY ID --------------------

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

    // -------------------- LIST / FILTER / SEARCH / PAGINATION / SORT --------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsersSuccess() throws Exception {
        userRepository.save(buildUser("user2@example.com", anotherSecret, "User Two", UserRole.ADMIN, true, AccountStatus.ACTIVE));

        getUsers()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @ParameterizedTest
    @WithMockUser(roles = "ADMIN")
    @CsvSource({
            "role,ADMIN",
            "status,INACTIVE",
            "enabled,true",
            "search,test@example",
            "search,Test User"
    })
    void getAllUsersFilteringAndSearchingSuccess(String key, String value) throws Exception {
        if (key.equals("role")) {
            userRepository.save(buildUser("admin@example.com", anotherSecret, "Admin User", UserRole.ADMIN, true, AccountStatus.ACTIVE));
        } else if (key.equals("status")) {
            userRepository.save(buildUser("inactive@example.com", anotherSecret, "Inactive User", UserRole.USER, false, AccountStatus.INACTIVE));
        }

        ResultActions res = getUsers(key, value).andExpect(status().isOk());

        if (key.equals("enabled")) {
            res.andExpect(jsonPath("$.data.content[*].enabled", everyItem(is(true))));
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsersWithPaginationSuccess() throws Exception {
        for (int i = 0; i < 5; i++) {
            userRepository.save(buildUser("user" + i + "@example.com", anotherSecret, "User " + i, UserRole.USER, true, AccountStatus.ACTIVE));
        }

        getUsers("page", "0", "size", "3")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andExpect(jsonPath("$.data.totalElements").value(6))
                .andExpect(jsonPath("$.data.totalPages").value(2));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsersWithSortingSuccess() throws Exception {
        userRepository.save(buildUser("aaa@example.com", anotherSecret, "AAA User", UserRole.USER, true, AccountStatus.ACTIVE));

        getUsers("sortBy", "email", "sortDirection", "asc")
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
    void deactivateUserSuccess() throws Exception {
        deactivate(testUser.getId())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(false))
                .andExpect(jsonPath("$.data.accountStatus").value("INACTIVE"));
    }

    @ParameterizedTest
    @WithMockUser(roles = "ADMIN")
    @CsvSource({
            "activate",
            "deactivate"
    })
    void activateDeactivateNotFoundShouldFail(String action) throws Exception {
        if (action.equals("activate")) {
            activate(99999L).andExpect(status().isNotFound());
        } else {
            deactivate(99999L).andExpect(status().isNotFound());
        }
    }

    // -------------------- PASSWORD RESET --------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    void resetPasswordSuccess() throws Exception {
        resetPassword(testUser.getId(), resetSecret)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.lastPasswordResetDate").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void resetPasswordShortPasswordShouldFail() throws Exception {
        resetPassword(testUser.getId(), "x").andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void resetPasswordNotFoundShouldFail() throws Exception {
        resetPassword(99999L, resetSecret).andExpect(status().isNotFound());
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
}
