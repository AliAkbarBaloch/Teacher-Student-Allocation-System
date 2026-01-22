package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.aspect.Audited;
import de.unipassau.allocationsystem.constant.AuditEntityNames;
import de.unipassau.allocationsystem.dto.auth.PasswordResetDto;
import de.unipassau.allocationsystem.dto.user.UserCreateDto;
import de.unipassau.allocationsystem.dto.user.UserResponseDto;
import de.unipassau.allocationsystem.dto.user.UserStatisticsDto;
import de.unipassau.allocationsystem.dto.user.UserUpdateDto;
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
import java.util.function.Consumer;

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
    @Audited(
            action = AuditAction.CREATE,
            entityName = AuditEntityNames.USER,
            description = "Created new user",
            captureNewValue = true
    )
    public User createUser(String email, String password, String fullName) {
        UserCreateDto dto = new UserCreateDto();
        dto.setEmail(email);
        dto.setPassword(password);
        dto.setFullName(fullName);
        return toEntity(createUserWithDto(dto));
    }

    /**
     * Update user with automatic audit logging using @Audited annotation.
     */
    @Audited(
            action = AuditAction.UPDATE,
            entityName = AuditEntityNames.USER,
            description = "Updated user",
            captureNewValue = true
    )
    public User updateUser(Long userId, String newEmail, String newFullName) {
        UserUpdateDto dto = new UserUpdateDto();
        dto.setEmail(newEmail);
        dto.setFullName(newFullName);
        return toEntity(updateUserWithDto(userId, dto));
    }

    /**
     * Delete user with automatic audit logging using @Audited annotation.
     */
    @Audited(
            action = AuditAction.DELETE,
            entityName = AuditEntityNames.USER,
            description = "Deleted user",
            captureNewValue = false
    )
    public void deleteUser(Long userId) {
        userRepository.delete(requireUser(userId));
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
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = requireUser(userId);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    /**
     * Enable or disable user account.
     */
    @Audited(
            action = AuditAction.UPDATE,
            entityName = AuditEntityNames.USER,
            description = "Updated user enabled status",
            captureNewValue = true
    )
    public User setUserEnabled(Long userId, boolean enabled) {
        User user = requireUser(userId);
        user.setEnabled(enabled);
        return userRepository.save(user);
    }

    // ========== DTO-BASED CRUD METHODS ==========

    /**
     * Create user with DTO.
     */
    @Audited(
            action = AuditAction.CREATE,
            entityName = AuditEntityNames.USER,
            description = "Admin created new user",
            captureNewValue = true
    )
    public UserResponseDto createUserWithDto(UserCreateDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("User with email " + dto.getEmail() + " already exists");
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName());
        user.setRole(dto.getRole());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setEnabled(defaultTrue(dto.getEnabled()));
        user.setIsActive(defaultTrue(dto.getIsActive()));
        user.setAccountStatus(defaultAccountStatus(dto.getAccountStatus()));
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user.setLoginAttempt(0);

        return mapToResponseDto(userRepository.save(user));
    }

    /**
     * Update user with DTO.
     */
    @Audited(
            action = AuditAction.UPDATE,
            entityName = AuditEntityNames.USER,
            description = "Updated user with DTO",
            captureNewValue = true
    )
    public UserResponseDto updateUserWithDto(Long userId, UserUpdateDto dto) {
        User user = requireUser(userId);

        String incomingEmail = dto.getEmail();
        if (incomingEmail != null && !incomingEmail.equals(user.getEmail())) {
            if (userRepository.existsByEmail(incomingEmail)) {
                throw new DuplicateResourceException("User with email " + incomingEmail + " already exists");
            }
            user.setEmail(incomingEmail);
        }

        setIfPresent(dto.getFullName(), user::setFullName);
        setIfPresent(dto.getRole(), user::setRole);
        setIfPresent(dto.getPhoneNumber(), user::setPhoneNumber);
        setIfPresent(dto.getEnabled(), user::setEnabled);
        setIfPresent(dto.getIsActive(), user::setIsActive);
        setIfPresent(dto.getAccountStatus(), user::setAccountStatus);

        return mapToResponseDto(userRepository.save(user));
    }

    /**
     * Activate user account.
     */
    @Audited(
            action = AuditAction.UPDATE,
            entityName = AuditEntityNames.USER,
            description = "Activated user account",
            captureNewValue = true
    )
    public UserResponseDto activateUser(Long userId) {
        User user = requireUser(userId);

        user.setEnabled(true);
        user.setAccountStatus(User.AccountStatus.ACTIVE);
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);

        return mapToResponseDto(userRepository.save(user));
    }

    /**
     * Deactivate user account.
     */
    @Audited(
            action = AuditAction.UPDATE,
            entityName = AuditEntityNames.USER,
            description = "Deactivated user account",
            captureNewValue = true
    )
    public UserResponseDto deactivateUser(Long userId) {
        User user = requireUser(userId);

        user.setEnabled(false);
        user.setAccountStatus(User.AccountStatus.INACTIVE);

        return mapToResponseDto(userRepository.save(user));
    }

    /**
     * Reset user password (admin function).
     */
    @Audited(
            action = AuditAction.UPDATE,
            entityName = AuditEntityNames.USER,
            description = "Admin reset user password",
            captureNewValue = true
    )
    public UserResponseDto resetUserPassword(Long userId, PasswordResetDto dto) {
        User user = requireUser(userId);

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        LocalDateTime now = LocalDateTime.now();
        user.setLastPasswordResetDate(now);
        user.setPasswordUpdateDate(now);

        return mapToResponseDto(userRepository.save(user));
    }

    /**
     * Get user by ID with DTO response.
     */
    @Transactional(readOnly = true)
    public UserResponseDto getUserByIdDto(Long userId) {
        return mapToResponseDto(requireUser(userId));
    }

    /**
     * Get all users with pagination, filtering, and search.
     */
    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(UserQuery query) {
        Pageable pageable = buildPageable(query.page(), query.size(), query.sortBy(), query.sortDirection());
        Specification<User> spec = buildUserFilterSpec(query);
        return userRepository.findAll(spec, pageable).map(this::mapToResponseDto);
    }

    /**
     * Get user statistics.
     */
    @Transactional(readOnly = true)
    public UserStatisticsDto getUserStatistics() {
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
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setRoleId(user.getRoleEntity() != null ? user.getRoleEntity().getId() : null);
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setEnabled(user.isEnabled());
        dto.setIsActive(user.getIsActive());
        dto.setAccountLocked(user.isAccountLocked());
        dto.setAccountStatus(user.getAccountStatus());
        dto.setFailedLoginAttempts(user.getFailedLoginAttempts());
        dto.setLoginAttempt(user.getLoginAttempt());
        dto.setLastLoginDate(user.getLastLoginDate());
        dto.setLastPasswordResetDate(user.getLastPasswordResetDate());
        dto.setPasswordUpdateDate(user.getPasswordUpdateDate());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private boolean defaultTrue(Boolean value) {
        return value == null || value;
    }

    private User.AccountStatus defaultAccountStatus(User.AccountStatus value) {
        return value == null ? User.AccountStatus.ACTIVE : value;
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDirection) {
        Sort sort;
        if (sortDirection != null && sortDirection.equalsIgnoreCase("DESC")) {
            sort = Sort.by(sortBy).descending();
        } else {
            sort = Sort.by(sortBy).ascending();
        }
        return PageRequest.of(page, size, sort);
    }

    private Specification<User> buildUserFilterSpec(UserQuery query) {
        Specification<User> spec = Specification.allOf();

        if (query.role() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("role"), query.role()));
        }
        if (query.status() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("accountStatus"), query.status()));
        }
        if (query.enabled() != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("enabled"), query.enabled()));
        }
        if (query.search() != null && !query.search().trim().isEmpty()) {
            String pattern = "%" + query.search().toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.or(
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(cb.lower(root.get("fullName")), pattern)
            ));
        }

        return spec;
    }

    private static <T> void setIfPresent(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    /**
     * Converts the DTO response back to an entity by reloading it.
     * Keeps legacy methods compatible without duplicating logic.
     */
    private User toEntity(UserResponseDto dto) {
        return requireUser(dto.getId());
    }
}
