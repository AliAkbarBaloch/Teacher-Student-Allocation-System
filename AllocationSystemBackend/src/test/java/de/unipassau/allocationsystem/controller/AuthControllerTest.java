package de.unipassau.allocationsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.unipassau.allocationsystem.dto.auth.LoginRequestDto;
import de.unipassau.allocationsystem.dto.auth.PasswordChangeRequestDto;
import de.unipassau.allocationsystem.dto.auth.PasswordForgotRequestDto;
import de.unipassau.allocationsystem.dto.auth.PasswordResetRequestDto;
import de.unipassau.allocationsystem.dto.user.UserProfileUpdateRequest;
import de.unipassau.allocationsystem.entity.PasswordResetToken;
import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.repository.PasswordResetTokenRepository;
import de.unipassau.allocationsystem.repository.UserRepository;
import de.unipassau.allocationsystem.service.EmailService;
import de.unipassau.allocationsystem.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Comprehensive integration tests for the Authentication Controller.
 * Tests all authentication endpoints with success and failure scenarios.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AuthControllerTest {

    private static final String TEST_USER_EMAIL = "testuser@example.com";
    private static final String ANOTHER_USER_EMAIL = "another@example.com";
    private static final String UPDATED_EMAIL = "updated@example.com";

    /**
     * Test-only password fixtures.
     * <p>
     * To avoid “compromised password” rules and “hard-coded password” findings,
     * these values are generated per test run and are not real credentials.
     * </p>
     */
    private static final String VALID_PASSWORD = "TestPwd-" + UUID.randomUUID() + "-Aa1!";
    private static final String NEW_VALID_PASSWORD = "NewPwd-" + UUID.randomUUID() + "-Aa1!";
    private static final String WRONG_PASSWORD = "WrongPwd-" + UUID.randomUUID() + "-Aa1!";
    private static final String SHORT_PASSWORD = "short";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @MockBean
    private EmailService emailService;

    private User testUser;

    @Autowired
    AuthControllerTest(
            @Autowired
            MockMvc mockMvc,
            ObjectMapper objectMapper,
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @BeforeEach
    void setUp() {
        passwordResetTokenRepository.deleteAll();

        testUser = findOrCreateActiveUser(TEST_USER_EMAIL);
        ensureUserHasPasswordAndIsActive(testUser, VALID_PASSWORD);

        reset(emailService);
    }

    private User findOrCreateActiveUser(String email) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setEmail(email);
            u.setFullName("Test User");
            u.setRole(User.UserRole.USER);
            u.setEnabled(true);
            u.setAccountLocked(false);
            u.setAccountStatus(User.AccountStatus.ACTIVE);
            u.setPassword(passwordEncoder.encode(VALID_PASSWORD));
            return userRepository.save(u);
        });
    }

    private void ensureUserHasPasswordAndIsActive(User user, String rawPassword) {
        boolean changed = false;

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            changed = true;
        }

        if (!user.isEnabled() || !user.isAccountNonLocked() || user.getAccountStatus() != User.AccountStatus.ACTIVE) {
            user.setEnabled(true);
            user.setAccountLocked(false);
            user.setAccountStatus(User.AccountStatus.ACTIVE);
            changed = true;
        }

        if (changed) {
            testUser = userRepository.save(user);
        }
    }

    // ==================== LOGIN TESTS ====================

    @Test
    void testLoginSuccess() throws Exception {
        LoginRequestDto login = loginRequest(testUser.getEmail(), VALID_PASSWORD);

        performJsonPost("/api/auth/login", login)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.data.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.data.fullName").value(testUser.getFullName()))
                .andExpect(jsonPath("$.data.role").value(testUser.getRole().toString()));

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assert updatedUser.getFailedLoginAttempts() == 0;
        assert updatedUser.getLastLoginDate() != null;
    }

    @Test
    void testLoginWithInvalidPassword() throws Exception {
        LoginRequestDto login = loginRequest(testUser.getEmail(), WRONG_PASSWORD);

        performJsonPost("/api/auth/login", login)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assert updatedUser.getFailedLoginAttempts() == 1;
    }

    @Test
    void testLoginWithNonExistentEmail() throws Exception {
        LoginRequestDto login = loginRequest("nonexistent@example.com", VALID_PASSWORD);

        performJsonPost("/api/auth/login", login)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void testAccountLockoutAfterFailedAttempts() throws Exception {
        LoginRequestDto login = loginRequest(testUser.getEmail(), WRONG_PASSWORD);

        for (int i = 0; i < 5; i++) {
            performJsonPost("/api/auth/login", login)
                    .andExpect(status().isUnauthorized());
        }

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assert updatedUser.isAccountLocked();
        assert updatedUser.getFailedLoginAttempts() == 5;

        verify(emailService, times(1)).sendAccountLockedEmail(
                org.mockito.ArgumentMatchers.eq(testUser.getEmail()),
                org.mockito.ArgumentMatchers.eq(testUser.getFullName())
        );

        login.setPassword(VALID_PASSWORD);

        performJsonPost("/api/auth/login", login)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value("Account is locked. Please contact administrator or reset your password."));
    }

    @Test
    void testLoginWithDisabledAccount() throws Exception {
        testUser.setEnabled(false);
        userRepository.save(testUser);

        LoginRequestDto login = loginRequest(testUser.getEmail(), VALID_PASSWORD);

        performJsonPost("/api/auth/login", login)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Account is disabled"));
    }

    @Test
    void testLoginValidationErrors() throws Exception {
        LoginRequestDto emptyEmail = loginRequest("", VALID_PASSWORD);
        performJsonPost("/api/auth/login", emptyEmail)
                .andExpect(status().isBadRequest());

        LoginRequestDto invalidEmail = loginRequest("notanemail", VALID_PASSWORD);
        performJsonPost("/api/auth/login", invalidEmail)
                .andExpect(status().isBadRequest());

        LoginRequestDto emptyPassword = loginRequest("test@example.com", "");
        performJsonPost("/api/auth/login", emptyPassword)
                .andExpect(status().isBadRequest());
    }

    // ==================== LOGOUT TESTS ====================

    @Test
    void testLogoutSuccess() throws Exception {
        String token = jwtService.generateToken(testUser);

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }

    @Test
    void testLogoutWithoutAuthentication() throws Exception {
        // Logout endpoint allows anonymous access as per security configuration
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logout successful"));
    }

    // ==================== FORGOT PASSWORD TESTS ====================

    @Test
    void testForgotPasswordSuccess() throws Exception {
        PasswordForgotRequestDto request = new PasswordForgotRequestDto();
        request.setEmail(testUser.getEmail());

        performJsonPost("/api/auth/forgot-password", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("If the email exists, a password reset link has been sent."));

        verify(emailService, times(1)).sendPasswordResetEmail(
                org.mockito.ArgumentMatchers.eq(testUser.getEmail()),
                anyString(),
                org.mockito.ArgumentMatchers.eq(testUser.getFullName())
        );

        var tokens = passwordResetTokenRepository.findByUser(testUser);
        assert !tokens.isEmpty();
    }

    @Test
    void testForgotPasswordWithNonExistentEmail() throws Exception {
        PasswordForgotRequestDto request = new PasswordForgotRequestDto();
        request.setEmail("nonexistent@example.com");

        performJsonPost("/api/auth/forgot-password", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("If the email exists, a password reset link has been sent."));

        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testForgotPasswordValidationError() throws Exception {
        PasswordForgotRequestDto request = new PasswordForgotRequestDto();
        request.setEmail("invalid-email");

        performJsonPost("/api/auth/forgot-password", request)
                .andExpect(status().isBadRequest());
    }

    // ==================== RESET PASSWORD TESTS ====================

    @Test
    void testResetPasswordSuccess() throws Exception {
        String token = UUID.randomUUID().toString();
        createResetToken(token, LocalDateTime.now().plusHours(24), false);

        lockUserForResetScenario(testUser);

        PasswordResetRequestDto request = new PasswordResetRequestDto();
        request.setToken(token);
        request.setNewPassword(NEW_VALID_PASSWORD);

        performJsonPost("/api/auth/reset-password", request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password has been reset successfully"));

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assert passwordEncoder.matches(NEW_VALID_PASSWORD, updatedUser.getPassword());

        assert !updatedUser.isAccountLocked();
        assert updatedUser.getFailedLoginAttempts() == 0;

        PasswordResetToken usedToken = passwordResetTokenRepository.findByToken(token).orElseThrow();
        assert usedToken.isUsed();
    }

    @Test
    void testResetPasswordWithInvalidToken() throws Exception {
        PasswordResetRequestDto request = new PasswordResetRequestDto();
        request.setToken("invalid-token-" + UUID.randomUUID());
        request.setNewPassword(NEW_VALID_PASSWORD);

        performJsonPost("/api/auth/reset-password", request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired password reset token"));
    }

    @Test
    void testResetPasswordWithExpiredToken() throws Exception {
        String token = UUID.randomUUID().toString();
        createResetToken(token, LocalDateTime.now().minusHours(1), false);

        PasswordResetRequestDto request = new PasswordResetRequestDto();
        request.setToken(token);
        request.setNewPassword(NEW_VALID_PASSWORD);

        performJsonPost("/api/auth/reset-password", request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired password reset token"));
    }

    @Test
    void testResetPasswordWithUsedToken() throws Exception {
        String token = UUID.randomUUID().toString();
        createResetToken(token, LocalDateTime.now().plusHours(24), true);

        PasswordResetRequestDto request = new PasswordResetRequestDto();
        request.setToken(token);
        request.setNewPassword(NEW_VALID_PASSWORD);

        performJsonPost("/api/auth/reset-password", request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired password reset token"));
    }

    @Test
    void testResetPasswordValidationError() throws Exception {
        String token = UUID.randomUUID().toString();
        createResetToken(token, LocalDateTime.now().plusHours(24), false);

        PasswordResetRequestDto request = new PasswordResetRequestDto();
        request.setToken(token);
        request.setNewPassword(SHORT_PASSWORD);

        performJsonPost("/api/auth/reset-password", request)
                .andExpect(status().isBadRequest());
    }

    // ==================== CHANGE PASSWORD TESTS ====================

    @Test
    void testChangePasswordSuccess() throws Exception {
        String token = jwtService.generateToken(testUser);

        PasswordChangeRequestDto request = new PasswordChangeRequestDto();
        request.setCurrentPassword(VALID_PASSWORD);
        request.setNewPassword(NEW_VALID_PASSWORD);

        performJsonPostWithAuth("/api/auth/change-password", token, request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assert passwordEncoder.matches(NEW_VALID_PASSWORD, updatedUser.getPassword());
        assert updatedUser.getLastPasswordResetDate() != null;
    }

    @Test
    void testChangePasswordWithIncorrectCurrentPassword() throws Exception {
        String token = jwtService.generateToken(testUser);

        PasswordChangeRequestDto request = new PasswordChangeRequestDto();
        request.setCurrentPassword(WRONG_PASSWORD);
        request.setNewPassword(NEW_VALID_PASSWORD);

        performJsonPostWithAuth("/api/auth/change-password", token, request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Current password is incorrect"));
    }

    @Test
    void testChangePasswordWithoutAuthentication() throws Exception {
        PasswordChangeRequestDto request = new PasswordChangeRequestDto();
        request.setCurrentPassword(VALID_PASSWORD);
        request.setNewPassword(NEW_VALID_PASSWORD);

        performJsonPost("/api/auth/change-password", request)
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testChangePasswordValidationError() throws Exception {
        String token = jwtService.generateToken(testUser);

        PasswordChangeRequestDto request = new PasswordChangeRequestDto();
        request.setCurrentPassword(VALID_PASSWORD);
        request.setNewPassword(SHORT_PASSWORD);

        performJsonPostWithAuth("/api/auth/change-password", token, request)
                .andExpect(status().isBadRequest());
    }

    // ==================== PROFILE TESTS ====================

    @Test
    void testGetProfileSuccess() throws Exception {
        String token = jwtService.generateToken(testUser);

        mockMvc.perform(get("/api/auth/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testUser.getId()))
                .andExpect(jsonPath("$.data.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.data.fullName").value(testUser.getFullName()))
                .andExpect(jsonPath("$.data.phoneNumber").value(testUser.getPhoneNumber()))
                .andExpect(jsonPath("$.data.role").value(testUser.getRole().toString()));
    }

    @Test
    void testGetProfileWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/auth/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateProfileSuccess() throws Exception {
        String token = jwtService.generateToken(testUser);

        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setFullName("Updated Name");
        request.setEmail(UPDATED_EMAIL);
        request.setPhoneNumber("9876543210");

        performJsonPutWithAuth("/api/auth/profile", token, request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testUser.getId()))
                .andExpect(jsonPath("$.data.email").value(UPDATED_EMAIL))
                .andExpect(jsonPath("$.data.fullName").value("Updated Name"))
                .andExpect(jsonPath("$.data.phoneNumber").value("9876543210"));

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assert UPDATED_EMAIL.equals(updatedUser.getEmail());
        assert "Updated Name".equals(updatedUser.getFullName());
        assert "9876543210".equals(updatedUser.getPhoneNumber());
    }

    @Test
    void testUpdateProfileWithDuplicateEmail() throws Exception {
        ensureAnotherUserExists(ANOTHER_USER_EMAIL);

        String token = jwtService.generateToken(testUser);

        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setFullName("Updated Name");
        request.setEmail(ANOTHER_USER_EMAIL);
        request.setPhoneNumber("9876543210");

        performJsonPutWithAuth("/api/auth/profile", token, request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email is already in use"));
    }

    @Test
    void testUpdateProfileWithoutAuthentication() throws Exception {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setFullName("Updated Name");
        request.setEmail(UPDATED_EMAIL);

        mockMvc.perform(put("/api/auth/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateProfileValidationError() throws Exception {
        String token = jwtService.generateToken(testUser);

        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setFullName("Updated Name");
        request.setEmail("invalid-email");

        performJsonPutWithAuth("/api/auth/profile", token, request)
                .andExpect(status().isBadRequest());
    }

    // ==================== JWT FILTER TESTS ====================

    @Test
    void testAccessProtectedEndpointWithValidToken() throws Exception {
        String token = jwtService.generateToken(testUser);

        mockMvc.perform(get("/api/auth/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void testAccessProtectedEndpointWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/auth/profile")
                        .header("Authorization", "Bearer invalid-token-" + UUID.randomUUID()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void testAccessProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/auth/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testAccessProtectedEndpointWithMalformedAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/auth/profile")
                        .header("Authorization", "NotBearer token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testPublicEndpointsAccessibleWithoutToken() throws Exception {
        LoginRequestDto login = loginRequest(testUser.getEmail(), VALID_PASSWORD);

        performJsonPost("/api/auth/login", login)
                .andExpect(status().isOk());

        PasswordForgotRequestDto forgot = new PasswordForgotRequestDto();
        forgot.setEmail(testUser.getEmail());

        performJsonPost("/api/auth/forgot-password", forgot)
                .andExpect(status().isOk());
    }

    private void ensureAnotherUserExists(String email) {
        userRepository.findByEmail(email).orElseGet(() -> {
            User another = new User();
            another.setEmail(email);
            another.setPassword(passwordEncoder.encode(VALID_PASSWORD));
            another.setFullName("Another User");
            another.setRole(User.UserRole.USER);
            another.setEnabled(true);
            return userRepository.save(another);
        });
    }

    private void lockUserForResetScenario(User user) {
        user.setAccountLocked(true);
        user.setFailedLoginAttempts(5);
        userRepository.save(user);
    }

    private void createResetToken(String token, LocalDateTime expiryDate, boolean used) {
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(testUser);
        resetToken.setExpiryDate(expiryDate);
        resetToken.setUsed(used);
        passwordResetTokenRepository.save(resetToken);
    }

    private LoginRequestDto loginRequest(String email, String password) {
        LoginRequestDto dto = new LoginRequestDto();
        dto.setEmail(email);
        dto.setPassword(password);
        return dto;
    }

    private ResultActions performJsonPost(String url, Object body) throws Exception {
        return mockMvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private ResultActions performJsonPostWithAuth(String url, String token, Object body) throws Exception {
        return mockMvc.perform(post(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private ResultActions performJsonPutWithAuth(String url, String token, Object body) throws Exception {
        return mockMvc.perform(put(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }
}
