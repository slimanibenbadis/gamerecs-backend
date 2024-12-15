package com.gamerecs.gamerecs_backend.service;

import com.gamerecs.gamerecs_backend.config.ApplicationConfig;
import com.gamerecs.gamerecs_backend.model.Game;
import com.gamerecs.gamerecs_backend.repository.GameRepository;
import com.gamerecs.gamerecs_backend.repository.RatingRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private ApplicationConfig applicationConfig;

    @InjectMocks
    private GameService gameService;

    private Game testGame;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testGame = new Game();
        testGame.setGameId(1L);
        testGame.setIgdbId(123L);
        testGame.setTitle("Test Game");
        testGame.setDeveloper("Test Developer");

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void findById_ExistingGame_ReturnsGame() {
        when(gameRepository.findById(1L)).thenReturn(Optional.of(testGame));

        Optional<Game> result = gameService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(testGame.getGameId(), result.get().getGameId());
        verify(gameRepository).findById(1L);
    }

    @Test
    void findById_NonExistingGame_ReturnsEmpty() {
        when(gameRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Game> result = gameService.findById(99L);

        assertFalse(result.isPresent());
        verify(gameRepository).findById(99L);
    }

    @Test
    void findByIgdbId_ExistingGame_ReturnsGame() {
        when(gameRepository.findByIgdbId(123L)).thenReturn(Optional.of(testGame));

        Optional<Game> result = gameService.findByIgdbId(123L);

        assertTrue(result.isPresent());
        assertEquals(testGame.getIgdbId(), result.get().getIgdbId());
        verify(gameRepository).findByIgdbId(123L);
    }

    @Test
    void searchByTitle_WithResults_ReturnsPageOfGames() {
        List<Game> games = Arrays.asList(testGame);
        Page<Game> gamePage = new PageImpl<>(games, pageable, 1);
        when(gameRepository.findByTitleContainingIgnoreCase(anyString(), any(Pageable.class)))
            .thenReturn(gamePage);

        Page<Game> result = gameService.searchByTitle("Test", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testGame.getTitle(), result.getContent().get(0).getTitle());
        verify(gameRepository).findByTitleContainingIgnoreCase("Test", pageable);
    }

    @Test
    void findByGenre_WithResults_ReturnsPageOfGames() {
        List<Game> games = Arrays.asList(testGame);
        Page<Game> gamePage = new PageImpl<>(games, pageable, 1);
        when(gameRepository.findByGenre(anyString(), any(Pageable.class)))
            .thenReturn(gamePage);

        Page<Game> result = gameService.findByGenre("RPG", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(gameRepository).findByGenre("RPG", pageable);
    }

    @Test
    void findByPlatform_WithResults_ReturnsPageOfGames() {
        List<Game> games = Arrays.asList(testGame);
        Page<Game> gamePage = new PageImpl<>(games, pageable, 1);
        when(gameRepository.findByPlatform(anyString(), any(Pageable.class)))
            .thenReturn(gamePage);

        Page<Game> result = gameService.findByPlatform("PC", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(gameRepository).findByPlatform("PC", pageable);
    }

    @Test
    void findByDeveloper_WithResults_ReturnsPageOfGames() {
        List<Game> games = Arrays.asList(testGame);
        Page<Game> gamePage = new PageImpl<>(games, pageable, 1);
        when(gameRepository.findByDeveloperIgnoreCase(anyString(), any(Pageable.class)))
            .thenReturn(gamePage);

        Page<Game> result = gameService.findByDeveloper("Test Developer", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testGame.getDeveloper(), result.getContent().get(0).getDeveloper());
        verify(gameRepository).findByDeveloperIgnoreCase("Test Developer", pageable);
    }

    @Test
    void saveGame_ValidGame_ReturnsSavedGame() {
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);

        Game result = gameService.saveGame(testGame);

        assertNotNull(result);
        assertEquals(testGame.getGameId(), result.getGameId());
        verify(gameRepository).save(testGame);
    }

    @Test
    void getAverageRating_ExistingGame_ReturnsAverage() {
        when(ratingRepository.calculateAverageRatingByGameId(1L)).thenReturn(85.0);

        Double result = gameService.getAverageRating(1L);

        assertNotNull(result);
        assertEquals(85.0, result);
        verify(ratingRepository).calculateAverageRatingByGameId(1L);
    }

    @Test
    void existsByIgdbId_ExistingGame_ReturnsTrue() {
        when(gameRepository.existsByIgdbId(123L)).thenReturn(true);

        boolean result = gameService.existsByIgdbId(123L);

        assertTrue(result);
        verify(gameRepository).existsByIgdbId(123L);
    }

    @Test
    void addGame_NewGame_ReturnsSavedGame() {
        when(gameRepository.existsByIgdbId(123L)).thenReturn(false);
        when(gameRepository.save(any(Game.class))).thenReturn(testGame);

        Game result = gameService.addGame(testGame);

        assertNotNull(result);
        assertEquals(testGame.getGameId(), result.getGameId());
        verify(gameRepository).existsByIgdbId(123L);
        verify(gameRepository).save(testGame);
    }

    @Test
    void addGame_ExistingIgdbId_ThrowsException() {
        when(gameRepository.existsByIgdbId(123L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> gameService.addGame(testGame));
        verify(gameRepository).existsByIgdbId(123L);
        verify(gameRepository, never()).save(any(Game.class));
    }

    @Test
    void getAllGames_WithResults_ReturnsPageOfGames() {
        List<Game> games = Arrays.asList(testGame);
        Page<Game> gamePage = new PageImpl<>(games, pageable, 1);
        when(gameRepository.findAll(any(Pageable.class))).thenReturn(gamePage);

        Page<Game> result = gameService.getAllGames(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testGame.getGameId(), result.getContent().get(0).getGameId());
        verify(gameRepository).findAll(pageable);
    }
} 