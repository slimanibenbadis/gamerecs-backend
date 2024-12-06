package com.gamerecs.gamerecs_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.gamerecs.gamerecs_backend.model.User;

/**
 * Repository interface for User entity providing CRUD operations and custom
 * queries.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their username.
     * 
     * @param username the username to search for
     * @return an Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by their email address.
     * 
     * @param email the email to search for
     * @return an Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a username already exists.
     * 
     * @param username the username to check
     * @return true if the username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Check if an email already exists.
     * 
     * @param email the email to check
     * @return true if the email exists, false otherwise
     */
    boolean existsByEmail(String email);
}