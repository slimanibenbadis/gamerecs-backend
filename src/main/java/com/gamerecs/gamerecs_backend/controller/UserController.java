package com.gamerecs.gamerecs_backend.controller;

import com.gamerecs.gamerecs_backend.dto.AuthenticationResponse;
import com.gamerecs.gamerecs_backend.dto.LoginDTO;
import com.gamerecs.gamerecs_backend.dto.UserProfileDTO;
import com.gamerecs.gamerecs_backend.dto.UserRegistrationDTO;
import com.gamerecs.gamerecs_backend.exception.ErrorResponse;
import com.gamerecs.gamerecs_backend.exception.UserRegistrationException;
import com.gamerecs.gamerecs_backend.security.JwtService;
import com.gamerecs.gamerecs_backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Tag(name = "User Controller", description = "Endpoints for user management")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Autowired
    public UserController(UserService userService, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Register a new user", description = "Creates a new user account with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Username or email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDTO registrationDTO) {
        try {
            var registeredUser = userService.registerUser(registrationDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
        } catch (IllegalArgumentException e) {
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                e.getMessage()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        } catch (UserRegistrationException e) {
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "Registration Error",
                e.getMessage()
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }
    }

    @Operation(summary = "Authenticate user", description = "Authenticates user credentials and returns a JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginDTO loginDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtService.generateToken(userDetails);

            AuthenticationResponse response = AuthenticationResponse.builder()
                    .token(jwt)
                    .username(userDetails.getUsername())
                    .build();

            return ResponseEntity.ok()
                    .header("Authorization", "Bearer " + jwt)
                    .body(response);
        } catch (AuthenticationException e) {
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Authentication Error",
                "Invalid username or password"
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }
    }

    @Operation(summary = "Get current user profile", description = "Retrieves the profile of the currently authenticated user", security = {
            @SecurityRequirement(name = "bearerAuth") })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not authorized to view this profile")
    })
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            UserProfileDTO profile = userService.getUserProfile(userDetails.getUsername());
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Profile Error",
                "Unable to retrieve user profile"
            );
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }
}