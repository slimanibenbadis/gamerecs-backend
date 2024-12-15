package com.gamerecs.gamerecs_backend.service;

import com.gamerecs.gamerecs_backend.model.BacklogItem;
import com.gamerecs.gamerecs_backend.model.BacklogStatus;
import com.gamerecs.gamerecs_backend.model.Game;
import com.gamerecs.gamerecs_backend.model.User;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for managing user's game backlog.
 * Provides operations for adding, updating, removing, and querying backlog items.
 */
public interface IBacklogItemService {

    /**
     * Add a game to user's backlog
     * @param user the user
     * @param game the game to add
     * @param status initial status of the game
     * @return the created backlog item
     * @throws IllegalArgumentException if the game is already in user's backlog
     */
    BacklogItem addToBacklog(User user, Game game, BacklogStatus status);

    /**
     * Update the status of a game in user's backlog
     * @param user the user
     * @param game the game to update
     * @param newStatus new status to set
     * @return the updated backlog item
     * @throws IllegalArgumentException if the game is not in user's backlog
     */
    BacklogItem updateGameStatus(User user, Game game, BacklogStatus newStatus);

    /**
     * Remove a game from user's backlog
     * @param user the user
     * @param game the game to remove
     * @throws IllegalArgumentException if the game is not in user's backlog
     */
    void removeFromBacklog(User user, Game game);

    /**
     * Get user's backlog with optional filtering
     * @param user the user
     * @param status optional status filter
     * @param pageable pagination information
     * @return Page of backlog items
     */
    Page<BacklogItem> getUserBacklog(User user, BacklogStatus status, Pageable pageable);

    /**
     * Get user's backlog statistics
     * @param user the user
     * @return Map containing count of games in each status
     */
    Map<BacklogStatus, Long> getBacklogStatistics(User user);

    /**
     * Batch update statuses for multiple games
     * @param user the user
     * @param gameStatusMap map of games to their new statuses
     * @return list of updated backlog items
     * @throws IllegalArgumentException if any game is not in user's backlog
     */
    List<BacklogItem> batchUpdateStatus(User user, Map<Game, BacklogStatus> gameStatusMap);
} 