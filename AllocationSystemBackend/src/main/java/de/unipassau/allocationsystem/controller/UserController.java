package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.*;
import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for comprehensive user management operations.
 * All endpoints require ADMIN role.
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "Users management APIs")
public class UserController {

    private final UserService userService;

    /**
     * Create a new user.
     * POST /api/users
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserCreateDto dto) {
        log.info("Creating user: {}", dto.getEmail());
        UserResponseDto created = userService.createUserWithDto(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update an existing user.
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDto dto
    ) {
        log.info("Updating user: {}", id);
        UserResponseDto updated = userService.updateUserWithDto(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a user.
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Deleting user: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get user by ID.
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
        log.info("Getting user by id: {}", id);
        UserResponseDto user = userService.getUserByIdDto(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Get all users with pagination, filtering, and search.
     * GET /api/users
     *
     * Query parameters:
     * - role: Filter by user role (USER, ADMIN, MODERATOR)
     * - status: Filter by account status (ACTIVE, INACTIVE, SUSPENDED, PENDING_VERIFICATION)
     * - enabled: Filter by enabled status (true/false)
     * - search: Search in email and full name
     * - page: Page number (default: 0)
     * - size: Page size (default: 20)
     * - sortBy: Sort field (default: createdAt)
     * - sortDirection: Sort direction (ASC/DESC, default: DESC)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(
            @RequestParam(required = false) User.UserRole role,
            @RequestParam(required = false) User.AccountStatus status,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        log.info("Getting all users with filters");
        Page<UserResponseDto> users = userService.getAllUsers(
                role, status, enabled, search, page, size, sortBy, sortDirection
        );
        return ResponseEntity.ok(users);
    }

    /**
     * Activate a user account.
     * PATCH /api/users/{id}/activate
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> activateUser(@PathVariable Long id) {
        log.info("Activating user: {}", id);
        UserResponseDto activated = userService.activateUser(id);
        return ResponseEntity.ok(activated);
    }

    /**
     * Deactivate a user account.
     * PATCH /api/users/{id}/deactivate
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> deactivateUser(@PathVariable Long id) {
        log.info("Deactivating user: {}", id);
        UserResponseDto deactivated = userService.deactivateUser(id);
        return ResponseEntity.ok(deactivated);
    }

    /**
     * Reset user password (admin function).
     * POST /api/users/{id}/reset-password
     */
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody PasswordResetDto dto
    ) {
        log.info("Resetting password for user: {}", id);
        UserResponseDto user = userService.resetUserPassword(id, dto);
        return ResponseEntity.ok(user);
    }

    /**
     * Get user statistics.
     * GET /api/users/statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserStatisticsDto> getUserStatistics() {
        log.info("Getting user statistics");
        UserStatisticsDto stats = userService.getUserStatistics();
        return ResponseEntity.ok(stats);
    }
}
