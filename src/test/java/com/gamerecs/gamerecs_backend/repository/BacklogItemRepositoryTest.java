package com.gamerecs.gamerecs_backend.repository;

import com.gamerecs.gamerecs_backend.model.BacklogItem;
import com.gamerecs.gamerecs_backend.model.BacklogStatus;
import com.gamerecs.gamerecs_backend.model.Game;
import com.gamerecs.gamerecs_backend.model.User;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@DataJpaTest
class BacklogItemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BacklogItemRepository backlogItemRepository;

    private User testUser;
    private Game testGame1;
    private Game testGame2;
    private BacklogItem testBacklogItem1;
    private BacklogItem testBacklogItem2;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User("testuser", "test@example.com", "hashedPassword123");
        testUser.setBio("Test bio");
        testUser.setProfilePictureURL("http://example.com/pic.jpg");
        testUser.setJoinDate(Timestamp.from(Instant.now()));
        testUser.setLastLogin(Timestamp.from(Instant.now()));
        entityManager.persist(testUser);

        // Create test games
        testGame1 = new Game();
        testGame1.setTitle("Test Game 1");
        testGame1.setIgdbId(1L);
        testGame1.setGenres(Arrays.asList("Action", "Adventure"));
        testGame1.setPlatforms(Arrays.asList("PC", "PS5"));
        entityManager.persist(testGame1);

        testGame2 = new Game();
        testGame2.setTitle("Test Game 2");
        testGame2.setIgdbId(2L);
        testGame2.setGenres(Arrays.asList("RPG", "Strategy"));
        testGame2.setPlatforms(Arrays.asList("Xbox Series X", "Nintendo Switch"));
        entityManager.persist(testGame2);

        // Create test backlog items
        testBacklogItem1 = new BacklogItem(testUser, testGame1, BacklogStatus.TO_PLAY);
        testBacklogItem2 = new BacklogItem(testUser, testGame2, BacklogStatus.IN_PROGRESS);

        entityManager.persist(testBacklogItem1);
        entityManager.persist(testBacklogItem2);
        entityManager.flush();
    }

    @Test
    void shouldSaveBacklogItem() {
        // given
        Game testGame3 = new Game();
        testGame3.setTitle("Test Game 3");
        testGame3.setIgdbId(3L);
        testGame3.setGenres(Arrays.asList("RPG", "Action"));
        testGame3.setPlatforms(Arrays.asList("PC", "PS5"));
        entityManager.persist(testGame3);
        
        BacklogItem newBacklogItem = new BacklogItem(testUser, testGame3, BacklogStatus.TO_PLAY);

        // when
        BacklogItem savedBacklogItem = backlogItemRepository.save(newBacklogItem);

        // then
        assertThat(savedBacklogItem).isNotNull();
        assertThat(savedBacklogItem.getBacklogItemId()).isNotNull();
        assertThat(savedBacklogItem.getUser()).isEqualTo(testUser);
        assertThat(savedBacklogItem.getGame()).isEqualTo(testGame3);
        assertThat(savedBacklogItem.getStatus()).isEqualTo(BacklogStatus.TO_PLAY);
    }

    @Test
    void shouldFindByUser() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<BacklogItem> foundItems = backlogItemRepository.findByUser(testUser, pageable);

        // then
        assertThat(foundItems).isNotNull();
        assertThat(foundItems.getContent()).hasSize(2);
        assertThat(foundItems.getContent()).contains(testBacklogItem1, testBacklogItem2);
    }

    @Test
    void shouldFindByUserAndStatus() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<BacklogItem> foundItems = backlogItemRepository.findByUserAndStatus(testUser, BacklogStatus.TO_PLAY, pageable);

        // then
        assertThat(foundItems).isNotNull();
        assertThat(foundItems.getContent()).hasSize(1);
        assertThat(foundItems.getContent().get(0)).isEqualTo(testBacklogItem1);
    }

    @Test
    void shouldFindByUserAndGame() {
        // when
        Optional<BacklogItem> foundItem = backlogItemRepository.findByUserAndGame(testUser, testGame1);

        // then
        assertThat(foundItem).isPresent();
        assertThat(foundItem.get()).isEqualTo(testBacklogItem1);
    }

    @Test
    void shouldCountByUserAndStatus() {
        // when
        long count = backlogItemRepository.countByUserAndStatus(testUser, BacklogStatus.TO_PLAY);

        // then
        assertThat(count).isEqualTo(1);
    }

    @Test
    void shouldFindByUserAndStatusIn() {
        // given
        List<BacklogStatus> statuses = Arrays.asList(BacklogStatus.TO_PLAY, BacklogStatus.IN_PROGRESS);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<BacklogItem> foundItems = backlogItemRepository.findByUserAndStatusIn(testUser, statuses, pageable);

        // then
        assertThat(foundItems).isNotNull();
        assertThat(foundItems.getContent()).hasSize(2);
        assertThat(foundItems.getContent()).contains(testBacklogItem1, testBacklogItem2);
    }

    @Test
    void shouldFindByUserWithGame() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("game.title"));

        // when
        Page<BacklogItem> foundItems = backlogItemRepository.findByUserWithGame(testUser, pageable);

        // then
        assertThat(foundItems).isNotNull();
        assertThat(foundItems.getContent()).hasSize(2);
        assertThat(foundItems.getContent()).contains(testBacklogItem1, testBacklogItem2);
    }

    @Test
    void shouldDeleteByUser() {
        // when
        backlogItemRepository.deleteByUser(testUser);
        entityManager.flush();

        // then
        Page<BacklogItem> remainingItems = backlogItemRepository.findByUser(testUser, PageRequest.of(0, 10));
        assertThat(remainingItems.getContent()).isEmpty();
    }

    @Test
    void shouldDeleteByGame() {
        // First, delete the backlog items associated with the game
        backlogItemRepository.deleteByGame(testGame1);
        entityManager.flush();
        
        // Then remove the game
        entityManager.remove(testGame1);
        entityManager.flush();
        
        // then
        Optional<BacklogItem> deletedItem = backlogItemRepository.findByUserAndGame(testUser, testGame1);
        assertThat(deletedItem).isEmpty();
    }

    @Test
    void shouldEnforceUniqueUserGameConstraint() {
        // given
        BacklogItem duplicateBacklogItem = new BacklogItem(testUser, testGame1, BacklogStatus.COMPLETED);

        // when/then
        assertThat(
            org.junit.jupiter.api.Assertions.assertThrows(
                org.hibernate.exception.ConstraintViolationException.class,
                () -> entityManager.persistAndFlush(duplicateBacklogItem)
            )
        ).isNotNull();
    }

    @Test
    void shouldSortBacklogItemsByGameTitle() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("game.title"));

        // when
        Page<BacklogItem> sortedItems = backlogItemRepository.findByUserWithGame(testUser, pageable);

        // then
        assertThat(sortedItems.getContent()).hasSize(2);
        assertThat(sortedItems.getContent().get(0).getGame().getTitle())
            .isLessThan(sortedItems.getContent().get(1).getGame().getTitle());
    }

    @Test
    void shouldTestLazyLoadingOfUserAndGame() {
        // when
        BacklogItem loadedBacklogItem = backlogItemRepository.findById(testBacklogItem1.getBacklogItemId()).orElseThrow();
        
        // then
        assertThat(loadedBacklogItem).isNotNull();
        // Access lazy-loaded properties to ensure they're properly mapped
        assertThat(loadedBacklogItem.getUser().getUsername()).isEqualTo("testuser");
        assertThat(loadedBacklogItem.getGame().getTitle()).isEqualTo("Test Game 1");
    }

    @Test
    void shouldCascadeDeleteWhenUserIsDeleted() {
        // Verify backlog items exist before deletion
        Page<BacklogItem> beforeDelete = backlogItemRepository.findByUser(testUser, PageRequest.of(0, 10));
        assertThat(beforeDelete.getContent()).hasSize(2);
        
        // First delete the backlog items
        backlogItemRepository.deleteByUser(testUser);
        entityManager.flush();
        
        // Then delete the user
        entityManager.remove(testUser);
        entityManager.flush();
        entityManager.clear(); // Clear persistence context to ensure fresh data load
        
        // Verify backlog items were deleted
        Page<BacklogItem> afterDelete = backlogItemRepository.findByUser(testUser, PageRequest.of(0, 10));
        assertThat(afterDelete.getContent()).isEmpty();
    }

    @Test
    void shouldValidateNotNullConstraints() {
        // given
        BacklogItem invalidBacklogItem = new BacklogItem();
        
        // when/then
        assertThat(
            org.junit.jupiter.api.Assertions.assertThrows(
                jakarta.validation.ConstraintViolationException.class,
                () -> entityManager.persist(invalidBacklogItem)
            )
        ).isNotNull();
    }

    @Test
    void shouldValidateStatusEnumConstraint() {
        // given
        BacklogItem backlogItem = new BacklogItem(testUser, testGame1, null);
        
        // when/then
        assertThat(
            org.junit.jupiter.api.Assertions.assertThrows(
                jakarta.validation.ConstraintViolationException.class,
                () -> entityManager.persist(backlogItem)
            )
        ).isNotNull();
    }

    @Test
    void shouldMaintainBidirectionalRelationship() {
        // given
        User newUser = new User("newuser", "newuser@example.com", "password123");
        entityManager.persist(newUser);
        
        // when
        testBacklogItem1.setUser(newUser);
        backlogItemRepository.save(testBacklogItem1);
        entityManager.flush();
        entityManager.clear();
        
        // then
        BacklogItem reloadedBacklogItem = backlogItemRepository.findById(testBacklogItem1.getBacklogItemId()).orElseThrow();
        assertThat(reloadedBacklogItem.getUser().getUsername()).isEqualTo("newuser");
    }

    @Test
    void shouldHandleBatchOperations() {
        // given
        Game testGame3 = new Game();
        testGame3.setTitle("Test Game 3");
        testGame3.setIgdbId(3L);
        testGame3.setGenres(Arrays.asList("RPG", "Action"));
        testGame3.setPlatforms(Arrays.asList("PC", "PS5"));
        entityManager.persist(testGame3);

        Game testGame4 = new Game();
        testGame4.setTitle("Test Game 4");
        testGame4.setIgdbId(4L);
        testGame4.setGenres(Arrays.asList("Strategy", "Simulation"));
        testGame4.setPlatforms(Arrays.asList("PC", "Xbox"));
        entityManager.persist(testGame4);
        
        List<BacklogItem> batchItems = Arrays.asList(
            new BacklogItem(testUser, testGame3, BacklogStatus.COMPLETED),
            new BacklogItem(testUser, testGame4, BacklogStatus.ABANDONED)
        );
        
        // when
        backlogItemRepository.saveAll(batchItems);
        entityManager.flush();
        
        // then
        Page<BacklogItem> userItems = backlogItemRepository.findByUserAndStatusIn(
            testUser, 
            Arrays.asList(BacklogStatus.COMPLETED, BacklogStatus.ABANDONED), 
            PageRequest.of(0, 10)
        );
        assertThat(userItems.getContent()).hasSize(2);
        assertThat(userItems.getContent())
            .extracting(BacklogItem::getStatus)
            .containsExactlyInAnyOrder(BacklogStatus.COMPLETED, BacklogStatus.ABANDONED);
    }

    @Test
    void shouldCascadeDeleteWhenGameIsDeleted() {
        // Verify backlog item exists before deletion
        Optional<BacklogItem> beforeDelete = backlogItemRepository.findByUserAndGame(testUser, testGame1);
        assertThat(beforeDelete).isPresent();
        
        // First delete the backlog items
        backlogItemRepository.deleteByGame(testGame1);
        entityManager.flush();
        
        // Then delete the game
        entityManager.remove(testGame1);
        entityManager.flush();
        entityManager.clear(); // Clear persistence context to ensure fresh data load
        
        // Verify backlog item was deleted
        Optional<BacklogItem> afterDelete = backlogItemRepository.findByUserAndGame(testUser, testGame1);
        assertThat(afterDelete).isEmpty();
    }
} 