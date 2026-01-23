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

/**
 * Configuration class for initializing database with default data.
 * Creates default users (test and admin) and academic years on application startup.
 * Active only in non-test profiles.
 * 
 * WARNING: This class contains hardcoded passwords for development purposes.
 * In production, use secure configuration management or remove this initializer.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class DataInitializer {

    private final UserRepository userRepository;
    private final AcademicYearRepository academicYearRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Initializes default data on application startup.
     * Creates test users and academic years if they don't exist.
     * 
     * @return CommandLineRunner that executes the initialization logic
     */
    @Bean
    public CommandLineRunner initData() {
        return args -> {
            insertUsers();
            insertAcademicYears();
            log.info("=== Data Initialization Complete ===");
        };
    }

    private void insertUsers() {
        // WARNING: Hardcoded passwords - for development only. Change in production!
        String testPassword = "password123";
        String adminPassword = "admin123";
        
        User testUser = createUser("test@example.com", testPassword, "Test User", 
                                   User.UserRole.USER, "1234567890");
        User adminUser = createUser("admin@example.com", adminPassword, "Admin User", 
                                    User.UserRole.ADMIN, "0987654321");

        List<User> users = List.of(testUser, adminUser);
        String[] rawPasswords = {testPassword, adminPassword};

        log.info("Test Users Available:");
        for (int i = 0; i < users.size(); i++) {
            if (userRepository.findByEmail(users.get(i).getEmail()).isEmpty()) {
                userRepository.save(users.get(i));
            }
            log.info("{} user created: {} / {}", users.get(i).getRole(), 
                    users.get(i).getEmail(), rawPasswords[i]);
        }
        log.info("====================================");
    }
    
    private User createUser(String email, String rawPassword, String fullName, 
                           User.UserRole role, String phoneNumber) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName);
        user.setEnabled(true);
        user.setIsActive(true);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user.setLoginAttempt(0);
        user.setAccountStatus(User.AccountStatus.ACTIVE);
        user.setRole(role);
        user.setPhoneNumber(phoneNumber);
        return user;
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
