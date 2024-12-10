package com.gamerecs.gamerecs_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "igdb")
@Getter
@Setter
public class ApplicationConfig {
    private String clientId;
    private String clientSecret;
    private String apiUrl;
}
