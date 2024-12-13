package com.gamerecs.gamerecs_backend.controller;

import com.gamerecs.gamerecs_backend.dto.GameDTO;
import com.gamerecs.gamerecs_backend.exception.ErrorResponse;
import com.gamerecs.gamerecs_backend.model.Game;
import com.gamerecs.gamerecs_backend.service.GameService;
import com.gamerecs.gamerecs_backend.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games")
@Tag(name = "Game Controller", description = "Endpoints for game management")
@CrossOrigin(origins = "${app.cors.allowed-origins}", maxAge = 3600)
public class GameController {

    private final GameService gameService;
    private final RatingService ratingService;

    @Autowired
    public GameController(GameService gameService, RatingService ratingService) {
        this.gameService = gameService;
        this.ratingService = ratingService;
    }

    @Operation(summary = "Get game by ID", description = "Retrieves a game by its ID with detailed information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Game found and returned successfully"),
            @ApiResponse(responseCode = "404", description = "Game not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getGameById(
            @PathVariable @Parameter(description = "ID of the game to retrieve") Long id) {
        try {
            Optional<Game> gameOpt = gameService.findById(id);
            if (gameOpt.isPresent()) {
                return ResponseEntity.ok(convertToDTO(gameOpt.get()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        "Game Not Found",
                        "Game with ID " + id + " not found"
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        "An error occurred while retrieving the game"
                    ));
        }
    }

    @Operation(
        summary = "Search games by title",
        description = "Searches for games with titles containing the search term, with pagination support"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/search")
    public ResponseEntity<?> searchGames(
            @RequestParam @Parameter(description = "Title to search for") String title,
            @Parameter(description = "Pagination parameters (page, size, sort)") Pageable pageable) {
        try {
            Page<Game> games = gameService.searchByTitle(title, pageable);
            Page<GameDTO> gameDTOs = games.map(this::convertToDTO);
            return ResponseEntity.ok(gameDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        "An error occurred while searching for games"
                    ));
        }
    }

    @Operation(
        summary = "Get games by genre",
        description = "Retrieves games of a specific genre with pagination support"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Games retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid genre parameter"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/genre/{genre}")
    public ResponseEntity<?> getGamesByGenre(
            @PathVariable @Parameter(description = "Genre to filter by") String genre,
            @Parameter(description = "Pagination parameters (page, size, sort)") Pageable pageable) {
        try {
            Page<Game> games = gameService.findByGenre(genre, pageable);
            Page<GameDTO> gameDTOs = games.map(this::convertToDTO);
            return ResponseEntity.ok(gameDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        "An error occurred while retrieving games by genre"
                    ));
        }
    }

    @Operation(
        summary = "Get games by platform",
        description = "Retrieves games available on a specific platform with pagination support"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Games retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid platform parameter"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/platform/{platform}")
    public ResponseEntity<?> getGamesByPlatform(
            @PathVariable @Parameter(description = "Platform to filter by") String platform,
            @Parameter(description = "Pagination parameters (page, size, sort)") Pageable pageable) {
        try {
            Page<Game> games = gameService.findByPlatform(platform, pageable);
            Page<GameDTO> gameDTOs = games.map(this::convertToDTO);
            return ResponseEntity.ok(gameDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        "An error occurred while retrieving games by platform"
                    ));
        }
    }

    @Operation(
        summary = "Get games by developer",
        description = "Retrieves games from a specific developer with pagination support"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Games retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid developer parameter"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/developer/{developer}")
    public ResponseEntity<?> getGamesByDeveloper(
            @PathVariable @Parameter(description = "Developer to filter by") String developer,
            @Parameter(description = "Pagination parameters (page, size, sort)") Pageable pageable) {
        try {
            Page<Game> games = gameService.findByDeveloper(developer, pageable);
            Page<GameDTO> gameDTOs = games.map(this::convertToDTO);
            return ResponseEntity.ok(gameDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        "An error occurred while retrieving games by developer"
                    ));
        }
    }

    @Operation(
        summary = "Add a new game",
        description = "Adds a new game to the system. The game must have a unique title and valid required fields."
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "201",
                description = "Game created successfully"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid input data"
            ),
            @ApiResponse(
                responseCode = "409",
                description = "Game already exists"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
    })
    @PostMapping
    public ResponseEntity<?> addGame(
            @Valid @RequestBody @Parameter(description = "Game data to create") GameDTO gameDTO) {
        try {
            Game game = convertToEntity(gameDTO);
            Game savedGame = gameService.addGame(game);
            GameDTO savedGameDTO = convertToDTO(savedGame);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedGameDTO);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(
                        HttpStatus.CONFLICT.value(),
                        "Game Already Exists",
                        e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        "An error occurred while creating the game"
                    ));
        }
    }

    @Operation(
        summary = "Get all games",
        description = "Retrieves all games with pagination support, including their details and ratings"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Games retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<?> getAllGames(
            @Parameter(description = "Pagination parameters (page, size, sort)") Pageable pageable) {
        try {
            Page<Game> games = gameService.getAllGames(pageable);
            Page<GameDTO> gameDTOs = games.map(this::convertToDTO);
            return ResponseEntity.ok(gameDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        "An error occurred while retrieving games"
                    ));
        }
    }

    private GameDTO convertToDTO(Game game) {
        return GameDTO.builder()
                .gameId(game.getGameId())
                .igdbId(game.getIgdbId())
                .title(game.getTitle())
                .genres(game.getGenres())
                .platforms(game.getPlatforms())
                .releaseDate(game.getReleaseDate())
                .description(game.getDescription())
                .coverImageURL(game.getCoverImageURL())
                .developer(game.getDeveloper())
                .publisher(game.getPublisher())
                .averageRating(ratingService.getAverageRating(game.getGameId()))
                .totalRatings(ratingService.getRatingCount(game.getGameId()))
                .build();
    }

    private Game convertToEntity(GameDTO gameDTO) {
        Game game = new Game(gameDTO.getTitle());
        game.setIgdbId(gameDTO.getIgdbId());
        game.setGenres(gameDTO.getGenres());
        game.setPlatforms(gameDTO.getPlatforms());
        game.setReleaseDate(gameDTO.getReleaseDate());
        game.setDescription(gameDTO.getDescription());
        game.setCoverImageURL(gameDTO.getCoverImageURL());
        game.setDeveloper(gameDTO.getDeveloper());
        game.setPublisher(gameDTO.getPublisher());
        return game;
    }
} 