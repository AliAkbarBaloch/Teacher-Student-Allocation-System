package de.unipassau.allocationsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user statistics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsDto {

    private Long totalUsers;
    private Long activeUsers;
    private Long inactiveUsers;
    private Long suspendedUsers;
    private Long lockedUsers;
    private Long adminUsers;
    private Long regularUsers;
}
