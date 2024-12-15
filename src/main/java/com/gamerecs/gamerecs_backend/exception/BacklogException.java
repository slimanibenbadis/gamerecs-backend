package com.gamerecs.gamerecs_backend.exception;

/**
 * Custom exception for handling backlog-related errors.
 */
public class BacklogException extends RuntimeException {

    public BacklogException(String message) {
        super(message);
    }

    public BacklogException(String message, Throwable cause) {
        super(message, cause);
    }
} 