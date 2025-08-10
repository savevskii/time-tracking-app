package com.fsavevsk.timetracking.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Service
public class CurrentUserServiceImpl implements CurrentUserService {

    @Override
    public AuthenticatedUser requireUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!(auth instanceof JwtAuthenticationToken jwtAuth)) {
            throw new IllegalStateException("No authenticated JWT in security context");
        }
        Jwt jwt = jwtAuth.getToken();

        String id = claim(jwt, "sub");
        String username = firstNonBlank(
                claimOrNull(jwt, "preferred_username"),
                claimOrNull(jwt, "username"),
                id
        );
        String email = firstNonBlank(
                claimOrNull(jwt, "email"),
                claimOrNull(jwt, "upn")
        );

        Set<String> roles = extractRoles(jwt);

        return new AuthenticatedUser(id, username, email, roles);
    }

    @Override
    public String userId() {
        return requireUser().id();
    }

    private String claim(Jwt jwt, String name) {
        String v = jwt.getClaimAsString(name);
        if (v == null || v.isBlank()) throw new IllegalStateException("Missing claim: " + name);
        return v;
    }

    private String claimOrNull(Jwt jwt, String name) {
        try { return jwt.getClaimAsString(name); } catch (Exception e) { return null; }
    }

    private String firstNonBlank(String... vals) {
        for (String v : vals) if (v != null && !v.isBlank()) return v;
        return null;
    }

    @SuppressWarnings("unchecked")
    private Set<String> extractRoles(Jwt jwt) {
        try {
            var realmAccess = (java.util.Map<String, Object>) jwt.getClaim("realm_access");
            var roles = (java.util.List<String>) realmAccess.get("roles");
            return new HashSet<>(roles);
        } catch (Exception ignored) {
            return Collections.emptySet();
        }
    }
}

