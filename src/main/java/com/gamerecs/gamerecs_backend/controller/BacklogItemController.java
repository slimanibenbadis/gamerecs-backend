package com.gamerecs.gamerecs_backend.controller;

import com.gamerecs.gamerecs_backend.exception.BacklogException;
import com.gamerecs.gamerecs_backend.exception.ErrorResponse;
import com.gamerecs.gamerecs_backend.model.BacklogItem;
import com.gamerecs.gamerecs_backend.model.BacklogStatus;
import com.gamerecs.gamerecs_backend.model.Game;
import com.gamerecs.gamerecs_backend.model.User;
import com.gamerecs.gamerecs_backend.security.UserDetailsImpl;
import com.gamerecs.gamerecs_backend.service.GameService;
import com.gamerecs.gamerecs_backend.service.IBacklogItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing user's game backlog.
 * Provides endpoints for adding, updating, removing, and querying backlog items.
 */
@RestController
@RequestMapping("/api/backlog")
@Tag(name = "Backlog", description = "Game backlog management APIs")
@CrossOrigin(origins = "${app.cors.allowed-origins}", maxAge = 3600)
public class BacklogItemController {

    private final IBacklogItemService backlogItemService;
    private final GameService gameService;

    @Autowired
    public BacklogItemController(IBacklogItemService backlogItemService,
                               GameService gameService) {
        this.backlogItemService = backlogItemService;
        this.gameService = gameService;
    }

    @Operation(summary = "Add game to backlog", description = "Add a game to the user's backlog with initial status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Game added to backlog successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "409", description = "Game already in backlog"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/add/{gameId}")
    public ResponseEntity<?> addToBacklog(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long gameId,
            @Valid @RequestBody BacklogStatus status) {
        try {
            User user = ((UserDetailsImpl) userDetails).getUser();
            Game game = gameService.findById(gameId)
                    .orElseThrow(() -> new BacklogException("Game not found with ID: " + gameId));
            
            BacklogItem backlogItem = backlogItemService.addToBacklog(user, game, status);
            return ResponseEntity.status(HttpStatus.CREATED).body(backlogItem);
        } catch (BacklogException e) {
            if (e.getMessage().contains("Game not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        "Not Found",
                        e.getMessage()
                    ));
            }
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(
                        HttpStatus.CONFLICT.value(),
                        "Backlog Error",
                        e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        "An error occurred while adding game to backlog"
                    ));
        }
    }

    @Operation(summary = "Update backlog item status", description = "Update the status of a game in user's backlog")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Status updated successfully"),
        @ApiResponse(responseCode = "404", description = "Backlog item not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{gameId}")
    public ResponseEntity<?> updateStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long gameId,
            @Valid @RequestParam BacklogStatus status) {
        try {
            User user = ((UserDetailsImpl) userDetails).getUser();
            Game game = gameService.findById(gameId)
                    .orElseThrow(() -> new BacklogException("Game not found with ID: " + gameId));
            
            BacklogItem backlogItem = backlogItemService.updateGameStatus(user, game, status);
            return ResponseEntity.ok(backlogItem);
        } catch (BacklogException e) {
            if (e.getMessage().contains("Invalid status")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Bad Request",
                        e.getMessage()
                    ));
            } else if (e.getMessage().contains("Game not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        "Not Found",
                        e.getMessage()
                    ));
            }
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(
                        HttpStatus.CONFLICT.value(),
                        "Backlog Error",
                        e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        "An error occurred while updating backlog status"
                    ));
        }
    }

    @Operation(summary = "Remove game from backlog", description = "Remove a game from user's backlog")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Game removed successfully"),
        @ApiResponse(responseCode = "404", description = "Backlog item not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{gameId}")
    public ResponseEntity<?> removeFromBacklog(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long gameId) {
        try {
            User user = ((UserDetailsImpl) userDetails).getUser();
            Game game = gameService.findById(gameId)
                    .orElseThrow(() -> new IllegalArgumentException("Game not found"));
            
            backlogItemService.removeFromBacklog(user, game);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        "Not Found",
                        e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        "An error occurred while removing game from backlog"
                    ));
        }
    }

    @Operation(summary = "Get user's backlog", description = "Get user's backlog with optional status filter and pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Backlog retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<?> getUserBacklog(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "Optional status filter") @RequestParam(required = false) BacklogStatus status,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        try {
            User user = ((UserDetailsImpl) userDetails).getUser();
            Page<BacklogItem> backlog = backlogItemService.getUserBacklog(user, status, pageable);
            return ResponseEntity.ok(backlog);
        } catch (BacklogException e) {
            if (e.getMessage().contains("not authorized")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(
                        HttpStatus.UNAUTHORIZED.value(),
                        "Unauthorized",
                        e.getMessage()
                    ));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Bad Request",
                        e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        "An error occurred while retrieving backlog"
                    ));
        }
    }

    @Operation(summary = "Get backlog statistics", description = "Get statistics about user's backlog")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/stats")
    public ResponseEntity<?> getBacklogStats(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = ((UserDetailsImpl) userDetails).getUser();
            Map<BacklogStatus, Long> stats = backlogItemService.getBacklogStatistics(user);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        "An error occurred while retrieving backlog statistics"
                    ));
        }
    }

    @Operation(summary = "Get backlog items by status", description = "Get user's backlog items filtered by status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Backlog items retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid status"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getBacklogByStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable BacklogStatus status,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        try {
            User user = ((UserDetailsImpl) userDetails).getUser();
            Page<BacklogItem> backlog = backlogItemService.getUserBacklog(user, status, pageable);
            return ResponseEntity.ok(backlog);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        "Invalid Status",
                        e.getMessage()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Internal Server Error",
                        "An error occurred while retrieving backlog items"
                    ));
        }
    }
} 