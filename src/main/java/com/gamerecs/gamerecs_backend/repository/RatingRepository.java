package com.gamerecs.gamerecs_backend.repository;

import com.gamerecs.gamerecs_backend.model.Rating;
import com.gamerecs.gamerecs_backend.model.User;
import com.gamerecs.gamerecs_backend.model.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Rating entity operations.
 * Extends JpaRepository to inherit basic CRUD operations and pagination support.
 */
@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    
    /**
     * Find a rating by user and game
     * @param user the user who made the rating
     * @param game the game that was rated
     * @return Optional containing the rating if found
     */
    Optional<Rating> findByUserAndGame(User user, Game game);
    
    /**
     * Find all ratings by user ordered by rating value in ascending order
     * @param user the user whose ratings to find
     * @return List of ratings ordered by rating value ascending
     */
    List<Rating> findByUserOrderByRatingValueAsc(User user);
    
    /**
     * Find all ratings by user
     * @param user the user whose ratings to find
     * @param pageable pagination information
     * @return Page of ratings from the specified user
     */
    Page<Rating> findByUser(User user, Pageable pageable);
    
    /**
     * Find all ratings for a game
     * @param game the game to find ratings for
     * @param pageable pagination information
     * @return Page of ratings for the specified game
     */
    Page<Rating> findByGame(Game game, Pageable pageable);
    
    /**
     * Calculate the average rating for a game
     * @param gameId the ID of the game
     * @return the average rating value, or null if no ratings exist
     */
    @Query("SELECT AVG(r.ratingValue) FROM Rating r WHERE r.game.gameId = :gameId")
    Double calculateAverageRatingByGameId(@Param("gameId") Long gameId);
    
    /**
     * Count the number of ratings for a game
     * @param gameId the ID of the game
     * @return the number of ratings
     */
    long countByGameGameId(Long gameId);
    
    /**
     * Find all ratings by user with rating value above a threshold
     * @param user the user whose ratings to find
     * @param threshold the minimum rating value
     * @param pageable pagination information
     * @return Page of ratings above the threshold
     */
    Page<Rating> findByUserAndRatingValueGreaterThanEqual(User user, Integer threshold, Pageable pageable);
    
    /**
     * Delete all ratings for a specific game
     * @param game the game whose ratings should be deleted
     */
    void deleteByGame(Game game);
    
    /**
     * Delete all ratings by a specific user
     * @param user the user whose ratings should be deleted
     */
    void deleteByUser(User user);
} 