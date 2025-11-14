package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service for managing users with integrated audit logging.
 * This is an example service demonstrating how to use the audit logging system.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    /**
     * Create a new user with automatic audit logging using @Audited annotation.
     */
    @Transactional
    @Audited(
        action = AuditAction.CREATE,
        entityName = "User",
        description = "Created new user",
        captureNewValue = true
    )
    public User createUser(String email, String password, String fullName) {
        log.info("Creating new user: {}", email);

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        // The @Audited annotation will automatically create an audit log entry
        return savedUser;
    }

    /**
     * Update user with manual audit logging (for more control over captured values).
     */
    @Transactional
    public User updateUser(Long userId, String newEmail, String newFullName) {
        log.info("Updating user: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Capture previous state
        Map<String, Object> previousValue = new HashMap<>();
        previousValue.put("email", user.getEmail());
        previousValue.put("fullName", user.getFullName());

        // Update user
        user.setEmail(newEmail);
        user.setFullName(newFullName);
        User updatedUser = userRepository.save(user);

        // Capture new state
        Map<String, Object> newValue = new HashMap<>();
        newValue.put("email", updatedUser.getEmail());
        newValue.put("fullName", updatedUser.getFullName());

        // Manually log the update with before/after values
        auditLogService.logUpdate("User", userId.toString(), previousValue, newValue);

        return updatedUser;
    }

    /**
     * Delete user with audit logging.
     */
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Deleting user: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Capture user state before deletion
        Map<String, Object> previousValue = new HashMap<>();
        previousValue.put("id", user.getId());
        previousValue.put("email", user.getEmail());
        previousValue.put("fullName", user.getFullName());
        previousValue.put("enabled", user.isEnabled());

        // Delete user
        userRepository.delete(user);

        // Log the deletion
        auditLogService.logDelete("User", userId.toString(), previousValue);
    }

    /**
     * Get user by ID (optionally with view logging for sensitive operations).
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long userId, boolean logView) {
        Optional<User> user = userRepository.findById(userId);

        // Optionally log view operations for compliance
        if (logView && user.isPresent()) {
            auditLogService.logView("User", userId.toString());
        }

        return user;
    }

    /**
     * Change user password with audit logging.
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("Changing password for user: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            // Log failed attempt
            auditLogService.logAction(
                AuditAction.PASSWORD_CHANGE,
                "User",
                "Failed password change attempt for user: " + userId
            );
            throw new RuntimeException("Invalid old password");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Log successful password change (without storing passwords)
        auditLogService.logAction(
            AuditAction.PASSWORD_CHANGE,
            "User",
            "Successfully changed password for user: " + userId
        );
    }

    /**
     * Enable or disable user account.
     */
    @Transactional
    public User setUserEnabled(Long userId, boolean enabled) {
        log.info("Setting user {} enabled status to: {}", userId, enabled);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        boolean previousEnabled = user.isEnabled();
        user.setEnabled(enabled);
        User updatedUser = userRepository.save(user);

        // Log the status change
        Map<String, Object> previousValue = Map.of("enabled", previousEnabled);
        Map<String, Object> newValue = Map.of("enabled", enabled);

        auditLogService.logUpdate("User", userId.toString(), previousValue, newValue);

        return updatedUser;
    }
}
