package de.unipassau.allocationsystem.testutil;

import de.unipassau.allocationsystem.entity.User;

import java.util.UUID;

/**
 * Shared factory for creating valid User instances in tests.
 * Centralizes common setup to avoid code duplication ("clone") across test classes.
 */
public final class TestUserFactory {

    private TestUserFactory() {
        // utility class
    }

    public static User newEnabledUser(String email, String fullName) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(generateTestPassword());
        user.setFullName(fullName);
        user.setEnabled(true);
        return user;
    }

    private static String generateTestPassword() {
        // Avoid hard-coded password values in tests.
        return "test-" + UUID.randomUUID();
    }
}
