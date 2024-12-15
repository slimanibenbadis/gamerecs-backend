package com.gamerecs.gamerecs_backend.model;

/**
 * Enum representing the possible statuses of a game in a user's backlog.
 */
public enum BacklogStatus {
    TO_PLAY("To Play"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    ABANDONED("Abandoned");

    private final String displayName;

    BacklogStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
} 