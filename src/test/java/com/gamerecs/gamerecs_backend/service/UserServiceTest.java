package com.gamerecs.gamerecs_backend.service;

import java.sql.Timestamp;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.gamerecs.gamerecs_backend.dto.UserProfileDTO;
import com.gamerecs.gamerecs_backend.dto.UserRegistrationDTO;
import com.gamerecs.gamerecs_backend.exception.UserRegistrationException;
import com.gamerecs.gamerecs_backend.model.User;
import com.gamerecs.gamerecs_backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRegistrationDTO validRegistrationDTO;
    private User validUser;

    @BeforeEach
    void setUp() {
        // Setup test data
        validRegistrationDTO = new UserRegistrationDTO();
        validRegistrationDTO.setUsername("testuser");
        validRegistrationDTO.setEmail("test@example.com");
        validRegistrationDTO.setPassword("password123");
        validRegistrationDTO.setConfirmPassword("password123");
        validRegistrationDTO.setBio("Test bio");
        validRegistrationDTO.setProfilePictureURL("http://example.com/pic.jpg");

        validUser = new User();
        validUser.setUsername("testuser");
        validUser.setEmail("test@example.com");
        validUser.setPasswordHash("hashedPassword");
        validUser.setBio("Test bio");
        validUser.setProfilePictureURL("http://example.com/pic.jpg");
        validUser.setJoinDate(new Timestamp(System.currentTimeMillis()));
    }

    @Test
    @DisplayName("Should successfully register a new user")
    void registerUser_WithValidData_ShouldSucceed() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(validUser);

        // Act
        User result = userService.registerUser(validRegistrationDTO);

        // Assert
        assertNotNull(result);
        assertEquals(validRegistrationDTO.getUsername(), result.getUsername());
        assertEquals(validRegistrationDTO.getEmail(), result.getEmail());
        assertEquals(validRegistrationDTO.getBio(), result.getBio());
        assertEquals(validRegistrationDTO.getProfilePictureURL(), result.getProfilePictureURL());

        verify(userRepository).existsByUsername(validRegistrationDTO.getUsername());
        verify(userRepository).existsByEmail(validRegistrationDTO.getEmail());
        verify(passwordEncoder).encode(validRegistrationDTO.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when passwords don't match")
    void registerUser_WithMismatchedPasswords_ShouldThrowException() {
        // Arrange
        validRegistrationDTO.setConfirmPassword("differentPassword");

        // Act & Assert
        assertThrows(UserRegistrationException.class, () -> userService.registerUser(validRegistrationDTO));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void registerUser_WithExistingUsername_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByUsername(validRegistrationDTO.getUsername())).thenReturn(true);

        // Act & Assert
        assertThrows(UserRegistrationException.class, () -> userService.registerUser(validRegistrationDTO));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void registerUser_WithExistingEmail_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(validRegistrationDTO.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(UserRegistrationException.class, () -> userService.registerUser(validRegistrationDTO));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should load user details by username successfully")
    void loadUserByUsername_WithValidUsername_ShouldSucceed() {
        // Arrange
        when(userRepository.findByUsername(validUser.getUsername()))
                .thenReturn(Optional.of(validUser));

        // Act
        UserDetails result = userService.loadUserByUsername(validUser.getUsername());

        // Assert
        assertNotNull(result);
        assertEquals(validUser.getUsername(), result.getUsername());
        assertEquals(validUser.getPasswordHash(), result.getPassword());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    @DisplayName("Should throw exception when username not found")
    void loadUserByUsername_WithInvalidUsername_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername(anyString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("nonexistent"));
    }

    @Test
    @DisplayName("Should get user profile successfully")
    void getUserProfile_WithValidUsername_ShouldSucceed() {
        // Arrange
        when(userRepository.findByUsername(validUser.getUsername()))
                .thenReturn(Optional.of(validUser));

        // Act
        UserProfileDTO result = userService.getUserProfile(validUser.getUsername());

        // Assert
        assertNotNull(result);
        assertEquals(validUser.getUsername(), result.getUsername());
        assertEquals(validUser.getEmail(), result.getEmail());
        assertEquals(validUser.getBio(), result.getBio());
        assertEquals(validUser.getProfilePictureURL(), result.getProfilePictureURL());
        assertNotNull(result.getJoinDate());
    }

    @Test
    @DisplayName("Should throw exception when getting profile of non-existent user")
    void getUserProfile_WithInvalidUsername_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername(anyString()))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> userService.getUserProfile("nonexistent"));
    }
}