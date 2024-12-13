package com.gamerecs.gamerecs_backend.controller;

import com.gamerecs.gamerecs_backend.exception.ErrorResponse;
import com.gamerecs.gamerecs_backend.model.Game;
import com.gamerecs.gamerecs_backend.model.Rating;
import com.gamerecs.gamerecs_backend.model.User;
import com.gamerecs.gamerecs_backend.service.GameService;
import com.gamerecs.gamerecs_backend.service.RatingService;
import com.gamerecs.gamerecs_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing game ratings.
 * Provides endpoints for creating, reading, updating, and deleting ratings.
 */
@RestController
@RequestMapping("/api/ratings")
@Tag(name = "Rating", description = "Rating management APIs")
@Validated
@CrossOrigin(origins = "${app.cors.allowed-origins}", maxAge = 3600)
public class RatingController {

    private final RatingService ratingService;
    private final GameService gameService;
    private final UserService userService;

    @Autowired
    public RatingController(RatingService ratingService, GameService gameService, UserService userService) {
        this.ratingService = ratingService;
        this.gameService = gameService;
        this.userService = userService;
    }

    @PutMapping("/games/{gameId}")
    @Operation(
        summary = "Rate a game",
        description = "Add or update a rating for a game. Rating must be between 0 and 100."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rating successfully added/updated"),
        @ApiResponse(responseCode = "400", description = "Invalid rating value (must be between 0 and 100)"),
        @ApiResponse(responseCode = "404", description = "Game not found"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> rateGame(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @Parameter(description = "ID of the game to rate") Long gameId,
            @RequestParam @Min(value = 0, message = "Rating must be at least 0") 
            @Max(value = 100, message = "Rating must not exceed 100") Integer ratingValue) {
        
        try {
            User user = (User) userDetails;
            Game game = gameService.findById(gameId)
                    .orElseThrow(() -> new IllegalArgumentException("Game with ID " + gameId + " not found"));

            Rating rating = ratingService.addOrUpdateRating(user, game, ratingValue);
            return ResponseEntity.ok(rating);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Game Not Found", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                            "Internal Server Error", "An error occurred while processing your request"));
        }
    }

    @GetMapping("/games/{gameId}")
    @Operation(
        summary = "Get game ratings",
        description = "Get all ratings for a specific game with pagination support"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved game ratings"),
        @ApiResponse(responseCode = "404", description = "Game not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getGameRatings(
            @PathVariable @Parameter(description = "ID of the game to get ratings for") Long gameId,
            @Parameter(description = "Pagination parameters (page, size, sort)") Pageable pageable) {
        
        try {
            Game game = gameService.findById(gameId)
                    .orElseThrow(() -> new IllegalArgumentException("Game with ID " + gameId + " not found"));
            
            Page<Rating> ratings = ratingService.getGameRatings(game, pageable);
            return ResponseEntity.ok(ratings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Game Not Found", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                            "Internal Server Error", "An error occurred while retrieving ratings"));
        }
    }

    @GetMapping("/games/{gameId}/average")
    @Operation(
        summary = "Get average game rating",
        description = "Get the average rating for a specific game"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved average rating"),
        @ApiResponse(responseCode = "404", description = "Game not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getAverageGameRating(
            @PathVariable @Parameter(description = "ID of the game to get average rating for") Long gameId) {
        try {
            Double averageRating = ratingService.getAverageRating(gameId);
            return ResponseEntity.ok(averageRating);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Game Not Found", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                            "Internal Server Error", "An error occurred while calculating average rating"));
        }
    }

    @GetMapping("/users/{username}")
    @Operation(
        summary = "Get user ratings",
        description = "Get all ratings by a specific user with pagination support"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved user ratings"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getUserRatings(
            @PathVariable @Parameter(description = "Username of the user to get ratings for") String username,
            @Parameter(description = "Pagination parameters (page, size, sort)") Pageable pageable) {
        
        try {
            User user = (User) userService.loadUserByUsername(username);
            Page<Rating> ratings = ratingService.getUserRatings(user, pageable);
            return ResponseEntity.ok(ratings);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "User Not Found", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                            "Internal Server Error", "An error occurred while retrieving user ratings"));
        }
    }

    @GetMapping("/users/me/games/{gameId}")
    @Operation(
        summary = "Get user's game rating",
        description = "Get the authenticated user's rating for a specific game"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved rating"),
        @ApiResponse(responseCode = "404", description = "Rating or game not found"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getUserGameRating(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @Parameter(description = "ID of the game to get rating for") Long gameId) {
        
        try {
            User user = (User) userDetails;
            Game game = gameService.findById(gameId)
                    .orElseThrow(() -> new IllegalArgumentException("Game with ID " + gameId + " not found"));
            
            Optional<Rating> rating = ratingService.getUserRatingForGame(user, game);
            return rating.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Game Not Found", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                            "Internal Server Error", "An error occurred while retrieving the rating"));
        }
    }

    @DeleteMapping("/users/me/games/{gameId}")
    @Operation(
        summary = "Delete rating",
        description = "Delete the authenticated user's rating for a specific game"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Rating successfully deleted"),
        @ApiResponse(responseCode = "404", description = "Rating or game not found"),
        @ApiResponse(responseCode = "401", description = "User not authenticated"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> deleteRating(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @Parameter(description = "ID of the game to delete rating for") Long gameId) {
        
        try {
            User user = (User) userDetails;
            Game game = gameService.findById(gameId)
                    .orElseThrow(() -> new IllegalArgumentException("Game with ID " + gameId + " not found"));
            
            Optional<Rating> rating = ratingService.getUserRatingForGame(user, game);
            if (rating.isPresent()) {
                ratingService.deleteRating(rating.get());
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(HttpStatus.NOT_FOUND.value(), "Game Not Found", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                            "Internal Server Error", "An error occurred while deleting the rating"));
        }
    }
} 