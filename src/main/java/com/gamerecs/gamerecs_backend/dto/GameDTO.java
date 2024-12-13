package com.gamerecs.gamerecs_backend.dto;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameDTO {
    private Long gameId;
    private Long igdbId;
    
    @NotBlank(message = "Game title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;
    
    private List<String> genres;
    private List<String> platforms;
    private LocalDate releaseDate;
    private String description;
    private String coverImageURL;
    
    @Size(max = 255, message = "Developer name cannot exceed 255 characters")
    private String developer;
    
    @Size(max = 255, message = "Publisher name cannot exceed 255 characters")
    private String publisher;
    
    private Double averageRating;
    private Long totalRatings;
} 