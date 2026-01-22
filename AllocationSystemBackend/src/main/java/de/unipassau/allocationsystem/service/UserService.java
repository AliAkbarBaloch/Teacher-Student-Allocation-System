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
import lombok.Getter;
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
     * Query container to avoid long parameter lists.
     */
    @Getter
    public static class UserQuery {
        private final User.UserRole role;
        private final User.AccountStatus status;
        private final Boolean enabled;
        private final String search;
        private final int page;
        private final int size;
        private final String sortBy;
        private final String sortDirection;

        public UserQuery(
                User.UserRole role,
                User.AccountStatus status,
                Boolean enabled,
                String search,
                int page,
                int size,
                String sortBy,
                String sortDirection
        ) {
            this.role = role;
            this.status = status;
            this.enabled = enabled;
            this.search = search;
            this.page = page;
            this.size = size;
            this.sortBy = sortBy;
            this.sortDirection = sortDirection;
        }
    }

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
        log.info("Creating new user: {}", email);

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setEnabled(true);

        return userRepository.save(user);
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
        log.info("Updating user: {}", userId);

        User user = requireUser(userId);
        user.setEmail(newEmail);
        user.setFullName(newFullName);

        return userRepository.save(user);
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
        log.info("Deleting user: {}", userId);
        User user = requireUser(userId);
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
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("Changing password for user: {}", userId);

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
        log.info("Setting user {} enabled status to: {}", userId, enabled);

        User user = requireUser(userId);
        user.setEnabled(enabled);

        return userRepository.save(user);
    }

    // ========== NEW COMPREHENSIVE CRUD METHODS ==========

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
        log.info("Updating user with DTO: {}", userId);

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
        log.info("Activating user: {}", userId);

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
        log.info("Deactivating user: {}", userId);

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
        log.info("Admin resetting password for user: {}", userId);

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
        log.info("Getting users - role: {}, status: {}, enabled: {}, search: {}",
                query.getRole(), query.getStatus(), query.getEnabled(), query.getSearch());

        Pageable pageable = buildPageable(
                query.getPage(),
                query.getSize(),
                query.getSortBy(),
                query.getSortDirection()
        );

        Specification<User> spec = Specification.allOf();
        spec = andRole(spec, query.getRole());
        spec = andStatus(spec, query.getStatus());
        spec = andEnabled(spec, query.getEnabled());
        spec = andSearch(spec, query.getSearch());

        return userRepository.findAll(spec, pageable).map(this::mapToResponseDto);
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
        if (value == null) {
            return true;
        }
        return value;
    }

    private User.AccountStatus defaultAccountStatus(User.AccountStatus value) {
        if (value == null) {
            return User.AccountStatus.ACTIVE;
        }
        return value;
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

    private Specification<User> andRole(Specification<User> spec, User.UserRole role) {
        if (role == null) {
            return spec;
        }
        return spec.and((root, query, cb) -> cb.equal(root.get("role"), role));
    }

    private Specification<User> andStatus(Specification<User> spec, User.AccountStatus status) {
        if (status == null) {
            return spec;
        }
        return spec.and((root, query, cb) -> cb.equal(root.get("accountStatus"), status));
    }

    private Specification<User> andEnabled(Specification<User> spec, Boolean enabled) {
        if (enabled == null) {
            return spec;
        }
        return spec.and((root, query, cb) -> cb.equal(root.get("enabled"), enabled));
    }

    private Specification<User> andSearch(Specification<User> spec, String search) {
        if (search == null || search.trim().isEmpty()) {
            return spec;
        }
        String pattern = "%" + search.toLowerCase() + "%";
        return spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("email")), pattern),
                cb.like(cb.lower(root.get("fullName")), pattern)
        ));
    }

    private static <T> void setIfPresent(T value, java.util.function.Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}
