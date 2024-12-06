package com.gamerecs.gamerecs_backend.exception;

/**
 * Custom exception for handling user registration related errors.
 */
public class UserRegistrationException extends RuntimeException {

    public UserRegistrationException(String message) {
        super(message);
    }

    public UserRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}