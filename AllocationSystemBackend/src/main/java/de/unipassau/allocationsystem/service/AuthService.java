package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.dto.auth.*;
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

    /**
     * Authenticate user and generate JWT token.
     */
    public LoginResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password"));

        // Check if account is locked
        if (user.isAccountLocked()) {
            auditLogService.logAction(
                    AuditLog.AuditAction.LOGIN_FAILED,
                    "User",
                    String.format("Login attempt for locked account: %s (ID: %d)", user.getEmail(), user.getId())
            );
            throw new LockedException("Account is locked. Please contact administrator or reset your password.");
        }

        try {
            // Authenticate
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Reset failed login attempts on successful login
            user.setFailedLoginAttempts(0);
            user.setLastLoginDate(LocalDateTime.now());
            userRepository.save(user);

            // Generate JWT token
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);

            auditLogService.logAction(
                    AuditLog.AuditAction.LOGIN,
                    "User",
                    String.format("User logged in: %s (ID: %d)", user.getEmail(), user.getId())
            );

            return LoginResponseDto.builder()
                    .token(token)
                    .userId(user.getId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole().name())
                    .build();

        } catch (BadCredentialsException e) {
            // Increment failed login attempts - this is business logic, not just error handling
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);

            if (attempts >= MAX_FAILED_ATTEMPTS) {
                user.setAccountLocked(true);
                emailService.sendAccountLockedEmail(user.getEmail(), user.getFullName());
                auditLogService.logAction(
                        AuditLog.AuditAction.ACCOUNT_LOCKED,
                        "User",
                        String.format("Account locked after %d failed attempts: %s (ID: %d)", attempts, user.getEmail(), user.getId())
                );
            }

            userRepository.save(user);

            auditLogService.logAction(
                    AuditLog.AuditAction.LOGIN_FAILED,
                    "User",
                    String.format("Failed login attempt %d/%d for: %s (ID: %d)", attempts, MAX_FAILED_ATTEMPTS, user.getEmail(), user.getId())
            );

            // Re-throw to let GlobalExceptionHandler handle the response
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
        // Find user by email - don't throw exception if not found (security: don't reveal if email exists)
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        
        if (user == null) {
            // User doesn't exist - just return without sending email (but don't reveal this to the caller)
            log.info("Password reset requested for non-existent email: {}", request.getEmail());
            return;
        }

        // Invalidate any existing tokens for this user
        tokenRepository.deleteByUser(user);

        // Generate new token
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        tokenRepository.save(resetToken);

        // Send password reset email
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
        user.setFailedLoginAttempts(0); // Reset failed attempts
        user.setAccountLocked(false); // Unlock account if locked
        userRepository.save(user);

        // Mark token as used
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new IllegalStateException("User is not authenticated");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            auditLogService.logAction(
                    AuditLog.AuditAction.PASSWORD_CHANGE_FAILED,
                    "User",
                    String.format("Failed password change attempt - incorrect current password (ID: %d)", user.getId())
            );
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // Update password
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new IllegalStateException("User is not authenticated");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if email is being changed and if it's already taken
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new IllegalStateException("User is not authenticated");
        }

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return mapToResponseDto(user);
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
