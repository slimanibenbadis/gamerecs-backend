package com.gamerecs.gamerecs_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class GameRecsBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(GameRecsBackendApplication.class, args);
	}

}
