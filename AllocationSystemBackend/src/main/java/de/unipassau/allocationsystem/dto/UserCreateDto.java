package de.unipassau.allocationsystem.dto;

import de.unipassau.allocationsystem.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotNull(message = "Role is required")
    private User.UserRole role;

    private String phoneNumber;

    private Boolean enabled = true;

    private User.AccountStatus accountStatus = User.AccountStatus.ACTIVE;
}
