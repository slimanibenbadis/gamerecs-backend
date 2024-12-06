package com.gamerecs.gamerecs_backend.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private String username;
    private String email;
    private String profilePictureURL;
    private String bio;
    private LocalDateTime joinDate;
}