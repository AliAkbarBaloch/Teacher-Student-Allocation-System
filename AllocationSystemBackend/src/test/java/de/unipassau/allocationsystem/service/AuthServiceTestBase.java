package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.dto.auth.LoginRequestDto;
import de.unipassau.allocationsystem.dto.auth.PasswordChangeRequestDto;
import de.unipassau.allocationsystem.dto.auth.PasswordForgotRequestDto;
import de.unipassau.allocationsystem.dto.auth.PasswordResetRequestDto;
import de.unipassau.allocationsystem.dto.user.UserProfileUpdateRequest;
import de.unipassau.allocationsystem.entity.PasswordResetToken;
import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.repository.PasswordResetTokenRepository;
import de.unipassau.allocationsystem.repository.UserRepository;
import de.unipassau.allocationsystem.service.audit.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Shared test base for {@link AuthService} unit tests.
 * <p>
 * Provides common mocks, test fixtures, and helper methods to avoid duplication across test classes.
 * </p>
 */
abstract class AuthServiceTestBase {

    @Mock
    protected UserRepository userRepository;

    @Mock
    protected PasswordResetTokenRepository tokenRepository;

    @Mock
    protected org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Mock
    protected JwtService jwtService;

    @Mock
    protected org.springframework.security.authentication.AuthenticationManager authenticationManager;

    @Mock
    protected AuditLogService auditLogService;

    @Mock
    protected EmailService emailService;

    @InjectMocks
    protected AuthService authService;

    protected User user;

    /**
     * Initializes a default enabled user and clears the {@link SecurityContextHolder}.
     */
    @BeforeEach
    void setUpBase() {
        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setPassword("encoded");
        user.setFullName("User Test");
        user.setRole(User.UserRole.USER);
        user.setEnabled(true);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user.setCreatedAt(LocalDateTime.now());

        SecurityContextHolder.clearContext();
        // Default mock behaviours to avoid nulls and simplify individual tests
        when(passwordEncoder.encode(org.mockito.ArgumentMatchers.anyString()))
            .thenAnswer(inv -> "enc-" + inv.getArgument(0));
        when(jwtService.generateToken(org.mockito.ArgumentMatchers.any()))
            .thenReturn("test-jwt-token");
    }

    /**
     * Generates a unique secret value for tests to avoid hard-coded password literals
     * that security analyzers may flag.
     *
     * @return random test secret
     */
    protected static String testSecret() {
        return "test-" + UUID.randomUUID();
    }

    /**
     * Builds a {@link LoginRequestDto}.
     */
    protected LoginRequestDto loginRequest(String email, String password) {
        LoginRequestDto req = new LoginRequestDto();
        req.setEmail(email);
        req.setPassword(password);
        return req;
    }

    /**
     * Builds a {@link PasswordForgotRequestDto}.
     */
    protected PasswordForgotRequestDto forgotRequest(String email) {
        PasswordForgotRequestDto req = new PasswordForgotRequestDto();
        req.setEmail(email);
        return req;
    }

    /**
     * Builds a {@link PasswordResetRequestDto}.
     */
    protected PasswordResetRequestDto resetRequest(String token, String newPassword) {
        PasswordResetRequestDto req = new PasswordResetRequestDto();
        req.setToken(token);
        req.setNewPassword(newPassword);
        return req;
    }

    /**
     * Builds a {@link PasswordChangeRequestDto}.
     */
    protected PasswordChangeRequestDto changeRequest(String currentPassword, String newPassword) {
        PasswordChangeRequestDto req = new PasswordChangeRequestDto();
        req.setCurrentPassword(currentPassword);
        req.setNewPassword(newPassword);
        return req;
    }

    /**
     * Builds a {@link UserProfileUpdateRequest}.
     */
    protected UserProfileUpdateRequest profileRequest(String email, String fullName, String phone) {
        UserProfileUpdateRequest req = new UserProfileUpdateRequest();
        req.setEmail(email);
        req.setFullName(fullName);
        req.setPhoneNumber(phone);
        return req;
    }

    /**
     * Creates Spring Security {@link UserDetails} for the given domain {@link User}.
     */
    protected UserDetails userDetailsFrom(User u) {
        return org.springframework.security.core.userdetails.User
                .withUsername(u.getEmail())
                .password(u.getPassword())
                .roles("USER")
                .build();
    }

    /**
     * Sets the {@link SecurityContextHolder} with an authenticated {@link Authentication} object.
     */
    protected void setAuthenticated(Authentication authentication) {
        SecurityContext sc = mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(sc);
    }

    /**
     * Creates a valid, unused {@link PasswordResetToken} expiring in the future.
     */
    protected PasswordResetToken validResetToken(String tokenValue, User tokenUser) {
        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken(tokenValue);
        prt.setUser(tokenUser);
        prt.setExpiryDate(LocalDateTime.now().plusHours(1));
        prt.setUsed(false);
        return prt;
    }
}
