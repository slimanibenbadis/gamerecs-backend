package com.gamerecs.gamerecs_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamerecs.gamerecs_backend.dto.LoginDTO;
import com.gamerecs.gamerecs_backend.dto.UserProfileDTO;
import com.gamerecs.gamerecs_backend.dto.UserRegistrationDTO;
import com.gamerecs.gamerecs_backend.exception.UserRegistrationException;
import com.gamerecs.gamerecs_backend.security.JwtService;
import com.gamerecs.gamerecs_backend.service.UserService;

import java.util.Collections;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtService jwtService;

    private UserRegistrationDTO validRegistrationDTO;
    private LoginDTO validLoginDTO;
    private UserProfileDTO validProfileDTO;

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

        validLoginDTO = new LoginDTO();
        validLoginDTO.setUsername("testuser");
        validLoginDTO.setPassword("password123");

        validProfileDTO = UserProfileDTO.builder()
                .username("testuser")
                .email("test@example.com")
                .bio("Test bio")
                .profilePictureURL("http://example.com/pic.jpg")
                .joinDate(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should successfully register a new user")
    void registerUser_WithValidData_ShouldReturn201() throws Exception {
        // Arrange
        com.gamerecs.gamerecs_backend.model.User createdUser = new com.gamerecs.gamerecs_backend.model.User();
        createdUser.setUsername(validRegistrationDTO.getUsername());
        when(userService.registerUser(any(UserRegistrationDTO.class)))
                .thenReturn(createdUser);

        // Act & Assert
        mockMvc.perform(post("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(validRegistrationDTO.getUsername()));
    }

    @Test
    @DisplayName("Should return 409 when registering with existing username")
    void registerUser_WithExistingUsername_ShouldReturn409() throws Exception {
        // Arrange
        when(userService.registerUser(any(UserRegistrationDTO.class)))
                .thenThrow(new UserRegistrationException("The username is already taken. Please choose a different username."));

        // Act & Assert
        mockMvc.perform(post("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Registration Error"))
                .andExpect(jsonPath("$.message").value("The username is already taken. Please choose a different username."))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Should return 400 when registering with invalid data")
    void registerUser_WithInvalidData_ShouldReturn400() throws Exception {
        // Arrange
        validRegistrationDTO.setEmail("invalid-email"); // Invalid email format

        // Act & Assert
        mockMvc.perform(post("/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRegistrationDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should successfully authenticate user")
    void authenticateUser_WithValidCredentials_ShouldReturn200() throws Exception {
        // Arrange
        UserDetails userDetails = new User(validLoginDTO.getUsername(),
                validLoginDTO.getPassword(), Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null);

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(any(UserDetails.class)))
                .thenReturn("dummy.jwt.token");

        // Act & Assert
        mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginDTO)))
                .andExpect(status().isOk())
                .andExpect(header().exists("Authorization"))
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value(validLoginDTO.getUsername()));
    }

    @Test
    @DisplayName("Should return 401 with invalid credentials")
    void authenticateUser_WithInvalidCredentials_ShouldReturn401() throws Exception {
        // Arrange
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(
                        new org.springframework.security.authentication.BadCredentialsException("Invalid credentials"));

        // Act & Assert
        mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validLoginDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 400 when login with missing credentials")
    void authenticateUser_WithMissingCredentials_ShouldReturn400() throws Exception {
        // Arrange
        LoginDTO emptyLoginDTO = new LoginDTO();

        // Act & Assert
        mockMvc.perform(post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyLoginDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should successfully retrieve user profile")
    @WithMockUser(username = "testuser")
    void getCurrentUserProfile_WithValidToken_ShouldReturn200() throws Exception {
        // Arrange
        when(userService.getUserProfile("testuser")).thenReturn(validProfileDTO);

        // Act & Assert
        mockMvc.perform(get("/users/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(validProfileDTO.getUsername()))
                .andExpect(jsonPath("$.email").value(validProfileDTO.getEmail()))
                .andExpect(jsonPath("$.bio").value(validProfileDTO.getBio()));
    }

    @Test
    @DisplayName("Should return 401 when accessing profile without authentication")
    void getCurrentUserProfile_WithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/users/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 404 when profile not found")
    @WithMockUser(username = "nonexistent")
    void getCurrentUserProfile_WithNonExistentUser_ShouldReturn404() throws Exception {
        // Arrange
        when(userService.getUserProfile("nonexistent"))
                .thenThrow(new UsernameNotFoundException("User not found"));

        // Act & Assert
        mockMvc.perform(get("/users/profile"))
                .andExpect(status().isNotFound());
    }
}