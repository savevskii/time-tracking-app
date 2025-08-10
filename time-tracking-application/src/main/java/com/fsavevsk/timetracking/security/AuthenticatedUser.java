package com.fsavevsk.timetracking.security;

import java.util.Set;

public record AuthenticatedUser(
        String id,
        String username,
        String email,
        Set<String> roles
) {}