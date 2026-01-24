package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.dto.auth.PasswordForgotRequestDto;
import de.unipassau.allocationsystem.dto.auth.PasswordResetRequestDto;
import de.unipassau.allocationsystem.entity.PasswordResetToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import de.unipassau.allocationsystem.entity.User;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthService} password reset flows (forgot + reset).
 */
@ExtendWith(MockitoExtension.class)
class AuthServicePasswordResetTest extends AuthServiceTestBase {

    /**
     * Verifies that requesting a reset for an unknown email does not create tokens or send mail.
     */
    @Test
    void forgotPasswordNonExistentDoesNothing() {
        PasswordForgotRequestDto req = forgotRequest("nope@example.com");

        when(userRepository.findByEmail("nope@example.com")).thenReturn(Optional.empty());

        authService.forgotPassword(req);

        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }

    /**
     * Verifies that a valid reset token updates the user's password and marks the token as used.
     */
    @Test
    void resetPasswordSuccess() {
        String tokenValue = "tok-123";
        PasswordResetToken prt = validResetToken(tokenValue, user);

        when(tokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(prt));

        String newPassword = testSecret();
        when(passwordEncoder.encode(newPassword)).thenReturn("enc-new");

        PasswordResetRequestDto req = resetRequest(tokenValue, newPassword);

        authService.resetPassword(req);

        verify(userRepository).save(any(User.class));
        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(auditLogService).logAction(any(), anyString(), anyString());
    }

    /**
     * Verifies that an unknown token results in an exception.
     */
    @Test
    void resetPasswordInvalidTokenThrows() {
        when(tokenRepository.findByToken("bad")).thenReturn(Optional.empty());

        PasswordResetRequestDto req = resetRequest("bad", testSecret());

        assertThrows(IllegalArgumentException.class, () -> authService.resetPassword(req));
    }
}
