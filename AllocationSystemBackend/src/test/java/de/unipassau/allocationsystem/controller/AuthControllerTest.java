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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.context.ActiveProfiles;

/**
 * Comprehensive integration tests for the Authentication Controller.
 * Tests all authentication endpoints with success and failure scenarios.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private EmailService emailService;

    private User testUser;
    private String testUserPassword = "password123";

    @BeforeEach
    void setUp() {
        // Clean up tokens and other non-user state (avoid deleting users because seeded data references them)
        passwordResetTokenRepository.deleteAll();

        String email = "testuser@example.com";
        String rawPassword = testUserPassword;

        // Find existing user or create
        testUser = userRepository.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setEmail(email);
            u.setFullName("Test User");
            u.setRole(User.UserRole.USER);
            u.setEnabled(true);
            u.setAccountLocked(false);
            u.setAccountStatus(User.AccountStatus.ACTIVE);
            u.setPassword(passwordEncoder.encode(rawPassword));
            return userRepository.save(u);
        });

        // Ensure stored password matches the expected raw password and user is active
        boolean changed = false;
        if (!passwordEncoder.matches(rawPassword, testUser.getPassword())) {
            testUser.setPassword(passwordEncoder.encode(rawPassword));
            changed = true;
        }
        if (!testUser.isEnabled() || testUser.isAccountNonLocked() == false || testUser.getAccountStatus() != User.AccountStatus.ACTIVE) {
            testUser.setEnabled(true);
            testUser.setAccountLocked(false);
            testUser.setAccountStatus(User.AccountStatus.ACTIVE);
            changed = true;
        }
        if (changed) {
            testUser = userRepository.save(testUser);
        }

        // Reset mocks
        reset(emailService);
    }


    // ==================== LOGIN TESTS ====================

    @Test
    void testLoginSuccess() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail(testUser.getEmail());
        loginRequestDto.setPassword(testUserPassword);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto))).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.data.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.data.fullName").value(testUser.getFullName()))
                .andExpect(jsonPath("$.data.role").value(testUser.getRole().toString()));

        // Verify failed login attempts reset
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assert updatedUser.getFailedLoginAttempts() == 0;
        assert updatedUser.getLastLoginDate() != null;
    }

    @Test
    void testLoginWithInvalidPassword() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail(testUser.getEmail());
        loginRequestDto.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));

        // Verify failed login attempts incremented
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assert updatedUser.getFailedLoginAttempts() == 1;
    }

    @Test
    void testLoginWithNonExistentEmail() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("nonexistent@example.com");
        loginRequestDto.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void testAccountLockoutAfterFailedAttempts() throws Exception {
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail(testUser.getEmail());
        loginRequestDto.setPassword("wrongpassword");

        // Attempt login 5 times with wrong password
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginRequestDto)))
                    .andExpect(status().isUnauthorized());
        }

        // Verify account is locked
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assert updatedUser.isAccountLocked();
        assert updatedUser.getFailedLoginAttempts() == 5;

        // Verify lockout email was sent
        verify(emailService, times(1)).sendAccountLockedEmail(
                eq(testUser.getEmail()),
                eq(testUser.getFullName())
        );

        // Try login with correct password - should still fail due to lockout
        loginRequestDto.setPassword(testUserPassword);
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Account is locked. Please contact administrator or reset your password."));
    }

    @Test
    void testLoginWithDisabledAccount() throws Exception {
        testUser.setEnabled(false);
        userRepository.save(testUser);

        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail(testUser.getEmail());
        loginRequestDto.setPassword(testUserPassword);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Account is disabled"));
    }

    @Test
    void testLoginValidationErrors() throws Exception {
        // Test empty email
        LoginRequestDto emptyEmail = new LoginRequestDto();
        emptyEmail.setEmail("");
        emptyEmail.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyEmail)))
                .andExpect(status().isBadRequest());

        // Test invalid email format
        LoginRequestDto invalidEmail = new LoginRequestDto();
        invalidEmail.setEmail("notanemail");
        invalidEmail.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmail)))
                .andExpect(status().isBadRequest());

        // Test empty password
        LoginRequestDto emptyPassword = new LoginRequestDto();
        emptyPassword.setEmail("test@example.com");
        emptyPassword.setPassword("");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyPassword)))
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
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== FORGOT PASSWORD TESTS ====================

    @Test
    void testForgotPasswordSuccess() throws Exception {
        PasswordForgotRequestDto request = new PasswordForgotRequestDto();
        request.setEmail(testUser.getEmail());

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If the email exists, a password reset link has been sent."));

        // Verify email was sent
        verify(emailService, times(1)).sendPasswordResetEmail(
                eq(testUser.getEmail()),
                anyString(),
                eq(testUser.getFullName())
        );

        // Verify token was created
        var tokens = passwordResetTokenRepository.findByUser(testUser);
        assert !tokens.isEmpty();
    }

    @Test
    void testForgotPasswordWithNonExistentEmail() throws Exception {
        PasswordForgotRequestDto request = new PasswordForgotRequestDto();
        request.setEmail("nonexistent@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("If the email exists, a password reset link has been sent."));

        // Verify no email was sent
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testForgotPasswordValidationError() throws Exception {
        PasswordForgotRequestDto request = new PasswordForgotRequestDto();
        request.setEmail("invalid-email");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== RESET PASSWORD TESTS ====================

    @Test
    void testResetPasswordSuccess() throws Exception {
        // Create password reset token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(testUser);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        resetToken.setUsed(false);
        passwordResetTokenRepository.save(resetToken);

        // Lock the account to test unlock functionality
        testUser.setAccountLocked(true);
        testUser.setFailedLoginAttempts(5);
        userRepository.save(testUser);

        PasswordResetRequestDto request = new PasswordResetRequestDto();
        request.setToken(token);
        request.setNewPassword("newpassword123");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password has been reset successfully"));

        // Verify password was changed
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assert passwordEncoder.matches("newpassword123", updatedUser.getPassword());

        // Verify account was unlocked
        assert !updatedUser.isAccountLocked();
        assert updatedUser.getFailedLoginAttempts() == 0;

        // Verify token was marked as used
        PasswordResetToken usedToken = passwordResetTokenRepository.findByToken(token).orElseThrow();
        assert usedToken.isUsed();
    }

    @Test
    void testResetPasswordWithInvalidToken() throws Exception {
        PasswordResetRequestDto request = new PasswordResetRequestDto();
        request.setToken("invalid-token");
        request.setNewPassword("newpassword123");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired password reset token"));
    }

    @Test
    void testResetPasswordWithExpiredToken() throws Exception {
        // Create expired token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(testUser);
        resetToken.setExpiryDate(LocalDateTime.now().minusHours(1)); // Expired
        resetToken.setUsed(false);
        passwordResetTokenRepository.save(resetToken);

        PasswordResetRequestDto request = new PasswordResetRequestDto();
        request.setToken(token);
        request.setNewPassword("newpassword123");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired password reset token"));
    }

    @Test
    void testResetPasswordWithUsedToken() throws Exception {
        // Create used token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(testUser);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        resetToken.setUsed(true); // Already used
        passwordResetTokenRepository.save(resetToken);

        PasswordResetRequestDto request = new PasswordResetRequestDto();
        request.setToken(token);
        request.setNewPassword("newpassword123");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired password reset token"));
    }

    @Test
    void testResetPasswordValidationError() throws Exception {
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(testUser);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        resetToken.setUsed(false);
        passwordResetTokenRepository.save(resetToken);

        // Password too short
        PasswordResetRequestDto request = new PasswordResetRequestDto();
        request.setToken(token);
        request.setNewPassword("short");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== CHANGE PASSWORD TESTS ====================

    @Test
    void testChangePasswordSuccess() throws Exception {
        String token = jwtService.generateToken(testUser);

        PasswordChangeRequestDto request = new PasswordChangeRequestDto();
        request.setCurrentPassword(testUserPassword);
        request.setNewPassword("newpassword123");

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));

        // Verify password was changed
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assert passwordEncoder.matches("newpassword123", updatedUser.getPassword());
        assert updatedUser.getLastPasswordResetDate() != null;
    }

    @Test
    void testChangePasswordWithIncorrectCurrentPassword() throws Exception {
        String token = jwtService.generateToken(testUser);

        PasswordChangeRequestDto request = new PasswordChangeRequestDto();
        request.setCurrentPassword("wrongpassword");
        request.setNewPassword("newpassword123");

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Current password is incorrect"));
    }

    @Test
    void testChangePasswordWithoutAuthentication() throws Exception {
        PasswordChangeRequestDto request = new PasswordChangeRequestDto();
        request.setCurrentPassword(testUserPassword);
        request.setNewPassword("newpassword123");

        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testChangePasswordValidationError() throws Exception {
        String token = jwtService.generateToken(testUser);

        // New password too short
        PasswordChangeRequestDto request = new PasswordChangeRequestDto();
        request.setCurrentPassword(testUserPassword);
        request.setNewPassword("short");

        mockMvc.perform(post("/api/auth/change-password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
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
        request.setEmail("updated@example.com");
        request.setPhoneNumber("9876543210");

        mockMvc.perform(put("/api/auth/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(testUser.getId()))
                .andExpect(jsonPath("$.data.email").value("updated@example.com"))
                .andExpect(jsonPath("$.data.fullName").value("Updated Name"))
                .andExpect(jsonPath("$.data.phoneNumber").value("9876543210"));

        // Verify changes in database
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assert updatedUser.getEmail().equals("updated@example.com");
        assert updatedUser.getFullName().equals("Updated Name");
        assert updatedUser.getPhoneNumber().equals("9876543210");
    }

    @Test
    void testUpdateProfileWithDuplicateEmail() throws Exception {
        // Create another user
        User anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword(passwordEncoder.encode("password123"));
        anotherUser.setFullName("Another User");
        anotherUser.setRole(User.UserRole.USER);
        anotherUser.setEnabled(true);
        userRepository.save(anotherUser);

        String token = jwtService.generateToken(testUser);

        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setFullName("Updated Name");
        request.setEmail("another@example.com"); // Duplicate email
        request.setPhoneNumber("9876543210");

        mockMvc.perform(put("/api/auth/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email is already in use"));
    }

    @Test
    void testUpdateProfileWithoutAuthentication() throws Exception {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setFullName("Updated Name");
        request.setEmail("updated@example.com");

        mockMvc.perform(put("/api/auth/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateProfileValidationError() throws Exception {
        String token = jwtService.generateToken(testUser);

        // Invalid email format
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setFullName("Updated Name");
        request.setEmail("invalid-email");

        mockMvc.perform(put("/api/auth/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
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
                        .header("Authorization", "Bearer invalid-token"))
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
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail(testUser.getEmail());
        loginRequestDto.setPassword(testUserPassword);

        // Login endpoint should be accessible
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequestDto)))
                .andExpect(status().isOk());

        // Forgot password endpoint should be accessible
        PasswordForgotRequestDto passwordForgotRequestDto = new PasswordForgotRequestDto();
        passwordForgotRequestDto.setEmail(testUser.getEmail());

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordForgotRequestDto)))
                .andExpect(status().isOk());
    }
}
