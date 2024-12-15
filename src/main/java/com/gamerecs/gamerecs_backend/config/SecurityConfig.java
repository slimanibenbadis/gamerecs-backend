package com.gamerecs.gamerecs_backend.config;

import com.gamerecs.gamerecs_backend.security.JwtAuthenticationFilter;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SecurityConfig.class);

    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Swagger UI and authentication paths
    private static final String[] WHITE_LIST_URLS = {
            "/users/register",
            "/users/login",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**",
            "/api/users/register",
            "/api/users/login",
            "/api/v3/api-docs/**",
            "/api/swagger-ui/**",
            "/api/swagger-ui.html",
            "/api/swagger-resources/**",
            "/api/webjars/**"
    };

    public SecurityConfig(
            AuthenticationProvider authenticationProvider,
            JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.authenticationProvider = authenticationProvider;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("Configuring security filter chain");
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> {
                log.info("Configuring CORS");
                cors.configurationSource(corsConfigurationSource());
            })
            .exceptionHandling(exceptions -> {
                log.info("Configuring exception handling");
                exceptions.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
            })
            .authorizeHttpRequests(auth -> {
                log.info("Configuring authorization rules");
                log.info("Whitelisted URLs: {}", Arrays.toString(WHITE_LIST_URLS));
                auth
                    .requestMatchers(WHITE_LIST_URLS).permitAll()
                    .requestMatchers("/error").permitAll() // Allow error pages
                    
                    // Game endpoints
                    .requestMatchers(HttpMethod.GET, "/api/games/**").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/games/**").authenticated()
                    .requestMatchers(HttpMethod.PUT, "/api/games/**").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/games/**").authenticated()
                    
                    // Rating endpoints - Read operations
                    .requestMatchers(HttpMethod.GET, "/api/ratings/games/*/average").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/ratings/games/*").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/ratings/users/*").permitAll()
                    
                    // Rating endpoints - Write operations (require authentication)
                    .requestMatchers(HttpMethod.PUT, "/api/ratings/**").authenticated()
                    .requestMatchers(HttpMethod.POST, "/api/ratings/**").authenticated()
                    .requestMatchers(HttpMethod.DELETE, "/api/ratings/**").authenticated()
                    .requestMatchers("/api/ratings/users/me/**").authenticated()
                    
                    // Any other request requires authentication
                    .anyRequest().authenticated();
            })
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200", "http://localhost:8080")); // Added Swagger UI origin
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}