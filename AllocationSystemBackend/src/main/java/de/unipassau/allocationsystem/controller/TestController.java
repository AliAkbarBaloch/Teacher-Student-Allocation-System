package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
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
        log.warn("TEST ENDPOINT: Resetting password for email: {}", email);

        String normalizedEmail = normalizeEmail(email);
        ResponseEntity<?> validationError = validateResetPasswordInput(normalizedEmail, newPassword);
        if (validationError != null) {
            return validationError;
        }

        User user = findUserByEmail(normalizedEmail);
        if (user == null) {
            return badRequest("User not found with email: " + normalizedEmail);
        }

        return ok(performPasswordReset(user, newPassword));
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
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null || normalizedEmail.isEmpty()) {
            return badRequest("Email parameter is required");
        }

        User user = findUserByEmail(normalizedEmail);
        if (user == null) {
            return ok(Map.of(
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

        return ok(response);
    }

    // -------------------------
    // Helpers
    // -------------------------

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim();
    }

    /**
     * @return ResponseEntity error if invalid, otherwise null
     */
    private ResponseEntity<?> validateResetPasswordInput(String email, String newPassword) {
        if (email == null || email.isEmpty()) {
            return badRequest("Email is required");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return badRequest("Password is required");
        }
        return null;
    }

    private User findUserByEmail(String email) {
        // repository returns Optional; no exceptions needed
        return userRepository.findByEmail(email).orElse(null);
    }

    private Map<String, Object> performPasswordReset(User user, String newPassword) {
        // If PasswordEncoder were to throw at runtime, let the global exception handler deal with it
        // (or wrap it in a specific exception type if you have a controller advice).
        String encodedPassword = passwordEncoder.encode(newPassword);

        user.setPassword(encodedPassword);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user.setEnabled(true);
        user.setAccountStatus(User.AccountStatus.ACTIVE);

        User savedUser = userRepository.save(user);
        boolean passwordMatches = passwordEncoder.matches(newPassword, savedUser.getPassword());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Password reset successfully");
        response.put("email", savedUser.getEmail());
        response.put("passwordVerified", passwordMatches);
        response.put("accountLocked", savedUser.isAccountLocked());
        response.put("enabled", savedUser.isEnabled());
        response.put("role", savedUser.getRole().name());

        log.info("Password reset for {}: verified={}", savedUser.getEmail(), passwordMatches);
        return response;
    }

    private ResponseEntity<?> ok(Object body) {
        return ResponseEntity.ok(body);
    }

    private ResponseEntity<?> badRequest(String message) {
        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", message
        ));
    }

    @SuppressWarnings("unused")
    private ResponseEntity<?> internalServerError(String message, String errorType) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", message,
                "error", errorType
        ));
    }
}

