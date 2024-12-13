package com.gamerecs.gamerecs_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Entity class representing a game in the system.
 */
@Entity
@Table(name = "Game")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GameID")
    private Long gameId;

    @Column(name = "IGDBID", unique = true)
    private Long igdbId;

    @NotBlank(message = "Game title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    @Column(name = "Title", nullable = false)
    private String title;

    @ElementCollection
    @Column(name = "Genres")
    private List<String> genres;

    @ElementCollection
    @Column(name = "Platforms")
    private List<String> platforms;

    @Column(name = "ReleaseDate")
    private LocalDate releaseDate;

    @Column(name = "Description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "CoverImageURL", columnDefinition = "TEXT")
    private String coverImageURL;

    @Size(max = 255, message = "Developer name cannot exceed 255 characters")
    @Column(name = "Developer")
    private String developer;

    @Size(max = 255, message = "Publisher name cannot exceed 255 characters")
    @Column(name = "Publisher")
    private String publisher;

    // Default constructor
    public Game() {
    }

    // Constructor with required fields
    public Game(String title) {
        this.title = title;
    }

    // Getters and Setters
    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public Long getIgdbId() {
        return igdbId;
    }

    public void setIgdbId(Long igdbId) {
        this.igdbId = igdbId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public List<String> getPlatforms() {
        return platforms;
    }

    public void setPlatforms(List<String> platforms) {
        this.platforms = platforms;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverImageURL() {
        return coverImageURL;
    }

    public void setCoverImageURL(String coverImageURL) {
        this.coverImageURL = coverImageURL;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return Objects.equals(gameId, game.gameId) &&
               Objects.equals(igdbId, game.igdbId) &&
               Objects.equals(title, game.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameId, igdbId, title);
    }

    @Override
    public String toString() {
        return "Game{" +
                "gameId=" + gameId +
                ", igdbId=" + igdbId +
                ", title='" + title + '\'' +
                ", genres=" + genres +
                ", platforms=" + platforms +
                ", releaseDate=" + releaseDate +
                ", developer='" + developer + '\'' +
                ", publisher='" + publisher + '\'' +
                '}';
    }
} 