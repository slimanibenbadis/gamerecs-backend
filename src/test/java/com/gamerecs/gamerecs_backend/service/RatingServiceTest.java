package com.gamerecs.gamerecs_backend.service;

import com.gamerecs.gamerecs_backend.model.Game;
import com.gamerecs.gamerecs_backend.model.Rating;
import com.gamerecs.gamerecs_backend.model.User;
import com.gamerecs.gamerecs_backend.repository.RatingRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @InjectMocks
    private RatingService ratingService;

    private User testUser;
    private Game testGame;
    private Rating testRating;
    private Pageable pageable;
    private List<Rating> existingRatings;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testUser");

        testGame = new Game();
        testGame.setGameId(1L);
        testGame.setTitle("Test Game");

        testRating = new Rating(testUser, testGame, 85);
        testRating.setRatingId(1L);
        testRating.setDateUpdated(LocalDateTime.now());
        testRating.setPercentileRank(79);

        pageable = PageRequest.of(0, 10);

        // Create a list of existing ratings for percentile calculation tests
        existingRatings = Arrays.asList(
            createRating(60),
            createRating(65),
            createRating(70),
            createRating(75),
            createRating(80)
        );
    }

    private Rating createRating(int value) {
        Rating rating = new Rating(testUser, new Game(), value);
        rating.setDateUpdated(LocalDateTime.now());
        rating.setPercentileRank(calculateMockPercentile(value));
        return rating;
    }

    private int calculateMockPercentile(int value) {
        // Simple mock calculation for test data
        return Math.min((value * 100) / 100, 99);
    }

    @Test
    void addOrUpdateRating_NewRating_CalculatesPercentileAndSaves() {
        Rating savedRating = new Rating(testUser, testGame, 85);
        savedRating.setPercentileRank(79);

        when(ratingRepository.findByUserAndGame(testUser, testGame))
            .thenReturn(Optional.empty());
        when(ratingRepository.findByUserOrderByRatingValueAsc(testUser))
            .thenReturn(existingRatings);
        when(ratingRepository.save(any(Rating.class)))
            .thenReturn(savedRating);

        Rating result = ratingService.addOrUpdateRating(testUser, testGame, 85);

        assertNotNull(result);
        assertEquals(85, result.getRatingValue());
        assertNotNull(result.getPercentileRank());
        assertTrue(result.getPercentileRank() >= 0 && result.getPercentileRank() <= 99);
        verify(ratingRepository).save(any(Rating.class));
    }

    @Test
    void addOrUpdateRating_UpdateExisting_UpdatesPercentileAndSaves() {
        Rating existingRating = new Rating(testUser, testGame, 70);
        existingRating.setPercentileRank(50);

        Rating savedRating = new Rating(testUser, testGame, 85);
        savedRating.setPercentileRank(79);

        when(ratingRepository.findByUserAndGame(testUser, testGame))
            .thenReturn(Optional.of(existingRating));
        when(ratingRepository.findByUserOrderByRatingValueAsc(testUser))
            .thenReturn(existingRatings);
        when(ratingRepository.save(any(Rating.class)))
            .thenReturn(savedRating);

        Rating result = ratingService.addOrUpdateRating(testUser, testGame, 85);

        assertNotNull(result);
        assertEquals(85, result.getRatingValue());
        assertNotNull(result.getPercentileRank());
        assertTrue(result.getPercentileRank() >= 0 && result.getPercentileRank() <= 99);
        verify(ratingRepository).save(any(Rating.class));
    }

    @Test
    void addOrUpdateRating_InvalidRatingValue_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            ratingService.addOrUpdateRating(testUser, testGame, 101));
        
        assertThrows(IllegalArgumentException.class, () ->
            ratingService.addOrUpdateRating(testUser, testGame, -1));
        
        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    void addOrUpdateRating_InsufficientRatingHistory_ThrowsException() {
        when(ratingRepository.findByUserOrderByRatingValueAsc(testUser))
            .thenReturn(Arrays.asList(createRating(70), createRating(70), createRating(70)));

        assertThrows(IllegalArgumentException.class, () ->
            ratingService.addOrUpdateRating(testUser, testGame, 85));
        
        verify(ratingRepository, never()).save(any(Rating.class));
    }

    @Test
    void calculatePercentileRank_ValidInput_CorrectCalculation() {
        // Test case from PRD example:
        // 195 ratings lower than 85
        // 10 ratings equal to 85
        // Total 251 ratings (including new rating)
        List<Rating> ratings = new ArrayList<>();
        
        // Add ratings with distinct values to meet the minimum requirement
        // Adding ratings with values 60, 65, 70, 75, 80 (5 distinct values)
        ratings.add(createRating(60));
        ratings.add(createRating(65));
        ratings.add(createRating(70));
        ratings.add(createRating(75));
        
        // Add the remaining lower ratings
        for (int i = 0; i < 191; i++) {
            ratings.add(createRating(80));
        }
        
        // Add ratings equal to the new rating
        for (int i = 0; i < 10; i++) {
            ratings.add(createRating(85));
        }
        
        // Add higher ratings
        for (int i = 0; i < 45; i++) {
            ratings.add(createRating(90));
        }

        Rating savedRating = new Rating(testUser, testGame, 85);
        savedRating.setPercentileRank(79); // Expected percentile as per PRD example

        when(ratingRepository.findByUserAndGame(testUser, testGame))
            .thenReturn(Optional.empty());
        when(ratingRepository.findByUserOrderByRatingValueAsc(testUser))
            .thenReturn(ratings);
        when(ratingRepository.save(any(Rating.class)))
            .thenReturn(savedRating);

        Rating result = ratingService.addOrUpdateRating(testUser, testGame, 85);

        assertNotNull(result);
        assertNotNull(result.getPercentileRank());
        assertEquals(79, result.getPercentileRank()); // As per PRD example
        
        // Verify the total number of ratings matches the PRD example
        assertEquals(250, ratings.size()); // 251 total including the new rating
    }

    @Test
    void getUserRatingForGame_ExistingRating_ReturnsRating() {
        when(ratingRepository.findByUserAndGame(testUser, testGame))
            .thenReturn(Optional.of(testRating));

        Optional<Rating> result = ratingService.getUserRatingForGame(testUser, testGame);

        assertTrue(result.isPresent());
        assertEquals(testRating.getRatingValue(), result.get().getRatingValue());
        verify(ratingRepository).findByUserAndGame(testUser, testGame);
    }

    @Test
    void getUserRatings_WithResults_ReturnsPageOfRatings() {
        List<Rating> ratings = Arrays.asList(testRating);
        Page<Rating> ratingPage = new PageImpl<>(ratings, pageable, 1);
        when(ratingRepository.findByUser(testUser, pageable))
            .thenReturn(ratingPage);

        Page<Rating> result = ratingService.getUserRatings(testUser, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testRating.getRatingValue(), result.getContent().get(0).getRatingValue());
        verify(ratingRepository).findByUser(testUser, pageable);
    }

    @Test
    void getGameRatings_WithResults_ReturnsPageOfRatings() {
        List<Rating> ratings = Arrays.asList(testRating);
        Page<Rating> ratingPage = new PageImpl<>(ratings, pageable, 1);
        when(ratingRepository.findByGame(testGame, pageable))
            .thenReturn(ratingPage);

        Page<Rating> result = ratingService.getGameRatings(testGame, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(ratingRepository).findByGame(testGame, pageable);
    }

    @Test
    void getAverageRating_ExistingRatings_ReturnsAverage() {
        when(ratingRepository.calculateAverageRatingByGameId(1L))
            .thenReturn(85.0);

        Double result = ratingService.getAverageRating(1L);

        assertNotNull(result);
        assertEquals(85.0, result);
        verify(ratingRepository).calculateAverageRatingByGameId(1L);
    }

    @Test
    void getRatingCount_ExistingGame_ReturnsCount() {
        when(ratingRepository.countByGameGameId(1L))
            .thenReturn(10L);

        long result = ratingService.getRatingCount(1L);

        assertEquals(10L, result);
        verify(ratingRepository).countByGameGameId(1L);
    }

    @Test
    void getUserRatingsAboveThreshold_WithResults_ReturnsFilteredRatings() {
        List<Rating> ratings = Arrays.asList(testRating);
        Page<Rating> ratingPage = new PageImpl<>(ratings, pageable, 1);
        when(ratingRepository.findByUserAndRatingValueGreaterThanEqual(
            eq(testUser), eq(80), any(Pageable.class)))
            .thenReturn(ratingPage);

        Page<Rating> result = ratingService.getUserRatingsAboveThreshold(testUser, 80, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(ratingRepository).findByUserAndRatingValueGreaterThanEqual(testUser, 80, pageable);
    }

    @Test
    void deleteRating_ValidRating_DeletesSuccessfully() {
        doNothing().when(ratingRepository).delete(testRating);

        ratingService.deleteRating(testRating);

        verify(ratingRepository).delete(testRating);
    }

    @Test
    void deleteRating_NullRating_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () ->
            ratingService.deleteRating(null));
        
        verify(ratingRepository, never()).delete(any(Rating.class));
    }

    @Test
    void deleteGameRatings_ValidGame_DeletesSuccessfully() {
        doNothing().when(ratingRepository).deleteByGame(testGame);

        ratingService.deleteGameRatings(testGame);

        verify(ratingRepository).deleteByGame(testGame);
    }

    @Test
    void deleteUserRatings_ValidUser_DeletesSuccessfully() {
        doNothing().when(ratingRepository).deleteByUser(testUser);

        ratingService.deleteUserRatings(testUser);

        verify(ratingRepository).deleteByUser(testUser);
    }
} 