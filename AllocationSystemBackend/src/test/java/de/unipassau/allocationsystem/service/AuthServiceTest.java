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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthService}.
 * <p>
 * This test class validates authentication, password reset, password change,
 * and user profile update operations.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
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
    }

    @Test
    void login_Success() {
        // Arrange
        LoginRequestDto req = new LoginRequestDto();
        req.setEmail(user.getEmail());
        req.setPassword("plain");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles("USER")
                .build();

        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("token-xyz");

        // Act
        var resp = authService.login(req);

        // Assert
        assertNotNull(resp);
        assertEquals(user.getEmail(), resp.getEmail());
        assertEquals(user.getId(), resp.getUserId());
        verify(userRepository).save(any(User.class));
        verify(auditLogService).logAction(any(), anyString(), anyString());
    }

    @Test
    void login_BadCredentials_IncrementsAttemptsAndThrows() {
        // Arrange
        LoginRequestDto req = new LoginRequestDto();
        req.setEmail(user.getEmail());
        req.setPassword("wrong");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        // Act & Assert
        assertThrows(BadCredentialsException.class, () -> authService.login(req));
        verify(userRepository).save(any(User.class));
        verify(auditLogService).logAction(any(), anyString(), anyString());
    }

    @Test
    void forgotPassword_NonExistent_DoesNothing() {
        PasswordForgotRequestDto req = new PasswordForgotRequestDto();
        req.setEmail("nope@example.com");

        when(userRepository.findByEmail("nope@example.com")).thenReturn(Optional.empty());

        authService.forgotPassword(req);

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    @Test
    void resetPassword_Success() {
        // Arrange
        String token = "tok-123";
        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken(token);
        prt.setUser(user);
        prt.setExpiryDate(LocalDateTime.now().plusHours(1));
        prt.setUsed(false);

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(prt));
        when(passwordEncoder.encode("newpass")).thenReturn("enc-new");

        PasswordResetRequestDto req = new PasswordResetRequestDto();
        req.setToken(token);
        req.setNewPassword("newpass");

        // Act
        authService.resetPassword(req);

        // Assert
        verify(userRepository).save(any(User.class));
        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(auditLogService).logAction(any(), anyString(), anyString());
    }

    @Test
    void resetPassword_InvalidToken_Throws() {
        when(tokenRepository.findByToken("bad")).thenReturn(Optional.empty());
        PasswordResetRequestDto req = new PasswordResetRequestDto();
        req.setToken("bad");
        req.setNewPassword("new");

        assertThrows(IllegalArgumentException.class, () -> authService.resetPassword(req));
    }

    @Test
    void changePassword_Success() {
        // Setup SecurityContext with authenticated principal
        UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles("USER")
                .build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContext sc = mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(sc);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", user.getPassword())).thenReturn(true);

        PasswordChangeRequestDto req = new PasswordChangeRequestDto();
        req.setCurrentPassword("old");
        req.setNewPassword("newpass");

        authService.changePassword(req);

        verify(userRepository).save(any(User.class));
        verify(auditLogService).logAction(any(), anyString(), anyString());
    }

    @Test
    void changePassword_IncorrectCurrent_Throws() {
        UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles("USER")
                .build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContext sc = mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(sc);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", user.getPassword())).thenReturn(false);

        PasswordChangeRequestDto req = new PasswordChangeRequestDto();
        req.setCurrentPassword("wrong");
        req.setNewPassword("newpass");

        assertThrows(IllegalArgumentException.class, () -> authService.changePassword(req));
    }

    @Test
    void updateProfile_Success() {
        UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles("USER")
                .build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContext sc = mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(sc);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserProfileUpdateRequest req = new UserProfileUpdateRequest();
        req.setEmail("new@example.com");
        req.setFullName("New Name");
        req.setPhoneNumber("123");

        var dto = authService.updateProfile(req);

        assertNotNull(dto);
        verify(userRepository).save(any(User.class));
        verify(auditLogService).logAction(any(), anyString(), anyString());
    }

    @Test
    void updateProfile_DuplicateEmail_Throws() {
        UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles("USER")
                .build();
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContext sc = mock(SecurityContext.class);
        when(sc.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(sc);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(new User()));

        UserProfileUpdateRequest req = new UserProfileUpdateRequest();
        req.setEmail("taken@example.com");
        req.setFullName("Name");

        assertThrows(IllegalArgumentException.class, () -> authService.updateProfile(req));
    }
}
