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

import java.util.ArrayList;
import java.util.List;

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
            String[] rawPasswords = {"password123", "admin123"};

            List<User> users = new ArrayList<>();
            users.add(new User(null, "test@example.com", passwordEncoder.encode(rawPasswords[0]),
                    "Test User", true, false, 0, null,
                    null, User.AccountStatus.ACTIVE, User.UserRole.USER, "1234567890",
                    null, null
            ));
            users.add(new User(null, "admin@example.com", passwordEncoder.encode(rawPasswords[1]),
                    "Admin User", true, false, 0, null,
                    null, User.AccountStatus.ACTIVE, User.UserRole.ADMIN, "0987654321",
                    null, null
            ));

            log.info("=== Data Initialization Complete ===");
            log.info("Test Users Available:");
            for (int i = 0; i < users.size(); i++) {
                String rawPassword = rawPasswords[i];
                if (userRepository.findByEmail(users.get(i).getEmail()).isEmpty()) {
                    userRepository.save(users.get(i));
                }
                log.info("{} user created: {} / {}", users.get(i).getRole(), users.get(i).getEmail(), rawPassword);
            }
            log.info("====================================");
        };
    }
}
