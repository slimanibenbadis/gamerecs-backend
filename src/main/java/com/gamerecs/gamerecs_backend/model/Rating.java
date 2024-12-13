package com.gamerecs.gamerecs_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity class representing a user's rating for a game.
 * RatingValue must be between 0 and 100 (inclusive).
 * PercentileRank must be between 0 and 99 (inclusive) when set.
 */
@Entity
@Table(name = "Rating")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RatingID")
    private Long ratingId;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @NotNull(message = "Game is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GameID", nullable = false)
    private Game game;

    @NotNull(message = "Rating value is required")
    @Min(value = 0, message = "Rating must be at least 0")
    @Max(value = 100, message = "Rating must not exceed 100")
    @Column(name = "RatingValue", nullable = false)
    private Integer ratingValue;

    @Min(value = 0, message = "Percentile rank must be at least 0")
    @Max(value = 99, message = "Percentile rank must not exceed 99")
    @Column(name = "PercentileRank")
    private Integer percentileRank;

    @NotNull(message = "Date updated is required")
    @PastOrPresent(message = "Date updated cannot be in the future")
    @Column(name = "DateUpdated", nullable = false)
    private LocalDateTime dateUpdated;

    // Default constructor
    public Rating() {
        this.dateUpdated = LocalDateTime.now();
    }

    // Constructor with required fields
    public Rating(User user, Game game, Integer ratingValue) {
        this.user = user;
        this.game = game;
        this.ratingValue = ratingValue;
        this.dateUpdated = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getRatingId() {
        return ratingId;
    }

    public void setRatingId(Long ratingId) {
        this.ratingId = ratingId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Integer getRatingValue() {
        return ratingValue;
    }

    public void setRatingValue(Integer ratingValue) {
        this.ratingValue = ratingValue;
    }

    public Integer getPercentileRank() {
        return percentileRank;
    }

    public void setPercentileRank(Integer percentileRank) {
        this.percentileRank = percentileRank;
    }

    public LocalDateTime getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(LocalDateTime dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rating rating = (Rating) o;
        return Objects.equals(ratingId, rating.ratingId) &&
               Objects.equals(user.getUserId(), rating.user.getUserId()) &&
               Objects.equals(game.getGameId(), rating.game.getGameId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(ratingId, user.getUserId(), game.getGameId());
    }

    @Override
    public String toString() {
        return "Rating{" +
                "ratingId=" + ratingId +
                ", userId=" + (user != null ? user.getUserId() : null) +
                ", gameId=" + (game != null ? game.getGameId() : null) +
                ", ratingValue=" + ratingValue +
                ", percentileRank=" + percentileRank +
                ", dateUpdated=" + dateUpdated +
                '}';
    }
} 