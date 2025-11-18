package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.dto.*;
import de.unipassau.allocationsystem.entity.AuditLog.AuditAction;
import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import de.unipassau.allocationsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for managing users with automatic audit logging via @Audited annotation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create a new user with automatic audit logging using @Audited annotation.
     */
    @Transactional
    @Audited(
        action = AuditAction.CREATE,
        entityName = AuditEntityNames.USER,
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
     * Update user with automatic audit logging using @Audited annotation.
     */
    @Transactional
    @Audited(
        action = AuditAction.UPDATE,
        entityName = AuditEntityNames.USER,
        description = "Updated user",
        captureNewValue = true
    )
    public User updateUser(Long userId, String newEmail, String newFullName) {
        log.info("Updating user: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Update user
        user.setEmail(newEmail);
        user.setFullName(newFullName);
        User updatedUser = userRepository.save(user);

        return updatedUser;
    }

    /**
     * Delete user with automatic audit logging using @Audited annotation.
     */
    @Transactional
    @Audited(
        action = AuditAction.DELETE,
        entityName = AuditEntityNames.USER,
        description = "Deleted user",
        captureNewValue = false
    )
    public void deleteUser(Long userId) {
        log.info("Deleting user: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Delete user
        userRepository.delete(user);
    }

    /**
     * Get user by ID.
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * Change user password.
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("Changing password for user: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Invalid old password");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Enable or disable user account.
     */
    @Transactional
    @Audited(
        action = AuditAction.UPDATE,
        entityName = AuditEntityNames.USER,
        description = "Updated user enabled status",
        captureNewValue = true
    )
    public User setUserEnabled(Long userId, boolean enabled) {
        log.info("Setting user {} enabled status to: {}", userId, enabled);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEnabled(enabled);
        User updatedUser = userRepository.save(user);

        return updatedUser;
    }

    // ========== NEW COMPREHENSIVE CRUD METHODS ==========

    /**
     * Create user with DTO.
     */
    @Transactional
    @Audited(
        action = AuditAction.CREATE,
        entityName = AuditEntityNames.USER,
        description = "Admin created new user",
        captureNewValue = true
    )
    public UserResponseDto createUserWithDto(UserCreateDto dto) {
        log.info("Creating new user with DTO: {}", dto.getEmail());

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("User with email " + dto.getEmail() + " already exists");
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName());
        user.setRole(dto.getRole());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);
        user.setAccountStatus(dto.getAccountStatus() != null ? dto.getAccountStatus() : User.AccountStatus.ACTIVE);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);

        User savedUser = userRepository.save(user);
        return mapToResponseDto(savedUser);
    }

    /**
     * Update user with DTO.
     */
    @Transactional
    @Audited(
        action = AuditAction.UPDATE,
        entityName = AuditEntityNames.USER,
        description = "Updated user with DTO",
        captureNewValue = true
    )
    public UserResponseDto updateUserWithDto(Long userId, UserUpdateDto dto) {
        log.info("Updating user with DTO: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new DuplicateResourceException("User with email " + dto.getEmail() + " already exists");
            }
            user.setEmail(dto.getEmail());
        }
        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName());
        }
        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }
        if (dto.getPhoneNumber() != null) {
            user.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getEnabled() != null) {
            user.setEnabled(dto.getEnabled());
        }
        if (dto.getAccountStatus() != null) {
            user.setAccountStatus(dto.getAccountStatus());
        }

        User updatedUser = userRepository.save(user);

        return mapToResponseDto(updatedUser);
    }

    /**
     * Activate user account.
     */
    @Transactional
    @Audited(
        action = AuditAction.UPDATE,
        entityName = AuditEntityNames.USER,
        description = "Activated user account",
        captureNewValue = true
    )
    public UserResponseDto activateUser(Long userId) {
        log.info("Activating user: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setEnabled(true);
        user.setAccountStatus(User.AccountStatus.ACTIVE);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);

        User updatedUser = userRepository.save(user);

        return mapToResponseDto(updatedUser);
    }

    /**
     * Deactivate user account.
     */
    @Transactional
    @Audited(
        action = AuditAction.UPDATE,
        entityName = AuditEntityNames.USER,
        description = "Deactivated user account",
        captureNewValue = true
    )
    public UserResponseDto deactivateUser(Long userId) {
        log.info("Deactivating user: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setEnabled(false);
        user.setAccountStatus(User.AccountStatus.INACTIVE);

        User updatedUser = userRepository.save(user);

        return mapToResponseDto(updatedUser);
    }

    /**
     * Reset user password (admin function).
     */
    @Transactional
    @Audited(
        action = AuditAction.UPDATE,
        entityName = AuditEntityNames.USER,
        description = "Admin reset user password",
        captureNewValue = true
    )
    public UserResponseDto resetUserPassword(Long userId, PasswordResetDto dto) {
        log.info("Admin resetting password for user: {}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setLastPasswordResetDate(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        
        return mapToResponseDto(savedUser);
    }

    /**
     * Get user by ID with DTO response.
     */
    @Transactional(readOnly = true)
    public UserResponseDto getUserByIdDto(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return mapToResponseDto(user);
    }

    /**
     * Get all users with pagination, filtering, and search.
     */
    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(
            User.UserRole role,
            User.AccountStatus status,
            Boolean enabled,
            String search,
            int page,
            int size,
            String sortBy,
            String sortDirection
    ) {
        log.info("Getting users - role: {}, status: {}, enabled: {}, search: {}", role, status, enabled, search);

        Pageable pageable = PageRequest.of(
            page,
            size,
            sortDirection.equalsIgnoreCase("DESC") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending()
        );

        Specification<User> spec = Specification.where(null);

        if (role != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("role"), role));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("accountStatus"), status));
        }
        if (enabled != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("enabled"), enabled));
        }
        if (search != null && !search.trim().isEmpty()) {
            String searchPattern = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("email")), searchPattern),
                cb.like(cb.lower(root.get("fullName")), searchPattern)
            ));
        }

        Page<User> users = userRepository.findAll(spec, pageable);
        return users.map(this::mapToResponseDto);
    }

    /**
     * Get user statistics.
     */
    @Transactional(readOnly = true)
    public UserStatisticsDto getUserStatistics() {
        log.info("Getting user statistics");

        UserStatisticsDto stats = new UserStatisticsDto();
        stats.setTotalUsers(userRepository.count());
        stats.setActiveUsers(userRepository.countByAccountStatus(User.AccountStatus.ACTIVE));
        stats.setInactiveUsers(userRepository.countByAccountStatus(User.AccountStatus.INACTIVE));
        stats.setSuspendedUsers(userRepository.countByAccountStatus(User.AccountStatus.SUSPENDED));
        stats.setLockedUsers(userRepository.countByAccountLocked(true));
        stats.setAdminUsers(userRepository.countByRole(User.UserRole.ADMIN));
        stats.setRegularUsers(userRepository.countByRole(User.UserRole.USER));

        return stats;
    }

    /**
     * Map User entity to UserResponseDto.
     */
    private UserResponseDto mapToResponseDto(User user) {
        return new UserResponseDto(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getRole(),
            user.getPhoneNumber(),
            user.isEnabled(),
            user.isAccountLocked(),
            user.getAccountStatus(),
            user.getFailedLoginAttempts(),
            user.getLastLoginDate(),
            user.getLastPasswordResetDate(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}
