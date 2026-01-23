package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.dto.auth.LoginRequestDto;
import de.unipassau.allocationsystem.dto.auth.LoginResponseDto;
import de.unipassau.allocationsystem.dto.auth.PasswordChangeRequestDto;
import de.unipassau.allocationsystem.dto.auth.PasswordForgotRequestDto;
import de.unipassau.allocationsystem.dto.auth.PasswordResetRequestDto;
import de.unipassau.allocationsystem.dto.user.UserProfileUpdateRequest;
import de.unipassau.allocationsystem.dto.user.UserResponseDto;
import de.unipassau.allocationsystem.entity.AuditLog;
import de.unipassau.allocationsystem.entity.PasswordResetToken;
import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.PasswordResetTokenRepository;
import de.unipassau.allocationsystem.repository.UserRepository;
import de.unipassau.allocationsystem.service.audit.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for authentication operations: login, logout, password management, and profile updates.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditLogService auditLogService;
    private final EmailService emailService;

    /** Authenticates the user and returns a JWT-based {@link LoginResponseDto}. */
    public LoginResponseDto login(LoginRequestDto request) {
        User user = requireUserByEmail(request.getEmail());
        assertAccountNotLocked(user);

        try {
            Authentication authentication = authenticate(request);
            setAuthentication(authentication);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);

            onSuccessfulLogin(user);
            logLoginSuccess(user);

            return buildLoginResponse(user, token);
        } catch (BadCredentialsException e) {
            onFailedLoginAttempt(user);
            throw e;
        }
    }


    /**
     * Logout user (client-side token invalidation).
     */
    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            auditLogService.logAction(
                    AuditLog.AuditAction.LOGOUT,
                    "User",
                    String.format("User logged out: %s (ID: %d)", user.getEmail(), user.getId())
            );
        }
        SecurityContextHolder.clearContext();
    }

    /**
     * Initiate forgot password flow by generating and sending reset token.
     */
    public void forgotPassword(PasswordForgotRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            log.info("Password reset requested for non-existent email: {}", request.getEmail());
            return;
        }

        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        tokenRepository.save(resetToken);

        emailService.sendPasswordResetEmail(user.getEmail(), token, user.getFullName());
        log.info("Password reset email sent to: {}", user.getEmail());

        auditLogService.logAction(
                AuditLog.AuditAction.PASSWORD_RESET_REQUESTED,
                "User",
                String.format("Password reset requested for: %s (ID: %d)", user.getEmail(), user.getId())
        );
    }

    /**
     * Reset password using valid token.
     */
    public void resetPassword(PasswordResetRequestDto request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired password reset token"));

        if (!resetToken.isValid()) {
            throw new IllegalArgumentException("Invalid or expired password reset token");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setLastPasswordResetDate(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        user.setAccountLocked(false);
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        auditLogService.logAction(
                AuditLog.AuditAction.PASSWORD_RESET,
                "User",
                String.format("Password reset completed for: %s (ID: %d)", user.getEmail(), user.getId())
        );
    }

    /**
     * Change password for authenticated user.
     */
    public void changePassword(PasswordChangeRequestDto request) {
        User user = requireAuthenticatedUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            auditLogService.logAction(
                    AuditLog.AuditAction.PASSWORD_CHANGE_FAILED,
                    "User",
                    String.format("Failed password change attempt - incorrect current password (ID: %d)", user.getId())
            );
            throw new IllegalArgumentException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setLastPasswordResetDate(LocalDateTime.now());
        userRepository.save(user);

        auditLogService.logAction(
                AuditLog.AuditAction.PASSWORD_CHANGE,
                "User",
                String.format("Password changed for: %s (ID: %d)", user.getEmail(), user.getId())
        );
    }

    /**
     * Update user profile information.
     */
    public UserResponseDto updateProfile(UserProfileUpdateRequest request) {
        User user = requireAuthenticatedUser();

        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Email is already in use");
            }
            user.setEmail(request.getEmail());
        }

        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        User savedUser = userRepository.save(user);

        auditLogService.logAction(
                AuditLog.AuditAction.PROFILE_UPDATED,
                "User",
                String.format("Profile updated for: %s (ID: %d)", user.getEmail(), user.getId())
        );

        return mapToResponseDto(savedUser);
    }

    /**
     * Get current authenticated user profile.
     */
    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUserProfile() {
        User user = requireAuthenticatedUser();
        return mapToResponseDto(user);
    }

    // =========================
    // Login helpers
    // =========================

    private User requireUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password"));
    }

    private void assertAccountNotLocked(User user) {
        if (!user.isAccountLocked()) {
            return;
        }

        auditLogService.logAction(
                AuditLog.AuditAction.LOGIN_FAILED,
                "User",
                String.format("Login attempt for locked account: %s (ID: %d)", user.getEmail(), user.getId())
        );
        throw new LockedException("Account is locked. Please contact administrator or reset your password.");
    }

    private Authentication authenticate(LoginRequestDto request) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
    }

    private void setAuthentication(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void onSuccessfulLogin(User user) {
        user.setFailedLoginAttempts(0);
        user.setLastLoginDate(LocalDateTime.now());
        userRepository.save(user);
    }

    private void logLoginSuccess(User user) {
        auditLogService.logAction(
                AuditLog.AuditAction.LOGIN,
                "User",
                String.format("User logged in: %s (ID: %d)", user.getEmail(), user.getId())
        );
    }

    private void onFailedLoginAttempt(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            lockAccount(user, attempts);
        }

        userRepository.save(user);

        auditLogService.logAction(
                AuditLog.AuditAction.LOGIN_FAILED,
                "User",
                String.format("Failed login attempt %d/%d for: %s (ID: %d)",
                        attempts, MAX_FAILED_ATTEMPTS, user.getEmail(), user.getId())
        );
    }

    private void lockAccount(User user, int attempts) {
        user.setAccountLocked(true);
        emailService.sendAccountLockedEmail(user.getEmail(), user.getFullName());

        auditLogService.logAction(
                AuditLog.AuditAction.ACCOUNT_LOCKED,
                "User",
                String.format("Account locked after %d failed attempts: %s (ID: %d)",
                        attempts, user.getEmail(), user.getId())
        );
    }

    private LoginResponseDto buildLoginResponse(User user, String token) {
        return LoginResponseDto.builder()
                .token(token)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }

    // =========================
    // Authenticated user helper
    // =========================

    private User requireAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new IllegalStateException("User is not authenticated");
        }

        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /**
     * Map User entity to UserResponseDto.
     */
    private UserResponseDto mapToResponseDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setEnabled(user.isEnabled());
        dto.setAccountLocked(user.isAccountLocked());
        dto.setFailedLoginAttempts(user.getFailedLoginAttempts());
        dto.setLastLoginDate(user.getLastLoginDate());
        dto.setLastPasswordResetDate(user.getLastPasswordResetDate());
        dto.setAccountStatus(user.getAccountStatus());
        dto.setRole(user.getRole());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}
