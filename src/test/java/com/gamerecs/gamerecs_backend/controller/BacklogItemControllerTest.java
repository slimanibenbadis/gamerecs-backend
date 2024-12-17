package com.gamerecs.gamerecs_backend.controller;

import com.gamerecs.gamerecs_backend.exception.BacklogException;
import com.gamerecs.gamerecs_backend.model.BacklogItem;
import com.gamerecs.gamerecs_backend.model.BacklogStatus;
import com.gamerecs.gamerecs_backend.model.Game;
import com.gamerecs.gamerecs_backend.model.User;
import com.gamerecs.gamerecs_backend.security.UserDetailsImpl;
import com.gamerecs.gamerecs_backend.service.GameService;
import com.gamerecs.gamerecs_backend.service.IBacklogItemService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

@SpringBootTest
class BacklogItemControllerTest {

    @Mock
    private IBacklogItemService backlogItemService;

    @Mock
    private GameService gameService;

    @InjectMocks
    private BacklogItemController backlogItemController;

    private User testUser;
    private Game testGame;
    private BacklogItem testBacklogItem;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testUser");

        testGame = new Game();
        testGame.setGameId(1L);
        testGame.setTitle("Test Game");

        testBacklogItem = new BacklogItem();
        testBacklogItem.setUser(testUser);
        testBacklogItem.setGame(testGame);
        testBacklogItem.setStatus(BacklogStatus.TO_PLAY);

