package de.unipassau.allocationsystem.config;

import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Data initialization for development and testing.
 * Creates default test users if they don't exist.
 * Only runs in non-test profiles.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // Check if test user already exists
            if (userRepository.findByEmail("test@example.com").isEmpty()) {
                // Create test user
                User testUser = new User();
                testUser.setEmail("test@example.com");
                testUser.setPassword(passwordEncoder.encode("password123"));
                testUser.setFullName("Test User");
                testUser.setPhoneNumber("1234567890");
                testUser.setRole(User.UserRole.USER);
                testUser.setEnabled(true);
                testUser.setAccountLocked(false);
                testUser.setFailedLoginAttempts(0);
                
                userRepository.save(testUser);
                log.info("Test user created: test@example.com / password123");
            } else {
                log.info("Test user already exists: test@example.com");
            }

            // Create admin user if doesn't exist
            if (userRepository.findByEmail("admin@example.com").isEmpty()) {
                User adminUser = new User();
                adminUser.setEmail("admin@example.com");
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                adminUser.setFullName("Admin User");
                adminUser.setPhoneNumber("0987654321");
                adminUser.setRole(User.UserRole.ADMIN);
                adminUser.setEnabled(true);
                adminUser.setAccountLocked(false);
                adminUser.setFailedLoginAttempts(0);
                
                userRepository.save(adminUser);
                log.info("Admin user created: admin@example.com / admin123");
            } else {
                log.info("Admin user already exists: admin@example.com");
            }

            log.info("=== Data Initialization Complete ===");
            log.info("Test Users Available:");
            log.info("  - test@example.com / password123 (USER)");
            log.info("  - admin@example.com / admin123 (ADMIN)");
            log.info("====================================");
        };
    }
}
