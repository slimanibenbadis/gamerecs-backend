package com.gamerecs.gamerecs_backend.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BacklogItemTest {

    private Validator validator;
    private User testUser;
    private Game testGame;
    private BacklogItem testBacklogItem;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        // Set up test user
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testUser");
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedPassword");

        // Set up test game
        testGame = new Game();
        testGame.setGameId(1L);
        testGame.setTitle("Test Game");

        // Set up test backlog item
        testBacklogItem = new BacklogItem(testUser, testGame, BacklogStatus.TO_PLAY);
        testBacklogItem.setBacklogItemId(1L);
    }

    @Test
    void whenAllFieldsAreValid_thenNoValidationViolations() {
        var violations = validator.validate(testBacklogItem);
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenUserIsNull_thenValidationFails() {
        testBacklogItem.setUser(null);
        var violations = validator.validate(testBacklogItem);
        
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("User is required", violations.iterator().next().getMessage());
    }

    @Test
    void whenGameIsNull_thenValidationFails() {
        testBacklogItem.setGame(null);
        var violations = validator.validate(testBacklogItem);
        
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Game is required", violations.iterator().next().getMessage());
    }

    @Test
    void whenStatusIsNull_thenValidationFails() {
        testBacklogItem.setStatus(null);
        var violations = validator.validate(testBacklogItem);
        
        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
        assertEquals("Status is required", violations.iterator().next().getMessage());
    }

    @Test
    void testEqualsWithSameObject() {
        assertTrue(testBacklogItem.equals(testBacklogItem));
    }

    @Test
    void testEqualsWithDifferentObject() {
        BacklogItem otherBacklogItem = new BacklogItem(testUser, testGame, BacklogStatus.TO_PLAY);
        otherBacklogItem.setBacklogItemId(1L);
        
        assertTrue(testBacklogItem.equals(otherBacklogItem));
    }

    @Test
    void testEqualsWithDifferentId() {
        BacklogItem otherBacklogItem = new BacklogItem(testUser, testGame, BacklogStatus.TO_PLAY);
        otherBacklogItem.setBacklogItemId(2L);
        
        assertFalse(testBacklogItem.equals(otherBacklogItem));
    }

    @Test
    void testEqualsWithDifferentUser() {
        User otherUser = new User();
        otherUser.setUserId(2L);
        
        BacklogItem otherBacklogItem = new BacklogItem(otherUser, testGame, BacklogStatus.TO_PLAY);
        otherBacklogItem.setBacklogItemId(1L);
        
        assertFalse(testBacklogItem.equals(otherBacklogItem));
    }

    @Test
    void testEqualsWithDifferentGame() {
        Game otherGame = new Game();
        otherGame.setGameId(2L);
        
        BacklogItem otherBacklogItem = new BacklogItem(testUser, otherGame, BacklogStatus.TO_PLAY);
        otherBacklogItem.setBacklogItemId(1L);
        
        assertFalse(testBacklogItem.equals(otherBacklogItem));
    }

    @Test
    void testEqualsWithNull() {
        assertFalse(testBacklogItem.equals(null));
    }

    @Test
    void testEqualsWithDifferentClass() {
        assertFalse(testBacklogItem.equals(new Object()));
    }

    @Test
    void testHashCodeConsistency() {
        int initialHashCode = testBacklogItem.hashCode();
        int secondHashCode = testBacklogItem.hashCode();
        
        assertEquals(initialHashCode, secondHashCode);
    }

    @Test
    void testHashCodeEquality() {
        BacklogItem otherBacklogItem = new BacklogItem(testUser, testGame, BacklogStatus.TO_PLAY);
        otherBacklogItem.setBacklogItemId(1L);
        
        assertEquals(testBacklogItem.hashCode(), otherBacklogItem.hashCode());
    }

    @Test
    void testToString() {
        String expectedString = "BacklogItem{" +
                "backlogItemId=" + testBacklogItem.getBacklogItemId() +
                ", userId=" + testUser.getUserId() +
                ", gameId=" + testGame.getGameId() +
                ", status=" + BacklogStatus.TO_PLAY +
                '}';
        
        assertEquals(expectedString, testBacklogItem.toString());
    }

    @Test
    void testDefaultConstructor() {
        BacklogItem backlogItem = new BacklogItem();
        assertNotNull(backlogItem);
    }

    @Test
    void testParameterizedConstructor() {
        BacklogItem backlogItem = new BacklogItem(testUser, testGame, BacklogStatus.TO_PLAY);
        
        assertNotNull(backlogItem);
        assertEquals(testUser, backlogItem.getUser());
        assertEquals(testGame, backlogItem.getGame());
        assertEquals(BacklogStatus.TO_PLAY, backlogItem.getStatus());
    }
} 