package com.gamerecs.repository;

import com.gamerecs.gamerecs_backend.model.Game;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Game entity operations.
 * Extends JpaRepository to inherit basic CRUD operations and pagination support.
 */
@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    
    /**
     * Find a game by its IGDB ID
     * @param igdbId the IGDB ID of the game
     * @return Optional containing the game if found
     */
    Optional<Game> findByIgdbId(Long igdbId);
    
    /**
     * Find games by title containing the given string (case-insensitive)
     * @param title the title to search for
     * @param pageable pagination information
     * @return Page of games matching the title
     */
    Page<Game> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    
    /**
     * Find games by genre
     * @param genre the genre to search for
     * @param pageable pagination information
     * @return Page of games with the specified genre
     */
    @Query("SELECT g FROM Game g JOIN g.genres genre WHERE LOWER(genre) = LOWER(:genre)")
    Page<Game> findByGenre(@Param("genre") String genre, Pageable pageable);
    
    /**
     * Find games by platform
     * @param platform the platform to search for
     * @param pageable pagination information
     * @return Page of games available on the specified platform
     */
    @Query("SELECT g FROM Game g JOIN g.platforms platform WHERE LOWER(platform) = LOWER(:platform)")
    Page<Game> findByPlatform(@Param("platform") String platform, Pageable pageable);
    
    /**
     * Find games by developer
     * @param developer the developer name
     * @param pageable pagination information
     * @return Page of games from the specified developer
     */
    Page<Game> findByDeveloperIgnoreCase(String developer, Pageable pageable);
    
    /**
     * Find games by publisher
     * @param publisher the publisher name
     * @param pageable pagination information
     * @return Page of games from the specified publisher
     */
    Page<Game> findByPublisherIgnoreCase(String publisher, Pageable pageable);
    
    /**
     * Check if a game exists by IGDB ID
     * @param igdbId the IGDB ID to check
     * @return true if the game exists, false otherwise
     */
    boolean existsByIgdbId(Long igdbId);
} 