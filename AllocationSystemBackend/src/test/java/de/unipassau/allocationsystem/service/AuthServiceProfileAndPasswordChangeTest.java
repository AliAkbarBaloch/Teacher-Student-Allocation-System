package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.dto.auth.PasswordChangeRequestDto;
import de.unipassau.allocationsystem.dto.user.UserProfileUpdateRequest;
import de.unipassau.allocationsystem.dto.user.UserResponseDto;
import de.unipassau.allocationsystem.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthService} password change and profile update operations.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceProfileAndPasswordChangeTest extends AuthServiceTestBase {

    /**
     * Verifies that a correct current password allows changing to a new password.
     */
    @Test
    void changePasswordSuccess() {
        UserDetails principal = userDetailsFrom(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );
        setAuthenticated(authentication);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        String currentPassword = testSecret();
        when(passwordEncoder.matches(currentPassword, user.getPassword())).thenReturn(true);

        PasswordChangeRequestDto req = changeRequest(currentPassword, testSecret());

        authService.changePassword(req);

        verify(userRepository).save(any(User.class));
        verify(auditLogService).logAction(any(), anyString(), anyString());
    }

    /**
     * Verifies that an incorrect current password causes an exception.
     */
    @Test
    void changePasswordIncorrectCurrentThrows() {
        UserDetails principal = userDetailsFrom(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );
        setAuthenticated(authentication);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        String wrongCurrent = testSecret();
        when(passwordEncoder.matches(wrongCurrent, user.getPassword())).thenReturn(false);

        PasswordChangeRequestDto req = changeRequest(wrongCurrent, testSecret());

        assertThrows(IllegalArgumentException.class, () -> authService.changePassword(req));
    }

    /**
     * Verifies that updating profile information persists the changes.
     */
    @Test
    void updateProfileSuccess() {
        UserDetails principal = userDetailsFrom(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );
        setAuthenticated(authentication);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserProfileUpdateRequest req = profileRequest("new@example.com", "New Name", "123");

        UserResponseDto dto = authService.updateProfile(req);

        assertNotNull(dto);
        verify(userRepository).save(any(User.class));
        verify(auditLogService).logAction(any(), anyString(), anyString());
    }

    /**
     * Verifies that attempting to update to an already-taken email fails.
     */
    @Test
    void updateProfileDuplicateEmailThrows() {
        UserDetails principal = userDetailsFrom(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );
        setAuthenticated(authentication);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(new User()));

        // UserProfileUpdateRequest requires BOTH fullName and email (both @NotBlank)
        UserProfileUpdateRequest req = profileRequest("taken@example.com", "Some Name", null);

        assertThrows(IllegalArgumentException.class, () -> authService.updateProfile(req));
    }
}
