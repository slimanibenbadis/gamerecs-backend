package com.gamerecs.gamerecs_backend.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamerecs.gamerecs_backend.dto.GameDTO;
import com.gamerecs.gamerecs_backend.model.Game;
import com.gamerecs.gamerecs_backend.service.GameService;
import com.gamerecs.gamerecs_backend.service.RatingService;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
public class GameControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GameService gameService;

    @MockBean
    private RatingService ratingService;

    private GameDTO validGameDTO;

    @BeforeEach
    void setUp() {
        // Create a valid GameDTO with all required fields
        validGameDTO = GameDTO.builder()
            .title("Test Game")
            .genres(Arrays.asList("Action"))
            .platforms(Arrays.asList("PC"))
            .description("Test Description")
            .build();
        
        // Create a Game entity that will be returned
        Game savedGame = new Game(validGameDTO.getTitle());
        savedGame.setGameId(1L);
        savedGame.setGenres(validGameDTO.getGenres());
        savedGame.setPlatforms(validGameDTO.getPlatforms());
        savedGame.setDescription(validGameDTO.getDescription());
        
        // Mock service responses
        when(gameService.findByGenre(anyString(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(gameService.getAllGames(any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(gameService.searchByTitle(anyString(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(gameService.addGame(any(Game.class))).thenReturn(savedGame);
    }

    @Test
    @DisplayName("Should allow anonymous access to get game by ID")
    void getGameById_ShouldAllowAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/games/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Not found is expected as we're not mocking the service response
    }

    @Test
    @DisplayName("Should allow anonymous access to search games")
    void searchGames_ShouldAllowAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/games/search")
                .param("title", "test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should require authentication for adding a new game")
    void addGame_ShouldRequireAuthentication() throws Exception {
        mockMvc.perform(post("/api/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validGameDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("Should allow authenticated user to add a new game")
    void addGame_ShouldAllowAuthenticatedUser() throws Exception {
        mockMvc.perform(post("/api/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validGameDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should allow anonymous access to get games by genre")
    void getGamesByGenre_ShouldAllowAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/games/genre/action")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should allow anonymous access to get all games")
    void getAllGames_ShouldAllowAnonymousAccess() throws Exception {
        mockMvc.perform(get("/api/games")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
} 