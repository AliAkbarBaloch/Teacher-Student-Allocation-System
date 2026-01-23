package de.unipassau.allocationsystem.testutil;

import de.unipassau.allocationsystem.entity.User;

import java.util.UUID;

/**
 * Shared factory for creating valid {@link User} instances in tests.
 * Centralizes common setup to avoid code duplication across test classes.
 */
public final class TestUserFactory {

    private TestUserFactory() {
        // utility class
    }

    /**
     * Creates an enabled {@link User} instance suitable for tests.
     * <p>
     * The returned user has the provided email and full name, is enabled, and contains a generated
     * password value to avoid hard-coded secrets in tests.
     * </p>
     *
     * @param email the user's email
     * @param fullName the user's full name
     * @return a new enabled user instance
     */
    public static User newEnabledUser(String email, String fullName) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(generateTestPassword());
        user.setFullName(fullName);
        user.setEnabled(true);
        return user;
    }

    /**
     * Generates a non-hardcoded password value for test users.
     *
     * @return generated password
     */
    private static String generateTestPassword() {
        return "test-" + UUID.randomUUID();
    }
}
