package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.auth.*;
import de.unipassau.allocationsystem.dto.user.UserProfileUpdateRequest;
import de.unipassau.allocationsystem.dto.user.UserResponseDto;
import de.unipassau.allocationsystem.service.AuthService;
import de.unipassau.allocationsystem.utils.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Login", description = "Authenticate user and return access tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = LoginResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request) {
        log.info("Login attempt for email: {}", request.getEmail());
        LoginResponseDto response = authService.login(request);
        return ResponseHandler.success("Login successful", response);
    }

    @Operation(summary = "Logout", description = "Invalidate current user session / tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        log.info("Logout request");
        authService.logout();
        return ResponseHandler.success("Logout successful", Map.of());
    }

    @Operation(summary = "Forgot password", description = "Initiate password reset. An email will be sent if the account exists.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset initiated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody PasswordForgotRequestDto request) {
        log.info("Forgot password request for email: {}", request.getEmail());
        authService.forgotPassword(request);
        return ResponseHandler.success("If the email exists, a password reset link has been sent.", Map.of());
    }

    @Operation(summary = "Reset password", description = "Complete password reset using token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successful"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetRequestDto request) {
        log.info("Reset password request");
        authService.resetPassword(request);
        return ResponseHandler.success("Password has been reset successfully", Map.of());
    }

    @Operation(summary = "Change password", description = "Change password for authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody PasswordChangeRequestDto request) {
        log.info("Change password request");
        authService.changePassword(request);
        return ResponseHandler.success("Password changed successfully", Map.of());
    }

    @Operation(summary = "Get current user profile", description = "Retrieve profile information for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        log.info("Get profile request");
        UserResponseDto profile = authService.getCurrentUserProfile();
        return ResponseHandler.success("Profile retrieved successfully", profile);
    }

    @Operation(summary = "Update current user profile", description = "Update profile information for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UserProfileUpdateRequest request) {
        log.info("Update profile request");
        UserResponseDto updated = authService.updateProfile(request);
        return ResponseHandler.updated("Profile updated successfully", updated);
    }
}
