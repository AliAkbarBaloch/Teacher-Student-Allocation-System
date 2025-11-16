package de.unipassau.allocationsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user login response containing JWT token and user information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDto {

    private String token;
    
    @Builder.Default
    private String tokenType = "Bearer";
    
    private Long userId;
    private String email;
    private String fullName;
    private String role;
}
