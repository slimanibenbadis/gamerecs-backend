package com.gamerecs.gamerecs_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamerecs.gamerecs_backend.dto.GameDTO;
import com.gamerecs.gamerecs_backend.model.Game;
import com.gamerecs.gamerecs_backend.security.JwtService;
import com.gamerecs.gamerecs_backend.service.GameService;
import com.gamerecs.gamerecs_backend.service.RatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameService gameService;

    @MockBean
    private RatingService ratingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    private Game testGame;
    private GameDTO testGameDTO;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        testGame = new Game("Test Game");
        testGame.setGameId(1L);
        testGame.setIgdbId(12345L);
        testGame.setGenres(Arrays.asList("RPG", "Action"));
        testGame.setPlatforms(Arrays.asList("PC", "PS5"));
        testGame.setReleaseDate(LocalDate.of(2023, 1, 1));
        testGame.setDescription("Test description");
        testGame.setCoverImageURL("http://test.com/image.jpg");
        testGame.setDeveloper("Test Developer");
        testGame.setPublisher("Test Publisher");

        testGameDTO = GameDTO.builder()
                .gameId(1L)
                .igdbId(12345L)
                .title("Test Game")
                .genres(Arrays.asList("RPG", "Action"))
                .platforms(Arrays.asList("PC", "PS5"))
                .releaseDate(LocalDate.of(2023, 1, 1))
                .description("Test description")
                .coverImageURL("http://test.com/image.jpg")
                .developer("Test Developer")
                .publisher("Test Publisher")
                .averageRating(85.0)
                .totalRatings(100L)
                .build();

        // Create user with proper authorities
        List<SimpleGrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_USER")
        );
        UserDetails userDetails = User.builder()
            .username("testuser")
            .password("password")
            .authorities(authorities)
            .build();
        jwtToken = jwtService.generateToken(userDetails);
    }

    @Test
    void getGameById_ExistingGame_ReturnsGame() throws Exception {
        when(gameService.findById(1L)).thenReturn(Optional.of(testGame));
        when(ratingService.getAverageRating(1L)).thenReturn(85.0);
        when(ratingService.getRatingCount(1L)).thenReturn(100L);

        mockMvc.perform(get("/api/games/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.gameId").value(1))
                .andExpect(jsonPath("$.title").value("Test Game"))
                .andExpect(jsonPath("$.averageRating").value(85.0))
                .andExpect(jsonPath("$.totalRatings").value(100));
    }

    @Test
    void getGameById_NonExistingGame_ReturnsNotFound() throws Exception {
        when(gameService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/games/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Game Not Found"))
                .andExpect(jsonPath("$.message").value("Game with ID 999 not found"));
    }

    @Test
    void searchGames_ValidTitle_ReturnsGames() throws Exception {
        List<Game> games = Arrays.asList(testGame);
        Page<Game> gamePage = new PageImpl<>(games);
        when(gameService.searchByTitle(eq("Test"), any(PageRequest.class))).thenReturn(gamePage);
        when(ratingService.getAverageRating(1L)).thenReturn(85.0);
        when(ratingService.getRatingCount(1L)).thenReturn(100L);

        mockMvc.perform(get("/api/games/search")
                .param("title", "Test")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Game"))
                .andExpect(jsonPath("$.content[0].averageRating").value(85.0));
    }

    @Test
    void getGamesByGenre_ValidGenre_ReturnsGames() throws Exception {
        List<Game> games = Arrays.asList(testGame);
        Page<Game> gamePage = new PageImpl<>(games);
        when(gameService.findByGenre(eq("RPG"), any(PageRequest.class))).thenReturn(gamePage);
        when(ratingService.getAverageRating(1L)).thenReturn(85.0);
        when(ratingService.getRatingCount(1L)).thenReturn(100L);

        mockMvc.perform(get("/api/games/genre/RPG")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Game"))
                .andExpect(jsonPath("$.content[0].genres[0]").value("RPG"));
    }

    @Test
    void getGamesByPlatform_ValidPlatform_ReturnsGames() throws Exception {
        List<Game> games = Arrays.asList(testGame);
        Page<Game> gamePage = new PageImpl<>(games);
        when(gameService.findByPlatform(eq("PC"), any(PageRequest.class))).thenReturn(gamePage);
        when(ratingService.getAverageRating(1L)).thenReturn(85.0);
        when(ratingService.getRatingCount(1L)).thenReturn(100L);

        mockMvc.perform(get("/api/games/platform/PC")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Game"))
                .andExpect(jsonPath("$.content[0].platforms[0]").value("PC"));
    }

    @Test
    void getGamesByDeveloper_ValidDeveloper_ReturnsGames() throws Exception {
        List<Game> games = Arrays.asList(testGame);
        Page<Game> gamePage = new PageImpl<>(games);
        when(gameService.findByDeveloper(eq("Test Developer"), any(PageRequest.class))).thenReturn(gamePage);
        when(ratingService.getAverageRating(1L)).thenReturn(85.0);
        when(ratingService.getRatingCount(1L)).thenReturn(100L);

        mockMvc.perform(get("/api/games/developer/Test Developer")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Game"))
                .andExpect(jsonPath("$.content[0].developer").value("Test Developer"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void addGame_ValidGame_ReturnsCreatedGame() throws Exception {
        when(gameService.addGame(any(Game.class))).thenReturn(testGame);
        when(ratingService.getAverageRating(1L)).thenReturn(85.0);
        when(ratingService.getRatingCount(1L)).thenReturn(100L);

        mockMvc.perform(post("/api/games")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testGameDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Game"))
                .andExpect(jsonPath("$.developer").value("Test Developer"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void addGame_DuplicateGame_ReturnsConflict() throws Exception {
        when(gameService.addGame(any(Game.class)))
                .thenThrow(new IllegalArgumentException("Game already exists"));

        mockMvc.perform(post("/api/games")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testGameDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Game already exists"));
    }

    @Test
    void getAllGames_ReturnsPageOfGames() throws Exception {
        List<Game> games = Arrays.asList(testGame);
        Page<Game> gamePage = new PageImpl<>(games);
        when(gameService.getAllGames(any(PageRequest.class))).thenReturn(gamePage);
        when(ratingService.getAverageRating(1L)).thenReturn(85.0);
        when(ratingService.getRatingCount(1L)).thenReturn(100L);

        mockMvc.perform(get("/api/games")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Game"))
                .andExpect(jsonPath("$.content[0].averageRating").value(85.0));
    }
} 