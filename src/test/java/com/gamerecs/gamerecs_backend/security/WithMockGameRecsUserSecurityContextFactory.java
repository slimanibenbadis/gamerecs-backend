package com.gamerecs.gamerecs_backend.security;

import com.gamerecs.gamerecs_backend.model.User;
import java.util.Collections;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockGameRecsUserSecurityContextFactory implements WithSecurityContextFactory<WithMockGameRecsUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockGameRecsUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        User user = new User();
        user.setUsername(annotation.username());
        user.setEmail(annotation.email());
        user.setUserId(1L); // Set a default ID for testing

        // Create authentication with a default ROLE_USER authority
        Authentication auth = new UsernamePasswordAuthenticationToken(
            user, 
            null, 
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        context.setAuthentication(auth);
        return context;
    }
} 