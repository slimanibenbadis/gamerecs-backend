package com.gamerecs.gamerecs_backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;

import com.gamerecs.gamerecs_backend.model.Game;
import com.gamerecs.gamerecs_backend.security.WithMockGameRecsUser;
import com.gamerecs.gamerecs_backend.service.GameService;
import com.gamerecs.gamerecs_backend.service.RatingService;
import com.gamerecs.gamerecs_backend.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
public class RatingControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RatingService ratingService;

    @MockBean
    private GameService gameService;

    @MockBean
    private UserService userService;

    private Game testGame;

    @BeforeEach
    void setUp() {
        testGame = new Game("Test Game");
        testGame.setGameId(1L);
    }

    @Test
    @DisplayName("Should require authentication for rating a game")
    void rateGame_ShouldRequireAuthentication() throws Exception {
        mockMvc.perform(put("/api/ratings/games/1")
                .param("ratingValue", "85")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockGameRecsUser
    @DisplayName("Should allow authenticated user to rate a game")
    void rateGame_ShouldAllowAuthenticatedUser() throws Exception {
        when(gameService.findById(anyLong())).thenReturn(Optional.of(testGame));
        when(ratingService.addOrUpdateRating(any(), any(), any())).thenReturn(null);

        mockMvc.perform(put("/api/ratings/games/1")
                .param("ratingValue", "85")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should allow anonymous access to get game ratings")
    void getGameRatings_ShouldAllowAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/ratings/games/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Not found is expected as we're not mocking the service response
    }

    @Test
    @DisplayName("Should allow anonymous access to get average game rating")
    void getAverageGameRating_ShouldAllowAnonymousAccess() throws Exception {
        when(ratingService.getAverageRating(anyLong())).thenThrow(new IllegalArgumentException("Game not found"));

        mockMvc.perform(get("/api/ratings/games/1/average")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should allow anonymous access to get user ratings")
    void getUserRatings_ShouldAllowAnonymousAccess() throws Exception {
        when(userService.loadUserByUsername(anyString()))
                .thenThrow(new UsernameNotFoundException("User not found"));

        mockMvc.perform(get("/api/ratings/users/testuser")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should require authentication to get authenticated user's game rating")
    void getUserGameRating_ShouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/ratings/users/me/games/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockGameRecsUser
    @DisplayName("Should allow authenticated user to get their game rating")
    void getUserGameRating_ShouldAllowAuthenticatedUser() throws Exception {
        when(gameService.findById(anyLong())).thenReturn(Optional.of(testGame));

        mockMvc.perform(get("/api/ratings/users/me/games/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Not found is expected as we're not mocking the rating service response
    }

    @Test
    @DisplayName("Should require authentication to delete a rating")
    void deleteRating_ShouldRequireAuthentication() throws Exception {
        mockMvc.perform(delete("/api/ratings/users/me/games/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockGameRecsUser
    @DisplayName("Should allow authenticated user to delete their rating")
    void deleteRating_ShouldAllowAuthenticatedUser() throws Exception {
        when(gameService.findById(anyLong())).thenReturn(Optional.of(testGame));

        mockMvc.perform(delete("/api/ratings/users/me/games/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Not found is expected as we're not mocking the rating service response
    }
} 