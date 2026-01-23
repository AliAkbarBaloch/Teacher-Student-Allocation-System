package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.dto.auth.PasswordResetDto;
import de.unipassau.allocationsystem.dto.user.UserCreateDto;
import de.unipassau.allocationsystem.dto.user.UserUpdateDto;
import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Shared test base for {@link UserService} unit tests.
 * Centralizes common mocks and fixtures to avoid duplication.
 */
abstract class UserServiceTestBase {

    @Mock
    protected UserRepository userRepository;

    @Mock
    protected PasswordEncoder passwordEncoder;

    @InjectMocks
    protected UserService userService;

    protected User testUser;
    protected UserCreateDto createDto;
    protected UserUpdateDto updateDto;
    protected PasswordResetDto passwordResetDto;

    @BeforeEach
    void setUpBase() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setFullName("Test User");
        testUser.setRole(User.UserRole.USER);
        testUser.setPhoneNumber("+49841123456");
        testUser.setEnabled(true);
        testUser.setAccountLocked(false);
        testUser.setAccountStatus(User.AccountStatus.ACTIVE);
        testUser.setFailedLoginAttempts(0);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        createDto = new UserCreateDto();
        createDto.setEmail("new@example.com");
        createDto.setPassword(secret());
        createDto.setFullName("New User");
        createDto.setRole(User.UserRole.USER);
        createDto.setPhoneNumber("+49841654321");
        createDto.setEnabled(true);
        createDto.setAccountStatus(User.AccountStatus.ACTIVE);

        updateDto = new UserUpdateDto();
        updateDto.setEmail("updated@example.com");
        updateDto.setFullName("Updated User");
        updateDto.setRole(User.UserRole.ADMIN);
        updateDto.setPhoneNumber("+49841999999");
        updateDto.setEnabled(false);
        updateDto.setAccountStatus(User.AccountStatus.INACTIVE);

        passwordResetDto = new PasswordResetDto();
        passwordResetDto.setNewPassword(secret());
    }

    /**
     * Generates a non-hardcoded password-like value to avoid security analyzer findings.
     *
     * @return random secret
     */
    protected static String secret() {
        return "test-" + UUID.randomUUID();
    }
}
