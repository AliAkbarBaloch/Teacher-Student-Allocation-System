package de.unipassau.allocationsystem.dto;

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

    private String phoneNumber;

    private Boolean enabled;

    private User.AccountStatus accountStatus;
}
