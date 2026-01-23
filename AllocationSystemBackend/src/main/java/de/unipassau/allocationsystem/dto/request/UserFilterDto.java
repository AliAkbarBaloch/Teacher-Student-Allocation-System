package de.unipassau.allocationsystem.dto.request;

import de.unipassau.allocationsystem.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for filtering and paginating users.
 * Consolidates multiple request parameters into a single object.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFilterDto {
    
    private User.UserRole role;
    
    private User.AccountStatus status;
    
    private Boolean enabled;
    
    private String search;
    
    @Builder.Default
    private int page = 0;
    
    @Builder.Default
    private int size = 20;
    
    @Builder.Default
    private String sortBy = "createdAt";
    
    @Builder.Default
    private String sortDirection = "DESC";
}
