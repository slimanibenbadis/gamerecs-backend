package com.gamerecs.gamerecs_backend.service;

import java.sql.Timestamp;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gamerecs.gamerecs_backend.dto.UserProfileDTO;
import com.gamerecs.gamerecs_backend.dto.UserRegistrationDTO;
import com.gamerecs.gamerecs_backend.exception.UserRegistrationException;
import com.gamerecs.gamerecs_backend.model.User;
import com.gamerecs.gamerecs_backend.repository.UserRepository;

/**
 * Service class handling user-related business logic.
 */
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Loads a user's details by username for Spring Security authentication.
     * 
     * @param username the username to search for
     * @return UserDetails object containing the user's security information
     * @throws UsernameNotFoundException if the user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPasswordHash(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
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
        // Validate password confirmation
        if (!registrationDTO.getPassword().equals(registrationDTO.getConfirmPassword())) {
            throw new UserRegistrationException("Passwords do not match");
        }

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

    public UserProfileDTO getUserProfile(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return UserProfileDTO.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .profilePictureURL(user.getProfilePictureURL())
                .bio(user.getBio())
                .joinDate(user.getJoinDate() != null ? user.getJoinDate().toLocalDateTime() : null)
                .build();
    }
}