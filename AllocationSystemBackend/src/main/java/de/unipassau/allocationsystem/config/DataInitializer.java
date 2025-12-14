// src/main/java/de/unipassau/allocationsystem/config/DataInitializer.java
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
import java.util.List;

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
            insertUsers();
            insertAcademicYears();
            log.info("=== Data Initialization Complete ===");
        };
    }

    private void insertUsers() {
        String[] rawPasswords = {"password123", "admin123"};
        
        // Create test user
        User testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPassword(passwordEncoder.encode(rawPasswords[0]));
        testUser.setFullName("Test User");
        testUser.setEnabled(true);
        testUser.setIsActive(true);
        testUser.setAccountLocked(false);
        testUser.setFailedLoginAttempts(0);
        testUser.setLoginAttempt(0);
        testUser.setAccountStatus(User.AccountStatus.ACTIVE);
        testUser.setRole(User.UserRole.USER);
        testUser.setPhoneNumber("1234567890");

        // Create admin user
        User adminUser = new User();
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword(passwordEncoder.encode(rawPasswords[1]));
        adminUser.setFullName("Admin User");
        adminUser.setEnabled(true);
        adminUser.setIsActive(true);
        adminUser.setAccountLocked(false);
        adminUser.setFailedLoginAttempts(0);
        adminUser.setLoginAttempt(0);
        adminUser.setAccountStatus(User.AccountStatus.ACTIVE);
        adminUser.setRole(User.UserRole.ADMIN);
        adminUser.setPhoneNumber("0987654321");

        List<User> users = List.of(testUser, adminUser);

        log.info("Test Users Available:");
        for (int i = 0; i < users.size(); i++) {
            String rawPassword = rawPasswords[i];
            if (userRepository.findByEmail(users.get(i).getEmail()).isEmpty()) {
                userRepository.save(users.get(i));
            }
            log.info("{} user created: {} / {}", users.get(i).getRole(), users.get(i).getEmail(), rawPassword);
        }
        log.info("====================================");
    }

    private void insertAcademicYears() {
        log.info("Initializing Academic Years...");
        int academicYearsCreated = 0;
        for (int year = 2025; year <= 2035; year++) {
            String yearName = year + "/" + (year + 1);

            if (academicYearRepository.findByYearName(yearName).isEmpty()) {
                AcademicYear academicYear = new AcademicYear();
                academicYear.setYearName(yearName);
                academicYear.setTotalCreditHours(1000);
                academicYear.setElementarySchoolHours(400);
                academicYear.setMiddleSchoolHours(600);
                academicYear.setBudgetAnnouncementDate(LocalDateTime.of(year, 1, 1, 0, 0));
                academicYear.setAllocationDeadline(LocalDateTime.of(year, 6, 30, 23, 59));
                academicYear.setIsLocked(false);

                academicYearRepository.save(academicYear);
                academicYearsCreated++;
                log.info("Academic year created: {}", yearName);
            }
        }
        log.info("Academic years initialized: {} created", academicYearsCreated);
    }
}
