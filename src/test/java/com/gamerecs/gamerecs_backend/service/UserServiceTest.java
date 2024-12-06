package com.gamerecs.gamerecs_backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.gamerecs.gamerecs_backend.dto.UserRegistrationDTO;
import com.gamerecs.gamerecs_backend.exception.UserRegistrationException;
import com.gamerecs.gamerecs_backend.model.User;
import com.gamerecs.gamerecs_backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private UserRegistrationDTO validRegistrationDTO;
    private static final String ENCODED_PASSWORD = "encodedPassword123";

    @BeforeEach
    void setUp() {
        // Set up a valid registration DTO
        validRegistrationDTO = new UserRegistrationDTO();
        validRegistrationDTO.setUsername("testuser");
        validRegistrationDTO.setEmail("test@example.com");
        validRegistrationDTO.setPassword("Password123");
        validRegistrationDTO.setConfirmPassword("Password123");
        validRegistrationDTO.setBio("Test bio");
        validRegistrationDTO.setProfilePictureURL("http://example.com/pic.jpg");
    }

    @Test
    void whenValidRegistration_thenSucceed() {
        // Given
        when(userRepository.existsByUsername(validRegistrationDTO.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(validRegistrationDTO.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(validRegistrationDTO.getPassword())).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User registeredUser = userService.registerUser(validRegistrationDTO);

        // Then
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertThat(registeredUser).isNotNull();
        assertThat(capturedUser.getUsername()).isEqualTo(validRegistrationDTO.getUsername());
        assertThat(capturedUser.getEmail()).isEqualTo(validRegistrationDTO.getEmail());
        assertThat(capturedUser.getPasswordHash()).isEqualTo(ENCODED_PASSWORD);
        assertThat(capturedUser.getBio()).isEqualTo(validRegistrationDTO.getBio());
        assertThat(capturedUser.getProfilePictureURL()).isEqualTo(validRegistrationDTO.getProfilePictureURL());
        assertThat(capturedUser.getJoinDate()).isNotNull();
        verify(passwordEncoder).encode(validRegistrationDTO.getPassword());
    }

    @Test
    void whenRegisteringUser_thenPasswordIsHashedBeforeSaving() {
        // Given
        String rawPassword = "Password123";
        validRegistrationDTO.setPassword(rawPassword);
        validRegistrationDTO.setConfirmPassword(rawPassword);
        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User registeredUser = userService.registerUser(validRegistrationDTO);

        // Then
        verify(passwordEncoder).encode(rawPassword);
        assertThat(registeredUser.getPasswordHash()).isEqualTo(ENCODED_PASSWORD);
        assertThat(registeredUser.getPasswordHash()).isNotEqualTo(rawPassword);
    }

    @Test
    void whenPasswordsDontMatch_thenThrowException() {
        // Given
        validRegistrationDTO.setConfirmPassword("DifferentPassword123");

        // When/Then
        UserRegistrationException exception = assertThrows(UserRegistrationException.class,
                () -> userService.registerUser(validRegistrationDTO));

        assertThat(exception.getMessage()).isEqualTo("Passwords do not match");
        verify(userRepository, never()).save(any());
    }

    @Test
    void whenUsernameExists_thenThrowException() {
        // Given
        when(userRepository.existsByUsername(validRegistrationDTO.getUsername())).thenReturn(true);

        // When/Then
        UserRegistrationException exception = assertThrows(UserRegistrationException.class,
                () -> userService.registerUser(validRegistrationDTO));

        assertThat(exception.getMessage()).isEqualTo("Username already exists");
        verify(userRepository, never()).save(any());
    }

    @Test
    void whenEmailExists_thenThrowException() {
        // Given
        when(userRepository.existsByUsername(validRegistrationDTO.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(validRegistrationDTO.getEmail())).thenReturn(true);

        // When/Then
        UserRegistrationException exception = assertThrows(UserRegistrationException.class,
                () -> userService.registerUser(validRegistrationDTO));

        assertThat(exception.getMessage()).isEqualTo("Email already exists");
        verify(userRepository, never()).save(any());
    }

    @Test
    void whenInvalidEmailFormat_thenThrowException() {
        // Given
        validRegistrationDTO.setEmail("invalid-email-format");

        // When/Then
        UserRegistrationException exception = assertThrows(UserRegistrationException.class,
                () -> userService.registerUser(validRegistrationDTO));

        assertThat(exception.getMessage()).isEqualTo("Invalid email format");
        verify(userRepository, never()).save(any());
    }

    @Test
    void whenWeakPassword_thenThrowException() {
        // Given
        validRegistrationDTO.setPassword("weak");
        validRegistrationDTO.setConfirmPassword("weak");

        // When/Then
        UserRegistrationException exception = assertThrows(UserRegistrationException.class,
                () -> userService.registerUser(validRegistrationDTO));

        assertThat(exception.getMessage()).isEqualTo("Password must be at least 6 characters and contain both letters and numbers");
        verify(userRepository, never()).save(any());
    }
} 