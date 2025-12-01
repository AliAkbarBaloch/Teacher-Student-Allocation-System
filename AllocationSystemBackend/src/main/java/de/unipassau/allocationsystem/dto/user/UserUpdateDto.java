package de.unipassau.allocationsystem.dto.user;

import de.unipassau.allocationsystem.entity.User;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating an existing user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {

    @Email(message = "Email should be valid")
    private String email;

    private String fullName;

    private User.UserRole role;

    private Long roleId;

    private String phoneNumber;

    private Boolean enabled;

    private Boolean isActive;

    private User.AccountStatus accountStatus;
}
