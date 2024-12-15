package com.gamerecs.gamerecs_backend.security;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.security.test.context.support.WithSecurityContext;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockGameRecsUserSecurityContextFactory.class)
public @interface WithMockGameRecsUser {
    String username() default "testuser";
    String email() default "testuser@example.com";
} 