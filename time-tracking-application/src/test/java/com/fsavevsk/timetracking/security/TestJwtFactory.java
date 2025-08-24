package com.fsavevsk.timetracking.security;

import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import java.time.Instant;
import java.util.Map;

public final class TestJwtFactory {

    private TestJwtFactory() {}

    public static String token(JwtEncoder encoder,
                               String subject,
                               Map<String,Object> customClaims,
                               String... scopes) {
        var now = Instant.now();
        var scope = String.join(" ", scopes);
        var claims = JwtClaimsSet.builder()
                .subject(subject)
                .claim("scope", scope)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .claims(map -> map.putAll(customClaims == null ? Map.of() : customClaims))
                .build();

        var headers = JwsHeader.with(SignatureAlgorithm.RS256).build();
        return encoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
    }
}