        userDetails = new UserDetailsImpl(testUser);
    }

    @Nested
    @DisplayName("Add to Backlog Tests")
    class AddToBacklogTests {

        @Test
        @DisplayName("Should successfully add game to backlog")
        void shouldAddGameToBacklog() {
            when(gameService.findById(1L)).thenReturn(Optional.of(testGame));
            when(backlogItemService.addToBacklog(any(), any(), any())).thenReturn(testBacklogItem);

            ResponseEntity<?> response = backlogItemController.addToBacklog(
                userDetails, 1L, BacklogStatus.TO_PLAY);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody() instanceof BacklogItem);
            verify(backlogItemService).addToBacklog(testUser, testGame, BacklogStatus.TO_PLAY);
        }

        @Test
        @DisplayName("Should return 409 when game already in backlog")
        void shouldReturnConflictWhenGameAlreadyInBacklog() {
            when(gameService.findById(1L)).thenReturn(Optional.of(testGame));
            when(backlogItemService.addToBacklog(any(), any(), any()))
                .thenThrow(new BacklogException("Game is already in user's backlog"));

            ResponseEntity<?> response = backlogItemController.addToBacklog(
                userDetails, 1L, BacklogStatus.TO_PLAY);

            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            verify(backlogItemService).addToBacklog(testUser, testGame, BacklogStatus.TO_PLAY);
        }

        @Test
        @DisplayName("Should return 404 when game not found")
        void shouldReturnNotFoundWhenGameNotFound() {
            when(gameService.findById(1L)).thenReturn(Optional.empty());

            ResponseEntity<?> response = backlogItemController.addToBacklog(
                userDetails, 1L, BacklogStatus.TO_PLAY);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            verify(gameService).findById(1L);
            verify(backlogItemService, never()).addToBacklog(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("Update Status Tests")
    class UpdateStatusTests {

        @Test
        @DisplayName("Should successfully update backlog status")
        void shouldUpdateBacklogStatus() {
            when(gameService.findById(1L)).thenReturn(Optional.of(testGame));
            when(backlogItemService.updateGameStatus(any(), any(), any())).thenReturn(testBacklogItem);

            ResponseEntity<?> response = backlogItemController.updateStatus(
                userDetails, 1L, BacklogStatus.IN_PROGRESS);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody() instanceof BacklogItem);
            verify(backlogItemService).updateGameStatus(testUser, testGame, BacklogStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("Should return 404 when backlog item not found")
        void shouldReturnNotFoundWhenBacklogItemNotFound() {
            when(gameService.findById(1L)).thenReturn(Optional.of(testGame));
            when(backlogItemService.updateGameStatus(any(), any(), any()))
                .thenThrow(new BacklogException("Game not found with ID: " + testGame.getGameId()));

            ResponseEntity<?> response = backlogItemController.updateStatus(
                userDetails, 1L, BacklogStatus.IN_PROGRESS);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            verify(backlogItemService).updateGameStatus(testUser, testGame, BacklogStatus.IN_PROGRESS);
        }
    }

    @Nested
    @DisplayName("Remove from Backlog Tests")
    class RemoveFromBacklogTests {

        @Test
        @DisplayName("Should successfully remove game from backlog")
        void shouldRemoveGameFromBacklog() {
            when(gameService.findById(1L)).thenReturn(Optional.of(testGame));
            doNothing().when(backlogItemService).removeFromBacklog(any(), any());

            ResponseEntity<?> response = backlogItemController.removeFromBacklog(userDetails, 1L);

            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            assertNull(response.getBody());
            verify(backlogItemService).removeFromBacklog(testUser, testGame);
        }

        @Test
        @DisplayName("Should return 404 when backlog item not found")
        void shouldReturnNotFoundWhenBacklogItemNotFound() {
            when(gameService.findById(1L)).thenReturn(Optional.of(testGame));
            doThrow(new IllegalArgumentException("Game not found"))
                .when(backlogItemService).removeFromBacklog(any(), any());

            ResponseEntity<?> response = backlogItemController.removeFromBacklog(userDetails, 1L);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            verify(backlogItemService).removeFromBacklog(testUser, testGame);
        }
    }

    @Nested
    @DisplayName("Get User Backlog Tests")
    class GetUserBacklogTests {

        @Test
        @DisplayName("Should successfully get user backlog")
        void shouldGetUserBacklog() {
            Page<BacklogItem> backlogPage = new PageImpl<>(Arrays.asList(testBacklogItem));
            Pageable pageable = PageRequest.of(0, 10);
            when(backlogItemService.getUserBacklog(any(), any(), any())).thenReturn(backlogPage);

            ResponseEntity<?> response = backlogItemController.getUserBacklog(
                userDetails, BacklogStatus.TO_PLAY, pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody() instanceof Page);
            verify(backlogItemService).getUserBacklog(testUser, BacklogStatus.TO_PLAY, pageable);
        }

        @Test
        @DisplayName("Should successfully get user backlog without status filter")
        void shouldGetUserBacklogWithoutStatusFilter() {
            Page<BacklogItem> backlogPage = new PageImpl<>(Arrays.asList(testBacklogItem));
            Pageable pageable = PageRequest.of(0, 10);
            when(backlogItemService.getUserBacklog(any(), any(), any())).thenReturn(backlogPage);

            ResponseEntity<?> response = backlogItemController.getUserBacklog(
                userDetails, null, pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody() instanceof Page);
            verify(backlogItemService).getUserBacklog(testUser, null, pageable);
        }

        @Test
        @DisplayName("Should return 401 when user not authorized")
        void shouldReturnUnauthorizedWhenUserNotAuthorized() {
            Pageable pageable = PageRequest.of(0, 10);
            when(backlogItemService.getUserBacklog(any(), any(), any()))
                .thenThrow(new BacklogException("User not authorized to access this backlog"));

            ResponseEntity<?> response = backlogItemController.getUserBacklog(
                userDetails, BacklogStatus.TO_PLAY, pageable);

            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            verify(backlogItemService).getUserBacklog(testUser, BacklogStatus.TO_PLAY, pageable);
        }
    }

    @Nested
    @DisplayName("Get Backlog Statistics Tests")
    class GetBacklogStatisticsTests {

        @Test
        @DisplayName("Should successfully get backlog statistics")
        void shouldGetBacklogStatistics() {
            Map<BacklogStatus, Long> stats = new HashMap<>();
            stats.put(BacklogStatus.TO_PLAY, 5L);
            stats.put(BacklogStatus.IN_PROGRESS, 2L);
            stats.put(BacklogStatus.COMPLETED, 10L);
            when(backlogItemService.getBacklogStatistics(any())).thenReturn(stats);

            ResponseEntity<?> response = backlogItemController.getBacklogStats(userDetails);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody() instanceof Map);
            verify(backlogItemService).getBacklogStatistics(testUser);
        }

        @Test
        @DisplayName("Should return 500 when error occurs")
        void shouldReturnInternalServerErrorWhenErrorOccurs() {
            when(backlogItemService.getBacklogStatistics(any()))
                .thenThrow(new RuntimeException("An error occurred"));

            ResponseEntity<?> response = backlogItemController.getBacklogStats(userDetails);

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
            verify(backlogItemService).getBacklogStatistics(testUser);
        }
    }

    @Nested
    @DisplayName("Get Backlog by Status Tests")
    class GetBacklogByStatusTests {

        @Test
        @DisplayName("Should successfully get backlog by status")
        void shouldGetBacklogByStatus() {
            Page<BacklogItem> backlogPage = new PageImpl<>(Arrays.asList(testBacklogItem));
            Pageable pageable = PageRequest.of(0, 10);
            when(backlogItemService.getUserBacklog(any(), any(), any())).thenReturn(backlogPage);

            ResponseEntity<?> response = backlogItemController.getBacklogByStatus(
                userDetails, BacklogStatus.TO_PLAY, pageable);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody() instanceof Page);
            verify(backlogItemService).getUserBacklog(testUser, BacklogStatus.TO_PLAY, pageable);
        }

        @Test
        @DisplayName("Should return 400 for invalid status")
        void shouldReturnBadRequestForInvalidStatus() {
            Pageable pageable = PageRequest.of(0, 10);
            when(backlogItemService.getUserBacklog(any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Invalid status"));

            ResponseEntity<?> response = backlogItemController.getBacklogByStatus(
                userDetails, BacklogStatus.TO_PLAY, pageable);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            verify(backlogItemService).getUserBacklog(testUser, BacklogStatus.TO_PLAY, pageable);
        }
    }
} 