package com.gamerecs.gamerecs_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamerecs.gamerecs_backend.dto.LoginDTO;
import com.gamerecs.gamerecs_backend.dto.UserRegistrationDTO;
import com.gamerecs.gamerecs_backend.model.User;
import com.gamerecs.gamerecs_backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private static final String REGISTRATION_ENDPOINT = "/api/users/register";
    private static final String LOGIN_ENDPOINT = "/api/users/login";

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void whenValidRegistration_thenReturns201() throws Exception {
        UserRegistrationDTO request = new UserRegistrationDTO();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("Password123!");
        request.setConfirmPassword("Password123!");

        mockMvc.perform(post(REGISTRATION_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void whenDuplicateUsername_thenReturns409() throws Exception {
        UserRegistrationDTO request = new UserRegistrationDTO();
        request.setUsername("testuser");
        request.setEmail("test1@example.com");
        request.setPassword("Password123!");
        request.setConfirmPassword("Password123!");

        mockMvc.perform(post(REGISTRATION_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        request.setEmail("test2@example.com");
        mockMvc.perform(post(REGISTRATION_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("User registration failed: Username already exists")));
    }

    @Test
    void whenInvalidRegistrationData_thenReturns400() throws Exception {
        UserRegistrationDTO request = new UserRegistrationDTO();
        request.setUsername("");
        request.setEmail("invalid-email");
        request.setPassword("weak");
        request.setConfirmPassword("different");

        mockMvc.perform(post(REGISTRATION_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").exists());
    }

    @Test
    void whenValidLogin_thenReturns200WithToken() throws Exception {
        UserRegistrationDTO regRequest = new UserRegistrationDTO();
        regRequest.setUsername("testuser");
        regRequest.setEmail("test@example.com");
        regRequest.setPassword("Password123!");
        regRequest.setConfirmPassword("Password123!");

        mockMvc.perform(post(REGISTRATION_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regRequest)))
                .andExpect(status().isCreated());

        LoginDTO loginRequest = new LoginDTO();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("Password123!");

        mockMvc.perform(post(LOGIN_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void whenInvalidCredentials_thenReturns401() throws Exception {
        LoginDTO request = new LoginDTO();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        mockMvc.perform(post(LOGIN_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid username or password"));
    }

    @Test
    void whenNonExistentUser_thenReturns401() throws Exception {
        LoginDTO request = new LoginDTO();
        request.setUsername("nonexistentuser");
        request.setPassword("Password123!");

        mockMvc.perform(post(LOGIN_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid username or password"));
    }
}