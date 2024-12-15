package com.gamerecs.gamerecs_backend.controller;

import com.gamerecs.gamerecs_backend.model.Game;
import com.gamerecs.gamerecs_backend.model.Rating;
import com.gamerecs.gamerecs_backend.model.User;
import com.gamerecs.gamerecs_backend.security.UserDetailsImpl;
import com.gamerecs.gamerecs_backend.service.GameService;
import com.gamerecs.gamerecs_backend.service.RatingService;
import com.gamerecs.gamerecs_backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RatingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RatingService ratingService;

    @MockBean
    private GameService gameService;

    @MockBean
    private UserService userService;

    private User testUser;
    private UserDetailsImpl userDetails;
    private Game testGame;
    private Rating testRating;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedpassword");
        testUser.setJoinDate(new Timestamp(System.currentTimeMillis()));

        userDetails = new UserDetailsImpl(testUser);
        when(userService.loadUserByUsername("testuser")).thenReturn(userDetails);

        testGame = new Game("Test Game");
        testGame.setGameId(1L);

        testRating = new Rating();
        testRating.setRatingId(1L);
        testRating.setUser(testUser);
        testRating.setGame(testGame);
        testRating.setRatingValue(85);
        testRating.setPercentileRank(75);
        testRating.setDateUpdated(LocalDateTime.now());
    }

    @Test
    void rateGame_ValidRating_ReturnsRating() throws Exception {
        when(gameService.findById(1L)).thenReturn(Optional.of(testGame));
        when(ratingService.addOrUpdateRating(any(User.class), any(Game.class), eq(85)))
                .thenReturn(testRating);

        mockMvc.perform(put("/api/ratings/games/1")
                .with(user(userDetails))
                .param("ratingValue", "85"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ratingValue").value(85))
                .andExpect(jsonPath("$.percentileRank").value(75));
    }

    @Test
    void rateGame_InvalidRatingValue_ReturnsBadRequest() throws Exception {
        mockMvc.perform(put("/api/ratings/games/1")
                .with(user(userDetails))
                .param("ratingValue", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rateGame_GameNotFound_ReturnsNotFound() throws Exception {
        when(gameService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/ratings/games/1")
                .with(user(userDetails))
                .param("ratingValue", "85"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserGameRating_ValidGameId_ReturnsRating() throws Exception {
        when(gameService.findById(1L)).thenReturn(Optional.of(testGame));
        when(ratingService.getUserRatingForGame(any(User.class), any(Game.class)))
                .thenReturn(Optional.of(testRating));

        mockMvc.perform(get("/api/ratings/users/me/games/1")
                .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ratingValue").value(85));
    }

    @Test
    void getUserGameRating_RatingNotFound_ReturnsNotFound() throws Exception {
        when(gameService.findById(1L)).thenReturn(Optional.of(testGame));
        when(ratingService.getUserRatingForGame(any(User.class), any(Game.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/ratings/users/me/games/1")
                .with(user(userDetails)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRating_ValidRating_ReturnsNoContent() throws Exception {
        when(gameService.findById(1L)).thenReturn(Optional.of(testGame));
        when(ratingService.getUserRatingForGame(any(User.class), any(Game.class)))
                .thenReturn(Optional.of(testRating));

        mockMvc.perform(delete("/api/ratings/users/me/games/1")
                .with(user(userDetails)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteRating_RatingNotFound_ReturnsNotFound() throws Exception {
        when(gameService.findById(1L)).thenReturn(Optional.of(testGame));
        when(ratingService.getUserRatingForGame(any(User.class), any(Game.class)))
                .thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/ratings/users/me/games/1")
                .with(user(userDetails)))
                .andExpect(status().isNotFound());
    }
} 