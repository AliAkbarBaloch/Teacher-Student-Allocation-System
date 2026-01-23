package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.dto.user.UserResponseDto;
import de.unipassau.allocationsystem.dto.user.UserStatisticsDto;
import de.unipassau.allocationsystem.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserService} covering querying and statistics.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceQueryAndStatsTest extends UserServiceTestBase {

    @Test
    void getAllUsersWithoutFiltersReturnsPage() {
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);

        UserQuery query = new UserQuery.Builder()
                .page(0)
                .size(10)
                .sortBy("id")
                .sortDirection("ASC")
                .build();

        Page<UserResponseDto> result = userService.getAllUsers(query);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAllUsersWithRoleFilterReturnsFilteredPage() {
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);

        UserQuery query = new UserQuery.Builder()
                .role(User.UserRole.USER)
                .page(0)
                .size(10)
                .sortBy("id")
                .sortDirection("ASC")
                .build();

        Page<UserResponseDto> result = userService.getAllUsers(query);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAllUsersWithStatusFilterReturnsFilteredPage() {
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);

        UserQuery query = new UserQuery.Builder()
                .status(User.AccountStatus.ACTIVE)
                .page(0)
                .size(10)
                .sortBy("id")
                .sortDirection("ASC")
                .build();

        Page<UserResponseDto> result = userService.getAllUsers(query);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAllUsersWithEnabledFilterReturnsFilteredPage() {
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);

        UserQuery query = new UserQuery.Builder()
                .enabled(true)
                .page(0)
                .size(10)
                .sortBy("id")
                .sortDirection("ASC")
                .build();

        Page<UserResponseDto> result = userService.getAllUsers(query);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAllUsersWithSearchQueryReturnsFilteredPage() {
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);

        UserQuery query = new UserQuery.Builder()
                .search("test")
                .page(0)
                .size(10)
                .sortBy("id")
                .sortDirection("ASC")
                .build();

        Page<UserResponseDto> result = userService.getAllUsers(query);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAllUsersWithAllFiltersReturnsFilteredPage() {
        Page<User> userPage = new PageImpl<>(List.of(testUser));
        when(userRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(userPage);

        UserQuery query = new UserQuery.Builder()
                .role(User.UserRole.USER)
                .status(User.AccountStatus.ACTIVE)
                .enabled(true)
                .search("test")
                .page(0)
                .size(10)
                .sortBy("email")
                .sortDirection("DESC")
                .build();

        Page<UserResponseDto> result = userService.getAllUsers(query);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void getUserStatisticsSuccess() {
        when(userRepository.count()).thenReturn(100L);
        when(userRepository.countByAccountStatus(User.AccountStatus.ACTIVE)).thenReturn(80L);
        when(userRepository.countByAccountStatus(User.AccountStatus.INACTIVE)).thenReturn(15L);
        when(userRepository.countByAccountStatus(User.AccountStatus.SUSPENDED)).thenReturn(5L);
        when(userRepository.countByAccountLocked(true)).thenReturn(3L);
        when(userRepository.countByRole(User.UserRole.ADMIN)).thenReturn(10L);
        when(userRepository.countByRole(User.UserRole.USER)).thenReturn(90L);

        UserStatisticsDto result = userService.getUserStatistics();

        assertNotNull(result);
        assertEquals(100L, result.getTotalUsers());
        assertEquals(80L, result.getActiveUsers());
        assertEquals(15L, result.getInactiveUsers());
        assertEquals(5L, result.getSuspendedUsers());
        assertEquals(3L, result.getLockedUsers());
        assertEquals(10L, result.getAdminUsers());
        assertEquals(90L, result.getRegularUsers());

        verify(userRepository).count();
        verify(userRepository, times(3)).countByAccountStatus(any(User.AccountStatus.class));
        verify(userRepository).countByAccountLocked(true);
        verify(userRepository, times(2)).countByRole(any(User.UserRole.class));
    }

    @Test
    void getUserStatisticsNoUsersReturnsZeroStatistics() {
        when(userRepository.count()).thenReturn(0L);
        when(userRepository.countByAccountStatus(any(User.AccountStatus.class))).thenReturn(0L);
        when(userRepository.countByAccountLocked(anyBoolean())).thenReturn(0L);
        when(userRepository.countByRole(any(User.UserRole.class))).thenReturn(0L);

        UserStatisticsDto result = userService.getUserStatistics();

        assertNotNull(result);
        assertEquals(0L, result.getTotalUsers());
        assertEquals(0L, result.getActiveUsers());
        assertEquals(0L, result.getInactiveUsers());
        assertEquals(0L, result.getSuspendedUsers());
        assertEquals(0L, result.getLockedUsers());
        assertEquals(0L, result.getAdminUsers());
        assertEquals(0L, result.getRegularUsers());
    }
}
