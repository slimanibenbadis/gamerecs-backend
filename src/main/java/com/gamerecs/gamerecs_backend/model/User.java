package com.gamerecs.gamerecs_backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Entity class representing a user in the system.
 */
@Entity
@Table(name = "users") // Using "users" instead of "User" as it's a better practice for table naming
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private Long userId;

    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^[a-zA-Z0-9_-]{3,50}$", message = "Username must be between 3 and 50 characters and can only contain letters, numbers, underscores and hyphens")
    @Column(name = "Username", nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(name = "Email", nullable = false, unique = true, length = 255)
    private String email;

    @NotBlank(message = "Password hash is required")
    @Column(name = "PasswordHash", nullable = false, length = 255)
    private String passwordHash;

    @Size(max = 500, message = "Profile picture URL cannot exceed 500 characters")
    @Column(name = "ProfilePictureURL", columnDefinition = "TEXT")
    private String profilePictureURL;

    @Size(max = 1000, message = "Bio cannot exceed 1000 characters")
    @Column(name = "Bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "JoinDate", nullable = false)
    private Timestamp joinDate;

    @Column(name = "LastLogin")
    private Timestamp lastLogin;

    // Default constructor
    public User() {
        this.joinDate = new Timestamp(System.currentTimeMillis());
    }

    // Constructor with required fields
    public User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.joinDate = new Timestamp(System.currentTimeMillis());
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getProfilePictureURL() {
        return profilePictureURL;
    }

    public void setProfilePictureURL(String profilePictureURL) {
        this.profilePictureURL = profilePictureURL;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Timestamp getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(Timestamp joinDate) {
        this.joinDate = joinDate;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId) &&
                Objects.equals(username, user.username) &&
                Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username, email);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", profilePictureURL='" + profilePictureURL + '\'' +
                ", bio='" + bio + '\'' +
                ", joinDate=" + joinDate +
                ", lastLogin=" + lastLogin +
                '}';
    }
}