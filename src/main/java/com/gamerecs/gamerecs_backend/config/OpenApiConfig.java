package com.gamerecs.gamerecs_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gameRecsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("GameRecs API")
                        .description("API documentation for the GameRecs application")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("GameRecs Team")
                                .email("contact@gamerecs.com")));
    }
}