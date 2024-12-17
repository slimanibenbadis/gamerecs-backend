package com.gamerecs.gamerecs_backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamerecs.gamerecs_backend.exception.BacklogException;
import com.gamerecs.gamerecs_backend.model.BacklogItem;
import com.gamerecs.gamerecs_backend.model.BacklogStatus;
import com.gamerecs.gamerecs_backend.model.Game;
import com.gamerecs.gamerecs_backend.model.User;
import com.gamerecs.gamerecs_backend.security.UserDetailsImpl;
import com.gamerecs.gamerecs_backend.service.GameService;
import com.gamerecs.gamerecs_backend.service.IBacklogItemService;
import com.gamerecs.gamerecs_backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BacklogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IBacklogItemService backlogItemService;

    @MockBean
    private GameService gameService;

    @MockBean
    private UserService userService;

    private User testUser;
    private Game testGame;
    private BacklogItem testBacklogItem;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        // Set up test user
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testUser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedPassword");
        testUser.setJoinDate(new Timestamp(System.currentTimeMillis()));

        // Set up UserDetails
        userDetails = new UserDetailsImpl(testUser);

        // Set up test game
        testGame = new Game();
        testGame.setGameId(1L);
        testGame.setTitle("Test Game");
        testGame.setGenres(Arrays.asList("RPG", "Action"));
        testGame.setPlatforms(Arrays.asList("PC", "PS5"));

        // Set up test backlog item
        testBacklogItem = new BacklogItem(testUser, testGame, BacklogStatus.TO_PLAY);
        testBacklogItem.setBacklogItemId(1L);

        // Mock UserService
        when(userService.loadUserByUsername(testUser.getUsername())).thenReturn(userDetails);
    }

    @Nested
    @DisplayName("CRUD Operations Tests")
    class CRUDOperationsTests {

        @Test
        @DisplayName("Should successfully add game to backlog")
        void shouldAddGameToBacklog() throws Exception {
            // Mock service responses
            when(gameService.findById(testGame.getGameId())).thenReturn(Optional.of(testGame));
            when(backlogItemService.addToBacklog(any(), any(), any())).thenReturn(testBacklogItem);

            // Perform request
            ResultActions result = mockMvc.perform(post("/api/backlog/add/{gameId}", testGame.getGameId())
                    .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(BacklogStatus.TO_PLAY)));

            // Verify response
            result.andExpect(status().isCreated())
                    .andExpect(jsonPath("$.game.title").value(testGame.getTitle()))
                    .andExpect(jsonPath("$.status").value(BacklogStatus.TO_PLAY.toString()));
        }

        @Test
        @DisplayName("Should successfully update backlog status")
        void shouldUpdateBacklogStatus() throws Exception {
            // Update test backlog item status
            testBacklogItem.setStatus(BacklogStatus.IN_PROGRESS);

            // Mock service responses
            when(gameService.findById(testGame.getGameId())).thenReturn(Optional.of(testGame));
            when(backlogItemService.updateGameStatus(any(), any(), any())).thenReturn(testBacklogItem);

            // Perform request
            ResultActions result = mockMvc.perform(put("/api/backlog/{gameId}", testGame.getGameId())
                    .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                    .param("status", "IN_PROGRESS"));

            // Verify response
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(BacklogStatus.IN_PROGRESS.toString()));
        }

        @Test
        @DisplayName("Should successfully remove game from backlog")
        void shouldRemoveGameFromBacklog() throws Exception {
            // Mock service responses
            when(gameService.findById(testGame.getGameId())).thenReturn(Optional.of(testGame));
            doNothing().when(backlogItemService).removeFromBacklog(any(), any());

            // Perform request
            ResultActions result = mockMvc.perform(delete("/api/backlog/{gameId}", testGame.getGameId())
                    .with(SecurityMockMvcRequestPostProcessors.user(userDetails)));

            // Verify response
            result.andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should successfully get user's backlog")
        void shouldGetUserBacklog() throws Exception {
            // Mock service responses
            when(backlogItemService.getUserBacklog(any(), any(), any()))
                    .thenReturn(new org.springframework.data.domain.PageImpl<>(Arrays.asList(testBacklogItem)));

            // Perform request
            ResultActions result = mockMvc.perform(get("/api/backlog")
                    .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                    .param("status", "TO_PLAY")
                    .param("page", "0")
                    .param("size", "10"));

            // Verify response
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].game.title").value(testGame.getTitle()));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should return 404 when game not found")
        void shouldReturn404WhenGameNotFound() throws Exception {
            // Mock service response
            when(gameService.findById(any())).thenReturn(Optional.of(testGame));
            when(backlogItemService.addToBacklog(any(), any(), any()))
                .thenThrow(new BacklogException("Game not found with ID: 999"));

            // Perform request
            ResultActions result = mockMvc.perform(post("/api/backlog/add/{gameId}", 999L)
                    .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(BacklogStatus.TO_PLAY)));

            // Verify response
            result.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("Game not found")));
        }

        @Test
        @DisplayName("Should return 409 when game already in backlog")
        void shouldReturn409WhenGameAlreadyInBacklog() throws Exception {
            // Mock service responses
            when(gameService.findById(testGame.getGameId())).thenReturn(Optional.of(testGame));
            when(backlogItemService.addToBacklog(any(), any(), any()))
                .thenThrow(new BacklogException("Game is already in user's backlog"));

            // Perform request
            ResultActions result = mockMvc.perform(post("/api/backlog/add/{gameId}", testGame.getGameId())
                    .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(BacklogStatus.TO_PLAY)));

            // Verify response
            result.andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Game is already in user's backlog"));
        }

        @Test
        @DisplayName("Should return 401 when unauthorized")
        void shouldReturn401WhenUnauthorized() throws Exception {
            // Perform request without authentication
            ResultActions result = mockMvc.perform(get("/api/backlog")
                    .contentType(MediaType.APPLICATION_JSON));

            // Verify response
            result.andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 for invalid status")
        void shouldReturn400ForInvalidStatus() throws Exception {
            // Mock service response
            when(gameService.findById(testGame.getGameId())).thenReturn(Optional.of(testGame));

            // Perform request with invalid status
            ResultActions result = mockMvc.perform(put("/api/backlog/{gameId}", testGame.getGameId())
                    .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                    .param("status", "INVALID_STATUS"));

            // Verify response
            result.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("status should be of type BacklogStatus"));
        }
    }

    @Nested
    @DisplayName("Authentication Flow Tests")
    class AuthenticationFlowTests {

        @Test
        @DisplayName("Should require authentication for all endpoints")
        void shouldRequireAuthentication() throws Exception {
            // Test all endpoints without authentication
            mockMvc.perform(get("/api/backlog")).andExpect(status().isUnauthorized());
            mockMvc.perform(post("/api/backlog/add/1")).andExpect(status().isUnauthorized());
            mockMvc.perform(put("/api/backlog/update/1")).andExpect(status().isUnauthorized());
            mockMvc.perform(delete("/api/backlog/remove/1")).andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should reject requests from wrong user")
        void shouldRejectWrongUser() throws Exception {
            // Create wrong user
            User wrongUser = new User();
            wrongUser.setUsername("wrongUser");
            UserDetailsImpl wrongUserDetails = new UserDetailsImpl(wrongUser);

            // Mock service for wrong user
            when(userService.loadUserByUsername("wrongUser")).thenReturn(wrongUserDetails);
            when(backlogItemService.getUserBacklog(eq(wrongUser), any(), any()))
                .thenThrow(new BacklogException("User not authorized to access this backlog"));

            // Perform request
            ResultActions result = mockMvc.perform(get("/api/backlog")
                    .with(SecurityMockMvcRequestPostProcessors.user(wrongUserDetails)));

            // Verify response
            result.andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.error").value("Unauthorized"))
                    .andExpect(jsonPath("$.message").value("User not authorized to access this backlog"));
        }

        @Test
        @DisplayName("Should accept requests with valid authentication")
        void shouldAcceptValidAuthentication() throws Exception {
            // Mock service responses
            when(backlogItemService.getUserBacklog(any(), any(), any()))
                    .thenReturn(new org.springframework.data.domain.PageImpl<>(Arrays.asList(testBacklogItem)));

            // Perform request
            ResultActions result = mockMvc.perform(get("/api/backlog")
                    .with(SecurityMockMvcRequestPostProcessors.user(userDetails)));

            // Verify response
            result.andExpect(status().isOk());
        }
    }
} 