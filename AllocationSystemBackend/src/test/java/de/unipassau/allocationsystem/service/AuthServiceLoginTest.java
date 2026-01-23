package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.dto.auth.LoginRequestDto;
import de.unipassau.allocationsystem.dto.auth.LoginResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import de.unipassau.allocationsystem.entity.User;


import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthService} login behavior.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceLoginTest extends AuthServiceTestBase {

    /**
     * Verifies that a valid login produces a token response and updates user state.
     */
    @Test
    void loginSuccess() {
        LoginRequestDto req = loginRequest(user.getEmail(), testSecret());

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        UserDetails principal = userDetailsFrom(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("token-xyz");

        LoginResponseDto resp = authService.login(req);

        assertNotNull(resp);
        assertEquals(user.getEmail(), resp.getEmail());
        assertEquals(user.getId(), resp.getUserId());
        verify(userRepository).save(any(User.class));
        verify(auditLogService).logAction(any(), anyString(), anyString());
    }

    /**
     * Verifies that bad credentials increment failed attempts and an exception is thrown.
     */
    @Test
    void loginBadCredentialsIncrementsAttemptsAndThrows() {
        LoginRequestDto req = loginRequest(user.getEmail(), testSecret());

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThrows(BadCredentialsException.class, () -> authService.login(req));

        verify(userRepository).save(any(User.class));
        verify(auditLogService).logAction(any(), anyString(), anyString());
    }
}
