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
        // when
        backlogItemRepository.deleteByGame(testGame1);
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
} 