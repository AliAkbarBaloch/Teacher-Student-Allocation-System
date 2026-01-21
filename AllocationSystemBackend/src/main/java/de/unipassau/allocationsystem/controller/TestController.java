package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Test controller for development purposes - allows password reset without authentication
 * WARNING: This should be disabled in production!
 * Only available in 'dev' profile.
 */
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class TestController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Resets a user's password without authentication (dev only).
     * WARNING: This endpoint should never be enabled in production!
     *
     * @param email User's email
     * @param newPassword New password to set
     * @return Password reset result
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String email, @RequestParam String newPassword) {
        try {
            log.warn("TEST ENDPOINT: Resetting password for email: {}", email);
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Email is required"
                ));
            }
            
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Password is required"
                ));
            }
            
            User user = userRepository.findByEmail(email.trim())
                    .orElse(null);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "User not found with email: " + email
                ));
            }
            
            // Reset password and unlock account
            String encodedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedPassword);
            user.setAccountLocked(false);
            user.setFailedLoginAttempts(0);
            user.setEnabled(true);
            user.setAccountStatus(User.AccountStatus.ACTIVE);
            User savedUser = userRepository.save(user);
            
            // Verify password
            boolean passwordMatches = passwordEncoder.matches(newPassword, savedUser.getPassword());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Password reset successfully");
            response.put("email", savedUser.getEmail());
            response.put("passwordVerified", passwordMatches);
            response.put("accountLocked", savedUser.isAccountLocked());
            response.put("enabled", savedUser.isEnabled());
            response.put("role", savedUser.getRole().name());
            
            log.info("Password reset for {}: verified={}", email, passwordMatches);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error resetting password for {}: {}", email, e.getMessage(), e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error resetting password: " + e.getMessage());
            errorResponse.put("error", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    /**
     * Checks if a user exists and returns their account details (dev only).
     * WARNING: This endpoint should never be enabled in production!
     *
     * @param email User's email to check
     * @return User account details if found
     */
    @GetMapping("/check-user")
    public ResponseEntity<?> checkUser(@RequestParam String email) {
        if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Email parameter is required"
                ));
            }
            
            User user = userRepository.findByEmail(email.trim())
                    .orElse(null);
            
            if (user == null) {
                return ResponseEntity.ok(Map.of(
                        "exists", false,
                        "message", "User not found"
                ));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("exists", true);
            response.put("email", user.getEmail());
            response.put("fullName", user.getFullName());
            response.put("role", user.getRole().name());
            response.put("accountLocked", user.isAccountLocked());
            response.put("enabled", user.isEnabled());
            response.put("accountStatus", user.getAccountStatus().name());
            response.put("failedLoginAttempts", user.getFailedLoginAttempts());
            
            return ResponseEntity.ok(response);
    }
}

