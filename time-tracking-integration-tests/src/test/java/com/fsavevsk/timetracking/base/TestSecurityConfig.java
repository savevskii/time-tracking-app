package com.fsavevsk.timetracking.base;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    JwtDecoder jwtDecoder() {
        return token -> Jwt.withTokenValue(token == null ? "it-token" : token)
                .header("alg", "none")
                .subject("it-user")
                .claim("preferred_username", "it-user")
                .claim("realm_access", Map.of("roles", List.of("admin","user")))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }
}
