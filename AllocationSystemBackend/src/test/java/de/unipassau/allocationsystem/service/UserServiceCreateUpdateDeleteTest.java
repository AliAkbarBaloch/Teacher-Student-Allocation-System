package de.unipassau.allocationsystem.service;

import de.unipassau.allocationsystem.dto.user.UserResponseDto;
import de.unipassau.allocationsystem.entity.User;
import de.unipassau.allocationsystem.exception.DuplicateResourceException;
import de.unipassau.allocationsystem.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserService} covering create/update/delete operations.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceCreateUpdateDeleteTest extends UserServiceTestBase {

    @Test
    void createUserSuccess() {
        String email = "test@example.com";
        String rawPassword = secret();
        String fullName = "Test User";
        String encodedPassword = "encoded-" + rawPassword;

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.createUser(email, rawPassword, fullName);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());
        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUserWithDtoSuccess() {
        User newUser = new User();
        newUser.setId(2L);
        newUser.setEmail(createDto.getEmail());
        newUser.setPassword("encodedPassword");
        newUser.setFullName(createDto.getFullName());
        newUser.setRole(createDto.getRole());
        newUser.setPhoneNumber(createDto.getPhoneNumber());
        newUser.setEnabled(true);
        newUser.setAccountStatus(User.AccountStatus.ACTIVE);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());

        when(userRepository.existsByEmail(createDto.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(createDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        UserResponseDto result = userService.createUserWithDto(createDto);

        assertNotNull(result);
        assertEquals(newUser.getId(), result.getId());
        assertEquals(createDto.getEmail(), result.getEmail());
        assertEquals(createDto.getFullName(), result.getFullName());
        assertEquals(createDto.getRole(), result.getRole());
        verify(userRepository).existsByEmail(createDto.getEmail());
        verify(passwordEncoder).encode(createDto.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUserWithDtoDuplicateEmailThrowsException() {
        when(userRepository.existsByEmail(createDto.getEmail())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.createUserWithDto(createDto));

        verify(userRepository).existsByEmail(createDto.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserSuccess() {
        Long userId = 1L;
        String newEmail = "newemail@example.com";
        String newFullName = "New Full Name";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.updateUser(userId, newEmail, newFullName);

        assertNotNull(result);
        verify(userRepository).findById(userId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUserUserNotFoundThrowsException() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.updateUser(userId, "x@example.com", "Name"));

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserWithDtoSuccess() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(updateDto.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserResponseDto result = userService.updateUserWithDto(testUser.getId(), updateDto);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        verify(userRepository).findById(testUser.getId());
        verify(userRepository).existsByEmail(updateDto.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUserWithDtoUserNotFoundThrowsException() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUserWithDto(userId, updateDto));

        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserWithDtoDuplicateEmailThrowsException() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(updateDto.getEmail())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> userService.updateUserWithDto(testUser.getId(), updateDto));

        verify(userRepository).findById(testUser.getId());
        verify(userRepository).existsByEmail(updateDto.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUserSuccess() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);

        userService.deleteUser(userId);

        verify(userRepository).findById(userId);
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUserUserNotFoundThrowsException() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(userId));

        verify(userRepository).findById(userId);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void getUserByIdUserExistsReturnsUser() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.getUserById(userId);

        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        assertEquals(testUser.getEmail(), result.get().getEmail());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserByIdUserNotFoundReturnsEmpty() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(userId);

        assertFalse(result.isPresent());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserByIdDtoSuccess() {
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        UserResponseDto result = userService.getUserByIdDto(testUser.getId());

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getFullName(), result.getFullName());
        verify(userRepository).findById(testUser.getId());
    }

    @Test
    void getUserByIdDtoUserNotFoundThrowsException() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserByIdDto(userId));

        verify(userRepository).findById(userId);
    }
}
