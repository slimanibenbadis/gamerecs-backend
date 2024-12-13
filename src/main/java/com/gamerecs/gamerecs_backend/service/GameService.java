package com.gamerecs.gamerecs_backend.service;

import com.gamerecs.gamerecs_backend.model.Game;
import com.gamerecs.gamerecs_backend.repository.GameRepository;
import com.gamerecs.gamerecs_backend.repository.RatingRepository;
import com.gamerecs.gamerecs_backend.config.ApplicationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service class handling game-related business logic.
 * This service manages game operations and integrates with the IGDB API.
 */
@Service
@Transactional
public class GameService {

    private final GameRepository gameRepository;
    private final RatingRepository ratingRepository;

    @Autowired
    public GameService(GameRepository gameRepository, 
                      RatingRepository ratingRepository,
                      ApplicationConfig applicationConfig) {
        this.gameRepository = gameRepository;
        this.ratingRepository = ratingRepository;
    }

    /**
     * Find a game by its ID
     * @param id the game ID
     * @return Optional containing the game if found
     */
    public Optional<Game> findById(Long id) {
        return gameRepository.findById(id);
    }

    /**
     * Find a game by its IGDB ID
     * @param igdbId the IGDB ID
     * @return Optional containing the game if found
     */
    public Optional<Game> findByIgdbId(Long igdbId) {
        return gameRepository.findByIgdbId(igdbId);
    }

    /**
     * Search games by title (case-insensitive)
     * @param title the title to search for
     * @param pageable pagination information
     * @return Page of games matching the title
     */
    public Page<Game> searchByTitle(String title, Pageable pageable) {
        return gameRepository.findByTitleContainingIgnoreCase(title, pageable);
    }

    /**
     * Find games by genre
     * @param genre the genre to search for
     * @param pageable pagination information
     * @return Page of games with the specified genre
     */
    public Page<Game> findByGenre(String genre, Pageable pageable) {
        return gameRepository.findByGenre(genre, pageable);
    }

    /**
     * Find games by platform
     * @param platform the platform to search for
     * @param pageable pagination information
     * @return Page of games available on the specified platform
     */
    public Page<Game> findByPlatform(String platform, Pageable pageable) {
        return gameRepository.findByPlatform(platform, pageable);
    }

    /**
     * Find games by developer
     * @param developer the developer name
     * @param pageable pagination information
     * @return Page of games from the specified developer
     */
    public Page<Game> findByDeveloper(String developer, Pageable pageable) {
        return gameRepository.findByDeveloperIgnoreCase(developer, pageable);
    }

    /**
     * Save a new game or update an existing one
     * @param game the game to save
     * @return the saved game
     */
    public Game saveGame(Game game) {
        return gameRepository.save(game);
    }

    /**
     * Get the average rating for a game
     * @param gameId the ID of the game
     * @return the average rating value, or null if no ratings exist
     */
    public Double getAverageRating(Long gameId) {
        return ratingRepository.calculateAverageRatingByGameId(gameId);
    }

    /**
     * Check if a game exists by IGDB ID
     * @param igdbId the IGDB ID to check
     * @return true if the game exists, false otherwise
     */
    public boolean existsByIgdbId(Long igdbId) {
        return gameRepository.existsByIgdbId(igdbId);
    }

    /**
     * Add a new game to the system
     * @param game the game to be added
     * @return the saved game
     * @throws IllegalArgumentException if the game already exists with the given IGDB ID
     */
    public Game addGame(Game game) {
        if (game.getIgdbId() != null && existsByIgdbId(game.getIgdbId())) {
            throw new IllegalArgumentException("Game already exists with IGDB ID: " + game.getIgdbId());
        }
        return saveGame(game);
    }

    /**
     * Retrieve all games with pagination support
     * @param pageable pagination information
     * @return Page of all games
     */
    public Page<Game> getAllGames(Pageable pageable) {
        return gameRepository.findAll(pageable);
    }
} 