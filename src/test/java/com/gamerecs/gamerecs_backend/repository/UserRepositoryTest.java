package com.gamerecs.gamerecs_backend.repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.gamerecs.gamerecs_backend.model.User;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create a test user
        testUser = new User(
                "testuser",
                "test@example.com",
                "hashedPassword123");
        testUser.setBio("Test bio");
        testUser.setProfilePictureURL("http://example.com/pic.jpg");
        testUser.setJoinDate(Timestamp.from(Instant.now()));
        testUser.setLastLogin(Timestamp.from(Instant.now()));
    }

    @Test
    void shouldSaveUser() {
        // when
        User savedUser = userRepository.save(testUser);

        // then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUserId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo(testUser.getUsername());
        assertThat(savedUser.getEmail()).isEqualTo(testUser.getEmail());
    }

    @Test
    void shouldFindUserById() {
        // given
        User persistedUser = entityManager.persist(testUser);
        entityManager.flush();

        // when
        Optional<User> foundUser = userRepository.findById(persistedUser.getUserId());

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo(testUser.getUsername());
    }

    @Test
    void shouldFindUserByUsername() {
        // given
        entityManager.persist(testUser);
        entityManager.flush();

        // when
        Optional<User> foundUser = userRepository.findByUsername(testUser.getUsername());

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo(testUser.getEmail());
    }

    @Test
    void shouldFindUserByEmail() {
        // given
        entityManager.persist(testUser);
        entityManager.flush();

        // when
        Optional<User> foundUser = userRepository.findByEmail(testUser.getEmail());

        // then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo(testUser.getUsername());
    }

    @Test
    void shouldCheckIfUsernameExists() {
        // given
        entityManager.persist(testUser);
        entityManager.flush();

        // when
        boolean exists = userRepository.existsByUsername(testUser.getUsername());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void shouldCheckIfEmailExists() {
        // given
        entityManager.persist(testUser);
        entityManager.flush();

        // when
        boolean exists = userRepository.existsByEmail(testUser.getEmail());

        // then
        assertThat(exists).isTrue();
    }

    @Test
    void shouldUpdateUser() {
        // given
        User persistedUser = entityManager.persist(testUser);
        entityManager.flush();

        // when
        persistedUser.setBio("Updated bio");
        User updatedUser = userRepository.save(persistedUser);

        // then
        assertThat(updatedUser.getBio()).isEqualTo("Updated bio");
    }

    @Test
    void shouldDeleteUser() {
        // given
        User persistedUser = entityManager.persist(testUser);
        entityManager.flush();

        // when
        userRepository.delete(persistedUser);
        Optional<User> deletedUser = userRepository.findById(persistedUser.getUserId());

        // then
        assertThat(deletedUser).isEmpty();
    }
}