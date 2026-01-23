package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.dto.user.UserResponseDto;
import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserService} covering password and account status operations.
 */
@ExtendWith(MockitoExtension.class)
class UserServicePasswordAndStatusTest extends UserServiceTestBase {

    @Test
    void changePasswordSuccess() {
        Long userId = 1L;
        String oldPassword = secret();
        String newPassword = secret();
        String encodedNewPassword = "encoded-" + newPassword;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(oldPassword, "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);

        userService.changePassword(userId, oldPassword, newPassword);

        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(oldPassword, "encodedPassword");
        verify(passwordEncoder).encode(newPassword);
    }

    @Test
    void changePasswordUserNotFoundThrowsException() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.changePassword(userId, secret(), secret()));

        verify(userRepository).findById(userId);
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void changePasswordInvalidOldPasswordThrowsException() {
        Long userId = 1L;
        String oldPassword = secret();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(oldPassword, "encodedPassword")).thenReturn(false);

        assertThrows(RuntimeException.class, () -> userService.changePassword(userId, oldPassword, secret()));

        verify(userRepository).findById(userId);
        verify(passwordEncoder).matches(oldPassword, "encodedPassword");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void resetUserPasswordSuccess() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(passwordResetDto.getNewPassword())).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponseDto result = userService.resetUserPassword(testUser.getId(), passwordResetDto);

        assertNotNull(result);
        verify(userRepository).findById(testUser.getId());
        verify(passwordEncoder).encode(passwordResetDto.getNewPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void resetUserPasswordUserNotFoundThrowsException() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.resetUserPassword(userId, passwordResetDto));

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void setUserEnabledEnableUserSuccess() {
        Long userId = 1L;
        testUser.setEnabled(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.setUserEnabled(userId, true);

        assertNotNull(result);
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void setUserEnabledDisableUserSuccess() {
        Long userId = 1L;
        testUser.setEnabled(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.setUserEnabled(userId, false);

        assertNotNull(result);
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void setUserEnabledUserNotFoundThrowsException() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.setUserEnabled(userId, true));

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void activateUserSuccess() {
        testUser.setEnabled(false);
        testUser.setAccountStatus(User.AccountStatus.INACTIVE);
        testUser.setAccountLocked(true);
        testUser.setFailedLoginAttempts(3);

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponseDto result = userService.activateUser(testUser.getId());

        assertNotNull(result);
        verify(userRepository).findById(testUser.getId());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void activateUserUserNotFoundThrowsException() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.activateUser(userId));

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deactivateUserSuccess() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponseDto result = userService.deactivateUser(testUser.getId());

        assertNotNull(result);
        verify(userRepository).findById(testUser.getId());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deactivateUserUserNotFoundThrowsException() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.deactivateUser(userId));

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }
}
