package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.*;
import de.unipassau.allocationsystem.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for authentication operations: login, logout, password management, and profile.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthService authService;

    /**
     * Login endpoint.
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        log.info("Login attempt for email: {}", request.getEmail());
        LoginResponseDto response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Logout endpoint.
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        log.info("Logout request");
        authService.logout();
        return ResponseEntity.ok(Map.of("message", "Logout successful"));
    }

    /**
     * Forgot password endpoint - initiates password reset flow.
     * POST /api/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody PasswordForgotRequestDto request) {
        log.info("Forgot password request for email: {}", request.getEmail());
        authService.forgotPassword(request);
        return ResponseEntity.ok(Map.of(
                "message", "If the email exists, a password reset link has been sent."
        ));
    }

    /**
     * Reset password endpoint - completes password reset using token.
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody PasswordResetRequestDto request) {
        log.info("Reset password request");
        authService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password has been reset successfully"));
    }

    /**
     * Change password endpoint - for authenticated users.
     * POST /api/auth/change-password
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody PasswordChangeRequestDto request) {
        log.info("Change password request");
        authService.changePassword(request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    /**
     * Get current user profile.
     * GET /api/auth/profile
     */
    @GetMapping("/profile")
    public ResponseEntity<UserResponseDto> getProfile() {
        log.info("Get profile request");
        UserResponseDto profile = authService.getCurrentUserProfile();
        return ResponseEntity.ok(profile);
    }

    /**
     * Update current user profile.
     * PUT /api/auth/profile
     */
    @PutMapping("/profile")
    public ResponseEntity<UserResponseDto> updateProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        log.info("Update profile request");
        UserResponseDto updated = authService.updateProfile(request);
        return ResponseEntity.ok(updated);
    }
}
