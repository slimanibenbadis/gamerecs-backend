package com.gamerecs.gamerecs_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Entity class representing a game in a user's backlog.
 * Each user can have multiple games in their backlog, but each game can only appear once per user.
 */
@Entity
@Table(
    name = "BacklogItem",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "unique_user_game_backlog",
            columnNames = {"UserID", "GameID"}
        )
    }
)
public class BacklogItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BacklogItemID")
    private Long backlogItemId;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @NotNull(message = "Game is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GameID", nullable = false, foreignKey = @jakarta.persistence.ForeignKey(
        name = "fk_backlog_game",
        foreignKeyDefinition = "FOREIGN KEY (GameID) REFERENCES Game(GameID) ON DELETE CASCADE"
    ))
    private Game game;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false)
    private BacklogStatus status;

    // Default constructor
    public BacklogItem() {
    }

    // Constructor with required fields
    public BacklogItem(User user, Game game, BacklogStatus status) {
        this.user = user;
        this.game = game;
        this.status = status;
    }

    // Getters and Setters
    public Long getBacklogItemId() {
        return backlogItemId;
    }

    public void setBacklogItemId(Long backlogItemId) {
        this.backlogItemId = backlogItemId;
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

    public BacklogStatus getStatus() {
        return status;
    }

    public void setStatus(BacklogStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BacklogItem that = (BacklogItem) o;
        return Objects.equals(backlogItemId, that.backlogItemId) &&
               Objects.equals(user.getUserId(), that.user.getUserId()) &&
               Objects.equals(game.getGameId(), that.game.getGameId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(backlogItemId, user.getUserId(), game.getGameId());
    }

    @Override
    public String toString() {
        return "BacklogItem{" +
                "backlogItemId=" + backlogItemId +
                ", userId=" + user.getUserId() +
                ", gameId=" + game.getGameId() +
                ", status=" + status +
                '}';
    }
} 