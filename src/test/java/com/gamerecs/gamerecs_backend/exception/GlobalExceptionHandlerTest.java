package com.gamerecs.gamerecs_backend.exception;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleUserRegistrationException() {
        String errorMessage = "Username already exists";
        UserRegistrationException exception = new UserRegistrationException(errorMessage);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUserRegistrationException(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertEquals("Registration Error", response.getBody().getError());
        assertEquals(409, response.getBody().getStatus());
    }

    @Test
    void handleBadCredentialsException() {
        BadCredentialsException exception = new BadCredentialsException("Bad credentials");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadCredentialsException(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid username or password", response.getBody().getMessage());
        assertEquals("Authentication Error", response.getBody().getError());
        assertEquals(401, response.getBody().getStatus());
    }

    @Test
    void handleUsernameNotFoundException() {
        String errorMessage = "User not found";
        UsernameNotFoundException exception = new UsernameNotFoundException(errorMessage);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUsernameNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertEquals("User Not Found", response.getBody().getError());
        assertEquals(404, response.getBody().getStatus());
    }

    @Test
    void handleMethodArgumentNotValidException() {
        // Mock MethodArgumentNotValidException and its dependencies
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        List<FieldError> fieldErrors = new ArrayList<>();
        
        fieldErrors.add(new FieldError("user", "username", "Username is required"));
        fieldErrors.add(new FieldError("user", "email", "Invalid email format"));

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationExceptions(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("username: Username is required, email: Invalid email format", 
            response.getBody().getMessage());
        assertEquals("Validation Error", response.getBody().getError());
        assertEquals(400, response.getBody().getStatus());
    }

    @Test
    void handleAllUncaughtException() {
        Exception exception = new RuntimeException("Unexpected error");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAllUncaughtException(exception, null);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred. Please try again later.", 
            response.getBody().getMessage());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals(500, response.getBody().getStatus());
    }
} 