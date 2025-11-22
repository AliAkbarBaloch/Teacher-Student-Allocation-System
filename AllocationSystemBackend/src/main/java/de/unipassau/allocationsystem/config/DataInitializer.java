package de.unipassau.allocationsystem.config;

import de.unipassau.allocationsystem.entity.AcademicYear;
import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.repository.AcademicYearRepository;
import de.unipassau.allocationsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
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
    private final AcademicYearRepository academicYearRepository;
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

            // Initialize academic years (2025-2035)
            log.info("Initializing Academic Years...");
            int academicYearsCreated = 0;
            for (int year = 2025; year <= 2035; year++) {
                String yearName = year + "/" + (year + 1);
                
                // Check if academic year already exists
                if (academicYearRepository.findByYearName(yearName).isEmpty()) {
                    AcademicYear academicYear = new AcademicYear();
                    academicYear.setYearName(yearName);
                    academicYear.setTotalCreditHours(1000); // Default total credit hours
                    academicYear.setElementarySchoolHours(400); // Default elementary hours
                    academicYear.setMiddleSchoolHours(600); // Default middle school hours
                    
                    // Set budget announcement date to January 1st of the year
                    academicYear.setBudgetAnnouncementDate(LocalDateTime.of(year, 1, 1, 0, 0));
                    
                    // Set allocation deadline to June 30th of the year
                    academicYear.setAllocationDeadline(LocalDateTime.of(year, 6, 30, 23, 59));
                    
                    academicYear.setIsLocked(false); // Not locked by default
                    
                    academicYearRepository.save(academicYear);
                    academicYearsCreated++;
                    log.info("Academic year created: {}", yearName);
                }
            }
            log.info("Academic years initialized: {} created", academicYearsCreated);

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
