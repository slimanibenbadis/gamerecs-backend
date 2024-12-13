package com.gamerecs.gamerecs_backend.service;

import com.gamerecs.gamerecs_backend.model.Game;
import com.gamerecs.gamerecs_backend.model.Rating;
import com.gamerecs.gamerecs_backend.model.User;
import com.gamerecs.gamerecs_backend.repository.RatingRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service class handling rating-related business logic.
 * This service manages operations related to game ratings with data consistency guarantees.
 */
@Service
@Transactional
public class RatingService {

    private final RatingRepository ratingRepository;
    private final Map<String, Lock> userLocks = new ConcurrentHashMap<>();

    @Autowired
    public RatingService(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    /**
     * Add or update a rating for a game by a user, including percentile rank calculation
     * This method ensures data consistency by using user-specific locks
     * @param user the user giving the rating
     * @param game the game being rated
     * @param ratingValue the rating value (0-100)
     * @return the created or updated rating
     * @throws IllegalArgumentException if rating value is invalid or if user has insufficient rating history
     */
    @Transactional
    @CacheEvict(value = {"userRatings", "gameRatings", "averageRatings"}, allEntries = true)
    public Rating addOrUpdateRating(User user, Game game, Integer ratingValue) {
        validateInputs(user, game, ratingValue);
        
        // Get or create a lock for this user
        Lock userLock = userLocks.computeIfAbsent(
            user.getUserId().toString(),
            k -> new ReentrantLock()
        );
        
        try {
            userLock.lock();
            
            // Get all user's ratings ordered by value for percentile calculation
            List<Rating> userRatings = getUserRatingsOrderedByValue(user);
            
            // Validate distinct ratings requirement
            validateDistinctRatings(userRatings, game);

            Optional<Rating> existingRating = ratingRepository.findByUserAndGame(user, game);
            Rating rating;

            if (existingRating.isPresent()) {
                rating = updateExistingRating(existingRating.get(), ratingValue);
            } else {
                rating = createNewRating(user, game, ratingValue);
            }

            // Calculate and set the percentile rank
            int percentileRank = calculatePercentileRank(ratingValue, userRatings);
            rating.setPercentileRank(percentileRank);

            return ratingRepository.save(rating);
        } finally {
            userLock.unlock();
        }
    }

    private void validateInputs(User user, Game game, Integer ratingValue) {
        if (user == null || game == null) {
            throw new IllegalArgumentException("User and game must not be null");
        }
        if (ratingValue == null) {
            throw new IllegalArgumentException("Rating value must not be null");
        }
        if (ratingValue < 0 || ratingValue > 100) {
            throw new IllegalArgumentException("Rating value must be between 0 and 100");
        }
    }

    private void validateDistinctRatings(List<Rating> userRatings, Game game) {
        long distinctRatings = userRatings.stream()
                .filter(r -> !r.getGame().equals(game))
                .map(Rating::getRatingValue)
                .distinct()
                .count();
                
        if (distinctRatings < 4) {
            throw new IllegalArgumentException("At least 5 distinct ratings are required to calculate percentiles");
        }
    }

    private Rating updateExistingRating(Rating rating, Integer ratingValue) {
        rating.setRatingValue(ratingValue);
        rating.setDateUpdated(LocalDateTime.now());
        return rating;
    }

    private Rating createNewRating(User user, Game game, Integer ratingValue) {
        Rating rating = new Rating(user, game, ratingValue);
        rating.setDateUpdated(LocalDateTime.now());
        return rating;
    }

    /**
     * Calculate the percentile rank for a rating value based on user's rating history
     * @param ratingValue the rating value to calculate percentile for
     * @param userRatings list of user's ratings ordered by value
     * @return the calculated percentile rank (0-99)
     */
    private int calculatePercentileRank(int ratingValue, List<Rating> userRatings) {
        int totalRatings = userRatings.size() + 1; // Include the new rating
        
        // Count ratings lower than the current rating
        long numLower = userRatings.stream()
                .filter(r -> r.getRatingValue() < ratingValue)
                .count();
                
        // Count ratings equal to the current rating
        long numMatching = userRatings.stream()
                .filter(r -> r.getRatingValue() == ratingValue)
                .count();
        
        // Calculate percentile using the formula from PRD:
        // percentile = floor((num_lower + num_matching/2) / total_ratings * 100)
        double percentile = Math.floor((numLower + (numMatching / 2.0)) / totalRatings * 100);
        
        // Ensure the result is between 0 and 99
        return (int) Math.min(Math.max(percentile, 0), 99);
    }

    /**
     * Get a user's rating for a specific game
     * @param user the user
     * @param game the game
     * @return Optional containing the rating if found
     */
    @Cacheable(value = "userRatings", key = "#user.id + '-' + #game.id")
    public Optional<Rating> getUserRatingForGame(User user, Game game) {
        return ratingRepository.findByUserAndGame(user, game);
    }

    /**
     * Get all ratings by a user
     * @param user the user
     * @param pageable pagination information
     * @return Page of ratings from the user
     */
    @Cacheable(value = "userRatings", key = "#user.id + '-' + #pageable.pageNumber")
    public Page<Rating> getUserRatings(User user, Pageable pageable) {
        return ratingRepository.findByUser(user, pageable);
    }

    /**
     * Get all ratings for a game
     * @param game the game
     * @param pageable pagination information
     * @return Page of ratings for the game
     */
    @Cacheable(value = "gameRatings", key = "#game.id + '-' + #pageable.pageNumber")
    public Page<Rating> getGameRatings(Game game, Pageable pageable) {
        return ratingRepository.findByGame(game, pageable);
    }

    /**
     * Get average rating for a game
     * @param gameId the ID of the game
     * @return the average rating value, or null if no ratings exist
     */
    @Cacheable(value = "averageRatings", key = "#gameId")
    public Double getAverageRating(Long gameId) {
        return ratingRepository.calculateAverageRatingByGameId(gameId);
    }

    /**
     * Get the number of ratings for a game
     * @param gameId the ID of the game
     * @return the count of ratings
     */
    public long getRatingCount(Long gameId) {
        return ratingRepository.countByGameGameId(gameId);
    }

    /**
     * Get user's ratings above a certain threshold
     * @param user the user
     * @param threshold the minimum rating value
     * @param pageable pagination information
     * @return Page of ratings above the threshold
     */
    public Page<Rating> getUserRatingsAboveThreshold(User user, Integer threshold, Pageable pageable) {
        return ratingRepository.findByUserAndRatingValueGreaterThanEqual(user, threshold, pageable);
    }

    /**
     * Get all ratings by a user ordered by rating value
     * @param user the user
     * @return List of ratings ordered by rating value ascending
     */
    public List<Rating> getUserRatingsOrderedByValue(User user) {
        return ratingRepository.findByUserOrderByRatingValueAsc(user);
    }

    /**
     * Delete a rating with proper cache eviction
     * @param rating the rating to delete
     */
    @Transactional
    @CacheEvict(value = {"userRatings", "gameRatings", "averageRatings"}, allEntries = true)
    public void deleteRating(Rating rating) {
        if (rating == null) {
            throw new IllegalArgumentException("Rating must not be null");
        }
        ratingRepository.delete(rating);
    }

    /**
     * Delete all ratings for a game with proper cache eviction
     * @param game the game whose ratings should be deleted
     */
    @Transactional
    @CacheEvict(value = {"userRatings", "gameRatings", "averageRatings"}, allEntries = true)
    public void deleteGameRatings(Game game) {
        if (game == null) {
            throw new IllegalArgumentException("Game must not be null");
        }
        ratingRepository.deleteByGame(game);
    }

    /**
     * Delete all ratings by a user with proper cache eviction
     * @param user the user whose ratings should be deleted
     */
    @Transactional
    @CacheEvict(value = {"userRatings", "gameRatings", "averageRatings"}, allEntries = true)
    public void deleteUserRatings(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User must not be null");
        }
        ratingRepository.deleteByUser(user);
    }
} 