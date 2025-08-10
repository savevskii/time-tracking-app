package com.fsavevsk.timetracking.security;

public interface CurrentUserService {
    AuthenticatedUser requireUser();
    String userId();
}