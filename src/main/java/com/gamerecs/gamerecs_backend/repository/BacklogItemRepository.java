package com.gamerecs.gamerecs_backend.repository;

import com.gamerecs.gamerecs_backend.model.BacklogItem;
import com.gamerecs.gamerecs_backend.model.BacklogStatus;
import com.gamerecs.gamerecs_backend.model.Game;
import com.gamerecs.gamerecs_backend.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for BacklogItem entity providing CRUD operations and custom queries.
 */
@Repository
public interface BacklogItemRepository extends JpaRepository<BacklogItem, Long> {

    /**
     * Find all backlog items for a specific user
     * @param user the user whose backlog items to find
     * @param pageable pagination information
     * @return Page of backlog items
     */
    Page<BacklogItem> findByUser(User user, Pageable pageable);

    /**
     * Find all backlog items for a specific user with a specific status
     * @param user the user whose backlog items to find
     * @param status the status to filter by
     * @param pageable pagination information
     * @return Page of backlog items
     */
    Page<BacklogItem> findByUserAndStatus(User user, BacklogStatus status, Pageable pageable);

    /**
     * Find a specific backlog item by user and game
     * @param user the user
     * @param game the game
     * @return Optional containing the backlog item if found
     */
    Optional<BacklogItem> findByUserAndGame(User user, Game game);

    /**
     * Count backlog items for a user with a specific status
     * @param user the user
     * @param status the status to count
     * @return the count of backlog items
     */
    long countByUserAndStatus(User user, BacklogStatus status);

    /**
     * Find backlog items for a user with any of the specified statuses
     * @param user the user
     * @param statuses list of statuses to include
     * @param pageable pagination information
     * @return Page of backlog items
     */
    @Query("SELECT bi FROM BacklogItem bi WHERE bi.user = :user AND bi.status IN :statuses")
    Page<BacklogItem> findByUserAndStatusIn(@Param("user") User user, @Param("statuses") List<BacklogStatus> statuses, Pageable pageable);

    /**
     * Find backlog items for a user sorted by game attributes
     * @param user the user
     * @param pageable pagination information with sort criteria (e.g., "game.title" or "game.releaseDate")
     * @return Page of backlog items
     */
    @Query("SELECT bi FROM BacklogItem bi JOIN FETCH bi.game WHERE bi.user = :user")
    Page<BacklogItem> findByUserWithGame(@Param("user") User user, Pageable pageable);

    /**
     * Delete all backlog items for a specific user
     * @param user the user whose backlog items should be deleted
     */
    void deleteByUser(User user);

    /**
     * Delete all backlog items for a specific game
     * @param game the game whose backlog items should be deleted
     */
    void deleteByGame(Game game);
} 