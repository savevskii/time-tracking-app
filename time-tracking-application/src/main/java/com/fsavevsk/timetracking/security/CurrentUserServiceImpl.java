package com.fsavevsk.timetracking.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    private Set<String> extractRoles(Jwt jwt) {
        var realm = jwt.getClaimAsMap("realm_access");
        if (realm == null) return Collections.emptySet();

        Object roles = realm.get("roles");
        if (roles instanceof List<?> list) {
            return list.stream().map(String::valueOf).collect(Collectors.toCollection(HashSet::new));
        }
        return Collections.emptySet();
    }
}

