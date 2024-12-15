package com.gamerecs.gamerecs_backend.service;

import com.gamerecs.gamerecs_backend.exception.BacklogException;
import com.gamerecs.gamerecs_backend.model.BacklogItem;
import com.gamerecs.gamerecs_backend.model.BacklogStatus;
import com.gamerecs.gamerecs_backend.model.Game;
import com.gamerecs.gamerecs_backend.model.User;
import com.gamerecs.gamerecs_backend.repository.BacklogItemRepository;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

/**
 * Service implementation for managing user's game backlog.
 * Implements business logic for backlog operations with proper validation and transaction management.
 */
@Service
@Validated
@Transactional
public class BacklogItemService implements IBacklogItemService {

    private final BacklogItemRepository backlogItemRepository;
    private final GameService gameService;

    @Autowired
    public BacklogItemService(BacklogItemRepository backlogItemRepository, GameService gameService) {
        this.backlogItemRepository = backlogItemRepository;
        this.gameService = gameService;
    }

    @Override
    @Transactional
    @CacheEvict(value = "userBacklog", key = "#user.userId")
    public BacklogItem addToBacklog(@NotNull User user, @NotNull Game game, @NotNull BacklogStatus status) {
        // Validate game exists
        gameService.findById(game.getGameId())
            .orElseThrow(() -> new BacklogException("Game not found with ID: " + game.getGameId()));

        // Check if game is already in backlog
        Optional<BacklogItem> existingItem = backlogItemRepository.findByUserAndGame(user, game);
        if (existingItem.isPresent()) {
            throw new BacklogException("Game is already in user's backlog");
        }

        // Create and save new backlog item
        BacklogItem backlogItem = new BacklogItem(user, game, status);
        return backlogItemRepository.save(backlogItem);
    }

    @Override
    @Transactional
    @CacheEvict(value = "userBacklog", key = "#user.userId")
    public BacklogItem updateGameStatus(@NotNull User user, @NotNull Game game, @NotNull BacklogStatus newStatus) {
        // Find existing backlog item
        BacklogItem backlogItem = backlogItemRepository.findByUserAndGame(user, game)
            .orElseThrow(() -> new BacklogException("Game not found in user's backlog"));

        // Validate status transition
        validateStatusTransition(backlogItem.getStatus(), newStatus);

        // Update status
        backlogItem.setStatus(newStatus);
        return backlogItemRepository.save(backlogItem);
    }

    @Override
    @Transactional
    @CacheEvict(value = "userBacklog", key = "#user.userId")
    public void removeFromBacklog(@NotNull User user, @NotNull Game game) {
        BacklogItem backlogItem = backlogItemRepository.findByUserAndGame(user, game)
            .orElseThrow(() -> new BacklogException("Game not found in user's backlog"));
        
        backlogItemRepository.delete(backlogItem);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "userBacklog", key = "{#user.userId, #status}")
    public Page<BacklogItem> getUserBacklog(@NotNull User user, BacklogStatus status, Pageable pageable) {
        return status == null ? 
            backlogItemRepository.findByUser(user, pageable) :
            backlogItemRepository.findByUserAndStatus(user, status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "backlogStats", key = "#user.userId")
    public Map<BacklogStatus, Long> getBacklogStatistics(@NotNull User user) {
        Map<BacklogStatus, Long> stats = new EnumMap<>(BacklogStatus.class);
        
        // Initialize all statuses with 0 count
        for (BacklogStatus status : BacklogStatus.values()) {
            stats.put(status, 0L);
        }
        
        // Update counts for existing statuses
        for (BacklogStatus status : BacklogStatus.values()) {
            long count = backlogItemRepository.countByUserAndStatus(user, status);
            stats.put(status, count);
        }
        
        return stats;
    }

    @Override
    @Transactional
    @CacheEvict(value = {"userBacklog", "backlogStats"}, key = "#user.userId")
    public List<BacklogItem> batchUpdateStatus(@NotNull User user, @NotNull Map<Game, BacklogStatus> gameStatusMap) {
        List<BacklogItem> updatedItems = new ArrayList<>();
        
        for (Map.Entry<Game, BacklogStatus> entry : gameStatusMap.entrySet()) {
            Game game = entry.getKey();
            BacklogStatus newStatus = entry.getValue();
            
            try {
                BacklogItem updated = updateGameStatus(user, game, newStatus);
                updatedItems.add(updated);
            } catch (BacklogException e) {
                // Log error and continue with other updates
                // Consider wrapping in a BatchUpdateException if needed
            }
        }
        
        return updatedItems;
    }

    /**
     * Validates if the status transition is allowed based on business rules.
     * @param currentStatus current status of the backlog item
     * @param newStatus requested new status
     * @throws BacklogException if the transition is not allowed
     */
    private void validateStatusTransition(BacklogStatus currentStatus, BacklogStatus newStatus) {
        // Implement status transition rules
        if (currentStatus == BacklogStatus.COMPLETED && newStatus == BacklogStatus.TO_PLAY) {
            throw new BacklogException("Cannot change status from Completed to To Play");
        }
        
        // Add more transition rules as needed
    }
} 