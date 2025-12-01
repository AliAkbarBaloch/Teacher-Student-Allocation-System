package de.unipassau.allocationsystem.dto.user;

import de.unipassau.allocationsystem.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for user response (returned to clients).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {

    private Long id;
    private Long roleId;
    private String email;
    private String fullName;
    private User.UserRole role;
    private String phoneNumber;
    private boolean enabled;
    private Boolean isActive;
    private boolean accountLocked;
    private User.AccountStatus accountStatus;
    private int failedLoginAttempts;
    private Integer loginAttempt;
    private LocalDateTime lastLoginDate;
    private LocalDateTime lastPasswordResetDate;
    private LocalDateTime passwordUpdateDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
