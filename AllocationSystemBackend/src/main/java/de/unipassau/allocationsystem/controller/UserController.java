package de.unipassau.allocationsystem.controller;

import de.unipassau.allocationsystem.dto.auth.PasswordResetDto;
import de.unipassau.allocationsystem.dto.request.UserFilterDto;
import de.unipassau.allocationsystem.dto.user.UserCreateDto;
import de.unipassau.allocationsystem.dto.user.UserResponseDto;
import de.unipassau.allocationsystem.dto.user.UserStatisticsDto;
import de.unipassau.allocationsystem.dto.user.UserUpdateDto;
import de.unipassau.allocationsystem.service.UserService;
import de.unipassau.allocationsystem.utils.ResponseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @Operation(summary = "Create user", description = "Create a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserCreateDto dto) {
        log.info("Creating user: {}", dto.getEmail());
        UserResponseDto created = userService.createUserWithDto(dto);
        return ResponseHandler.created("User created successfully", created);
    }

    /**
     * Update an existing user.
     * PUT /api/users/{id}
     */
        @Operation(summary = "Update user", description = "Update an existing user")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        @PutMapping("/{id}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDto dto
        ) {
        log.info("Updating user: {}", id);
        UserResponseDto updated = userService.updateUserWithDto(id, dto);
        return ResponseHandler.updated("User updated successfully", updated);
        }

    /**
     * Delete a user.
     * DELETE /api/users/{id}
     */
    @Operation(summary = "Delete user", description = "Delete a user by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        log.info("Deleting user: {}", id);
        userService.deleteUser(id);
        return ResponseHandler.noContent();
    }

    /**
     * Get user by ID.
     * GET /api/users/{id}
     */
    @Operation(summary = "Get user by ID", description = "Retrieve a user by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        log.info("Getting user by id: {}", id);
        UserResponseDto user = userService.getUserByIdDto(id);
        return ResponseHandler.success("User retrieved successfully", user);
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
        @Operation(summary = "Get all users", description = "Retrieve users with pagination and filters")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid pagination parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        @GetMapping
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> getAllUsers(
            @ModelAttribute UserFilterDto filterDto
    ) {
        log.info("Getting all users with filters");
        Page<UserResponseDto> users = userService.getAllUsers(
            filterDto.getRole(), filterDto.getStatus(), filterDto.getEnabled(), filterDto.getSearch(), 
            filterDto.getPage(), filterDto.getSize(), filterDto.getSortBy(), filterDto.getSortDirection()
        );
        return ResponseHandler.success("Users retrieved successfully", users);
    }

    /**
     * Activate a user account.
     * PATCH /api/users/{id}/activate
     */
    @Operation(summary = "Activate user", description = "Activate a user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User activated successfully",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> activateUser(@PathVariable Long id) {
        log.info("Activating user: {}", id);
        UserResponseDto activated = userService.activateUser(id);
        return ResponseHandler.updated("User activated successfully", activated);
    }

    /**
     * Deactivate a user account.
     * PATCH /api/users/{id}/deactivate
     */
    @Operation(summary = "Deactivate user", description = "Deactivate a user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deactivated successfully",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deactivateUser(@PathVariable Long id) {
        log.info("Deactivating user: {}", id);
        UserResponseDto deactivated = userService.deactivateUser(id);
        return ResponseHandler.updated("User deactivated successfully", deactivated);
    }

    /**
     * Reset user password (admin function).
     * POST /api/users/{id}/reset-password
     */
        @Operation(summary = "Reset user password", description = "Reset a user's password (admin)")
        @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successfully",
                content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        @PostMapping("/{id}/reset-password")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<?> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody PasswordResetDto dto
        ) {
        log.info("Resetting password for user: {}", id);
        UserResponseDto user = userService.resetUserPassword(id, dto);
        return ResponseHandler.updated("Password reset successfully", user);
        }

    /**
     * Get user statistics.
     * GET /api/users/statistics
     */
    @Operation(summary = "Get user statistics", description = "Retrieve aggregated user statistics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserStatisticsDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getUserStatistics() {
        log.info("Getting user statistics");
        UserStatisticsDto stats = userService.getUserStatistics();
        return ResponseHandler.success("User statistics retrieved successfully", stats);
    }
}
