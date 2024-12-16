package com.gamerecs.gamerecs_backend.service;

import com.gamerecs.gamerecs_backend.exception.BacklogException;
import com.gamerecs.gamerecs_backend.model.BacklogItem;
import com.gamerecs.gamerecs_backend.model.BacklogStatus;
import com.gamerecs.gamerecs_backend.model.Game;
import com.gamerecs.gamerecs_backend.model.User;
import com.gamerecs.gamerecs_backend.repository.BacklogItemRepository;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class BacklogItemServiceTest {

    @Mock
    private BacklogItemRepository backlogItemRepository;

    @Mock
    private GameService gameService;

    @InjectMocks
    private BacklogItemService backlogItemService;

    private User testUser;
    private Game testGame;
    private BacklogItem testBacklogItem;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testUser");

        testGame = new Game();
        testGame.setGameId(1L);
        testGame.setTitle("Test Game");

        testBacklogItem = new BacklogItem(testUser, testGame, BacklogStatus.TO_PLAY);
        testBacklogItem.setBacklogItemId(1L);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void addToBacklog_NewGame_Success() {
        when(gameService.findById(testGame.getGameId())).thenReturn(Optional.of(testGame));
        when(backlogItemRepository.findByUserAndGame(testUser, testGame)).thenReturn(Optional.empty());
        when(backlogItemRepository.save(any(BacklogItem.class))).thenReturn(testBacklogItem);

        BacklogItem result = backlogItemService.addToBacklog(testUser, testGame, BacklogStatus.TO_PLAY);

        assertNotNull(result);
        assertEquals(testUser, result.getUser());
        assertEquals(testGame, result.getGame());
        assertEquals(BacklogStatus.TO_PLAY, result.getStatus());
        verify(backlogItemRepository).save(any(BacklogItem.class));
    }

    @Test
    void addToBacklog_GameNotFound_ThrowsException() {
        when(gameService.findById(testGame.getGameId())).thenReturn(Optional.empty());

        assertThrows(BacklogException.class, () ->
            backlogItemService.addToBacklog(testUser, testGame, BacklogStatus.TO_PLAY));

        verify(backlogItemRepository, never()).save(any(BacklogItem.class));
    }

    @Test
    void addToBacklog_GameAlreadyInBacklog_ThrowsException() {
        when(gameService.findById(testGame.getGameId())).thenReturn(Optional.of(testGame));
        when(backlogItemRepository.findByUserAndGame(testUser, testGame)).thenReturn(Optional.of(testBacklogItem));

        assertThrows(BacklogException.class, () ->
            backlogItemService.addToBacklog(testUser, testGame, BacklogStatus.TO_PLAY));

        verify(backlogItemRepository, never()).save(any(BacklogItem.class));
    }

    @Test
    void updateGameStatus_ValidTransition_Success() {
        when(backlogItemRepository.findByUserAndGame(testUser, testGame))
            .thenReturn(Optional.of(testBacklogItem));
        when(backlogItemRepository.save(any(BacklogItem.class)))
            .thenReturn(testBacklogItem);

        BacklogItem result = backlogItemService.updateGameStatus(testUser, testGame, BacklogStatus.IN_PROGRESS);

        assertNotNull(result);
        assertEquals(BacklogStatus.IN_PROGRESS, result.getStatus());
        verify(backlogItemRepository).save(any(BacklogItem.class));
    }

    @Test
    void updateGameStatus_InvalidTransition_ThrowsException() {
        testBacklogItem.setStatus(BacklogStatus.COMPLETED);
        when(backlogItemRepository.findByUserAndGame(testUser, testGame))
            .thenReturn(Optional.of(testBacklogItem));

        assertThrows(BacklogException.class, () ->
            backlogItemService.updateGameStatus(testUser, testGame, BacklogStatus.TO_PLAY));

        verify(backlogItemRepository, never()).save(any(BacklogItem.class));
    }

    @Test
    void updateGameStatus_GameNotInBacklog_ThrowsException() {
        when(backlogItemRepository.findByUserAndGame(testUser, testGame))
            .thenReturn(Optional.empty());

        assertThrows(BacklogException.class, () ->
            backlogItemService.updateGameStatus(testUser, testGame, BacklogStatus.IN_PROGRESS));

        verify(backlogItemRepository, never()).save(any(BacklogItem.class));
    }

    @Test
    void removeFromBacklog_ExistingGame_Success() {
        when(backlogItemRepository.findByUserAndGame(testUser, testGame))
            .thenReturn(Optional.of(testBacklogItem));
        doNothing().when(backlogItemRepository).delete(testBacklogItem);

        backlogItemService.removeFromBacklog(testUser, testGame);

        verify(backlogItemRepository).delete(testBacklogItem);
    }

    @Test
    void removeFromBacklog_GameNotInBacklog_ThrowsException() {
        when(backlogItemRepository.findByUserAndGame(testUser, testGame))
            .thenReturn(Optional.empty());

        assertThrows(BacklogException.class, () ->
            backlogItemService.removeFromBacklog(testUser, testGame));

        verify(backlogItemRepository, never()).delete(any(BacklogItem.class));
    }

    @Test
    void getUserBacklog_WithoutStatusFilter_Success() {
        List<BacklogItem> items = Collections.singletonList(testBacklogItem);
        Page<BacklogItem> page = new PageImpl<>(items, pageable, 1);
        when(backlogItemRepository.findByUser(testUser, pageable)).thenReturn(page);

        Page<BacklogItem> result = backlogItemService.getUserBacklog(testUser, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testBacklogItem, result.getContent().get(0));
        verify(backlogItemRepository).findByUser(testUser, pageable);
    }

    @Test
    void getUserBacklog_WithStatusFilter_Success() {
        List<BacklogItem> items = Collections.singletonList(testBacklogItem);
        Page<BacklogItem> page = new PageImpl<>(items, pageable, 1);
        when(backlogItemRepository.findByUserAndStatus(testUser, BacklogStatus.TO_PLAY, pageable))
            .thenReturn(page);

        Page<BacklogItem> result = backlogItemService.getUserBacklog(testUser, BacklogStatus.TO_PLAY, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testBacklogItem, result.getContent().get(0));
        verify(backlogItemRepository).findByUserAndStatus(testUser, BacklogStatus.TO_PLAY, pageable);
    }

    @Test
    void getBacklogStatistics_Success() {
        when(backlogItemRepository.countByUserAndStatus(eq(testUser), any(BacklogStatus.class)))
            .thenReturn(1L);

        Map<BacklogStatus, Long> result = backlogItemService.getBacklogStatistics(testUser);

        assertNotNull(result);
        assertEquals(BacklogStatus.values().length, result.size());
        result.values().forEach(count -> assertEquals(1L, count));
        verify(backlogItemRepository, times(BacklogStatus.values().length))
            .countByUserAndStatus(eq(testUser), any(BacklogStatus.class));
    }

    @Test
    void batchUpdateStatus_AllGamesInBacklog_Success() {
        Map<Game, BacklogStatus> updates = new HashMap<>();
        updates.put(testGame, BacklogStatus.IN_PROGRESS);

        when(backlogItemRepository.findByUserAndGame(testUser, testGame))
            .thenReturn(Optional.of(testBacklogItem));
        when(backlogItemRepository.save(any(BacklogItem.class)))
            .thenReturn(testBacklogItem);

        List<BacklogItem> result = backlogItemService.batchUpdateStatus(testUser, updates);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(BacklogStatus.IN_PROGRESS, result.get(0).getStatus());
        verify(backlogItemRepository).save(any(BacklogItem.class));
    }

    @Test
    void batchUpdateStatus_SomeGamesNotInBacklog_PartialSuccess() {
        Game game2 = new Game();
        game2.setGameId(2L);
        
        Map<Game, BacklogStatus> updates = new HashMap<>();
        updates.put(testGame, BacklogStatus.IN_PROGRESS);
        updates.put(game2, BacklogStatus.TO_PLAY);

        when(backlogItemRepository.findByUserAndGame(testUser, testGame))
            .thenReturn(Optional.of(testBacklogItem));
        when(backlogItemRepository.findByUserAndGame(testUser, game2))
            .thenReturn(Optional.empty());
        when(backlogItemRepository.save(any(BacklogItem.class)))
            .thenReturn(testBacklogItem);

        List<BacklogItem> result = backlogItemService.batchUpdateStatus(testUser, updates);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(backlogItemRepository).save(any(BacklogItem.class));
    }

    @Test
    void batchUpdateStatus_InvalidTransition_PartialSuccess() {
        testBacklogItem.setStatus(BacklogStatus.COMPLETED);
        Map<Game, BacklogStatus> updates = new HashMap<>();
        updates.put(testGame, BacklogStatus.TO_PLAY);

        when(backlogItemRepository.findByUserAndGame(testUser, testGame))
            .thenReturn(Optional.of(testBacklogItem));

        List<BacklogItem> result = backlogItemService.batchUpdateStatus(testUser, updates);

        assertTrue(result.isEmpty());
        verify(backlogItemRepository, never()).save(any(BacklogItem.class));
    }
} 