package com.gamerecs.gamerecs_backend.service;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gamerecs.gamerecs_backend.dto.UserRegistrationDTO;
import com.gamerecs.gamerecs_backend.exception.UserRegistrationException;
import com.gamerecs.gamerecs_backend.model.User;
import com.gamerecs.gamerecs_backend.repository.UserRepository;

/**
 * Service class handling user-related business logic.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user in the system.
     * 
     * @param registrationDTO the user registration data
     * @return the created User entity
     * @throws UserRegistrationException if registration fails due to validation or
     *                                   duplicate data
     */
    @Transactional
    public User registerUser(UserRegistrationDTO registrationDTO) {
        // Check for existing username
        if (userRepository.existsByUsername(registrationDTO.getUsername())) {
            throw new UserRegistrationException("Username already exists");
        }

        // Check for existing email
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new UserRegistrationException("Email already exists");
        }

        // Create new user entity
        User user = new User(
                registrationDTO.getUsername(),
                registrationDTO.getEmail(),
                passwordEncoder.encode(registrationDTO.getPassword()));

        // Set optional fields
        user.setProfilePictureURL(registrationDTO.getProfilePictureURL());
        user.setBio(registrationDTO.getBio());
        user.setJoinDate(new Timestamp(System.currentTimeMillis()));

        // Save and return the new user
        return userRepository.save(user);
    }
}