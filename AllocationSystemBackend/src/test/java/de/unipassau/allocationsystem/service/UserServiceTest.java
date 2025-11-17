package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.dto.*;
import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserCreateDto createDto;
    private UserUpdateDto updateDto;
    private PasswordResetDto passwordResetDto;

    @BeforeEach
    void setUp() {
        // Setup test user
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

        // Setup create DTO
        createDto = new UserCreateDto();
        createDto.setEmail("new@example.com");
        createDto.setPassword("password123");
        createDto.setFullName("New User");
        createDto.setRole(User.UserRole.USER);
        createDto.setPhoneNumber("+49841654321");
        createDto.setEnabled(true);
        createDto.setAccountStatus(User.AccountStatus.ACTIVE);

        // Setup update DTO
        updateDto = new UserUpdateDto();
        updateDto.setEmail("updated@example.com");
        updateDto.setFullName("Updated User");
        updateDto.setRole(User.UserRole.ADMIN);
        updateDto.setPhoneNumber("+49841999999");
        updateDto.setEnabled(false);
        updateDto.setAccountStatus(User.AccountStatus.INACTIVE);

        // Setup password reset DTO
        passwordResetDto = new PasswordResetDto();
        passwordResetDto.setNewPassword("newPassword123");
    }

    // ========== CREATE USER TESTS ==========

    @Test
    void createUser_Success() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        String fullName = "Test User";
        String encodedPassword = "encodedPassword123";

        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.createUser(email, password, fullName);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
        // Audit logging is now handled by @Audited annotation via AOP
    }

    @Test
    void createUserWithDto_Success() {
        // Arrange
        User newUser = new User();
        newUser.setId(2L);
        newUser.setEmail(createDto.getEmail());
        newUser.setPassword("encodedPassword");
        newUser.setFullName(createDto.getFullName());
        newUser.setRole(createDto.getRole());
        newUser.setPhoneNumber(createDto.getPhoneNumber());
        newUser.setEnabled(true);
        newUser.setAccountStatus(User.AccountStatus.ACTIVE);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        when(userRepository.existsByEmail(createDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(createDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // Act
        UserResponseDto result = userService.createUserWithDto(createDto);

        // Assert
        assertNotNull(result);
        assertEquals(newUser.getId(), result.getId());
        assertEquals(createDto.getEmail(), result.getEmail());
        assertEquals(createDto.getFullName(), result.getFullName());
        assertEquals(createDto.getRole(), result.getRole());
        verify(userRepository).existsByEmail(createDto.getEmail());
        verify(passwordEncoder).encode(createDto.getPassword());
        verify(userRepository).save(any(User.class));
        // Audit logging is now handled by @Audited annotation via AOP
    }

    @Test
    void createUserWithDto_DuplicateEmail_ThrowsException() {
        // Arrange
        when(userRepository.existsByEmail(createDto.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () ->
            userService.createUserWithDto(createDto)
        );
        verify(userRepository).existsByEmail(createDto.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    // ========== UPDATE USER TESTS ==========

    @Test
    void updateUser_Success() {
        // Arrange
        Long userId = 1L;
        String newEmail = "newemail@example.com";
        String newFullName = "New Full Name";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.updateUser(userId, newEmail, newFullName);

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
        // Audit logging is now handled by @Audited annotation via AOP
    }

    @Test
    void updateUser_UserNotFound_ThrowsException() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            userService.updateUser(userId, "new@example.com", "New Name")
        );
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserWithDto_Success() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(updateDto.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponseDto result = userService.updateUserWithDto(testUser.getId(), updateDto);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        verify(userRepository).findById(testUser.getId());
        verify(userRepository).existsByEmail(updateDto.getEmail());
        verify(userRepository).save(any(User.class));
        // Audit logging is now handled by @Audited annotation via AOP
    }

    @Test
    void updateUserWithDto_UserNotFound_ThrowsException() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            userService.updateUserWithDto(userId, updateDto)
        );
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserWithDto_DuplicateEmail_ThrowsException() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(updateDto.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateResourceException.class, () ->
            userService.updateUserWithDto(testUser.getId(), updateDto)
        );
        verify(userRepository).findById(testUser.getId());
        verify(userRepository).existsByEmail(updateDto.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    // ========== DELETE USER TESTS ==========

    @Test
    void deleteUser_Success() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);

        // Act
        userService.deleteUser(userId);

        // Assert
        verify(userRepository).findById(userId);
        verify(userRepository).delete(testUser);
        // Audit logging is now handled by @Audited annotation via AOP
    }

    @Test
    void deleteUser_UserNotFound_ThrowsException() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            userService.deleteUser(userId)
        );
        verify(userRepository).findById(userId);
        verify(userRepository, never()).delete(any(User.class));
    }

    // ========== GET USER TESTS ==========

    @Test
    void getUserById_UserExists_ReturnsUser() {
        // Arrange
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userService.getUserById(userId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        assertEquals(testUser.getEmail(), result.get().getEmail());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_UserNotFound_ReturnsEmpty() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userService.getUserById(userId);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserByIdDto_Success() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // Act
        UserResponseDto result = userService.getUserByIdDto(testUser.getId());

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getFullName(), result.getFullName());
        verify(userRepository).findById(testUser.getId());
    }

    @Test
    void getUserByIdDto_UserNotFound_ThrowsException() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            userService.getUserByIdDto(userId)
        );
        verify(userRepository).findById(userId);
    }

    // ========== PASSWORD MANAGEMENT TESTS ==========

    @Test
    void changePassword_Success() {
        // Arrange
        Long userId = 1L;
        String oldPassword = "oldPassword";
        String newPassword = "newPassword";
        String encodedNewPassword = "encodedNewPassword";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(oldPassword, "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);

        // Act
        userService.changePassword(userId, oldPassword, newPassword);

        // Assert
        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(oldPassword, "encodedPassword");
        verify(passwordEncoder).encode(newPassword);
    }

    @Test
    void changePassword_UserNotFound_ThrowsException() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            userService.changePassword(userId, "oldPass", "newPass")
        );
        verify(userRepository).findById(userId);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void changePassword_InvalidOldPassword_ThrowsException() {
        // Arrange
        Long userId = 1L;
        String oldPassword = "wrongPassword";
        String newPassword = "newPassword";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(oldPassword, "encodedPassword")).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            userService.changePassword(userId, oldPassword, newPassword)
        );
        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(oldPassword, "encodedPassword");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetUserPassword_Success() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(passwordResetDto.getNewPassword())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponseDto result = userService.resetUserPassword(testUser.getId(), passwordResetDto);

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(testUser.getId());
        verify(passwordEncoder).encode(passwordResetDto.getNewPassword());
        verify(userRepository).save(any(User.class));
        // Audit logging is now handled by @Audited annotation via AOP
    }

    @Test
    void resetUserPassword_UserNotFound_ThrowsException() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            userService.resetUserPassword(userId, passwordResetDto)
        );
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    // ========== ACCOUNT STATUS TESTS ==========

    @Test
    void setUserEnabled_EnableUser_Success() {
        // Arrange
        Long userId = 1L;
        testUser.setEnabled(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.setUserEnabled(userId, true);

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
        // Audit logging is now handled by @Audited annotation via AOP
    }

    @Test
    void setUserEnabled_DisableUser_Success() {
        // Arrange
        Long userId = 1L;
        testUser.setEnabled(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = userService.setUserEnabled(userId, false);

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
        // Audit logging is now handled by @Audited annotation via AOP
    }

    @Test
    void setUserEnabled_UserNotFound_ThrowsException() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
            userService.setUserEnabled(userId, true)
        );
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void activateUser_Success() {
        // Arrange
        testUser.setEnabled(false);
        testUser.setAccountStatus(User.AccountStatus.INACTIVE);
        testUser.setAccountLocked(true);
        testUser.setFailedLoginAttempts(3);

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponseDto result = userService.activateUser(testUser.getId());

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(testUser.getId());
        verify(userRepository).save(any(User.class));
        // Audit logging is now handled by @Audited annotation via AOP
    }

    @Test
    void activateUser_UserNotFound_ThrowsException() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            userService.activateUser(userId)
        );
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deactivateUser_Success() {
        // Arrange
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        UserResponseDto result = userService.deactivateUser(testUser.getId());

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(testUser.getId());
        verify(userRepository).save(any(User.class));
        // Audit logging is now handled by @Audited annotation via AOP
    }

    @Test
    void deactivateUser_UserNotFound_ThrowsException() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () ->
            userService.deactivateUser(userId)
        );
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    // ========== GET ALL USERS TESTS ==========

    @Test
    void getAllUsers_WithoutFilters_ReturnsPage() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);

        // Act
        Page<UserResponseDto> result = userService.getAllUsers(
            null, null, null, null, 0, 10, "id", "ASC"
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAllUsers_WithRoleFilter_ReturnsFilteredPage() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);

        // Act
        Page<UserResponseDto> result = userService.getAllUsers(
            User.UserRole.USER, null, null, null, 0, 10, "id", "ASC"
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAllUsers_WithStatusFilter_ReturnsFilteredPage() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);

        // Act
        Page<UserResponseDto> result = userService.getAllUsers(
            null, User.AccountStatus.ACTIVE, null, null, 0, 10, "id", "ASC"
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAllUsers_WithEnabledFilter_ReturnsFilteredPage() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);

        // Act
        Page<UserResponseDto> result = userService.getAllUsers(
            null, null, true, null, 0, 10, "id", "ASC"
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAllUsers_WithSearchQuery_ReturnsFilteredPage() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);

        // Act
        Page<UserResponseDto> result = userService.getAllUsers(
            null, null, null, "test", 0, 10, "id", "ASC"
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAllUsers_WithAllFilters_ReturnsFilteredPage() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users);

        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);

        // Act
        Page<UserResponseDto> result = userService.getAllUsers(
            User.UserRole.USER, 
            User.AccountStatus.ACTIVE, 
            true, 
            "test", 
            0, 
            10, 
            "email", 
            "DESC"
        );

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    // ========== STATISTICS TESTS ==========

    @Test
    void getUserStatistics_Success() {
        // Arrange
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.countByAccountStatus(User.AccountStatus.ACTIVE)).thenReturn(80L);
        when(userRepository.countByAccountStatus(User.AccountStatus.INACTIVE)).thenReturn(15L);
        when(userRepository.countByAccountStatus(User.AccountStatus.SUSPENDED)).thenReturn(5L);
        when(userRepository.countByAccountLocked(true)).thenReturn(3L);
        when(userRepository.countByRole(User.UserRole.ADMIN)).thenReturn(10L);
        when(userRepository.countByRole(User.UserRole.USER)).thenReturn(90L);

        // Act
        UserStatisticsDto result = userService.getUserStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getTotalUsers());
        assertEquals(80L, result.getActiveUsers());
        assertEquals(15L, result.getInactiveUsers());
        assertEquals(5L, result.getSuspendedUsers());
        assertEquals(3L, result.getLockedUsers());
        assertEquals(10L, result.getAdminUsers());
        assertEquals(90L, result.getRegularUsers());

        verify(userRepository).count();
        verify(userRepository, times(3)).countByAccountStatus(any(User.AccountStatus.class));
        verify(userRepository).countByAccountLocked(true);
        verify(userRepository, times(2)).countByRole(any(User.UserRole.class));
    }

    @Test
    void getUserStatistics_NoUsers_ReturnsZeroStatistics() {
        // Arrange
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.countByAccountStatus(any(User.AccountStatus.class))).thenReturn(0L);
        when(userRepository.countByAccountLocked(anyBoolean())).thenReturn(0L);
        when(userRepository.countByRole(any(User.UserRole.class))).thenReturn(0L);

        // Act
        UserStatisticsDto result = userService.getUserStatistics();

        // Assert
        assertNotNull(result);
        assertEquals(0L, result.getTotalUsers());
        assertEquals(0L, result.getActiveUsers());
        assertEquals(0L, result.getInactiveUsers());
        assertEquals(0L, result.getSuspendedUsers());
        assertEquals(0L, result.getLockedUsers());
        assertEquals(0L, result.getAdminUsers());
        assertEquals(0L, result.getRegularUsers());
    }
}
