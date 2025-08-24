package com.fsavevsk.timetracking.api.dto;

public record Project(
        Long id,
        String name,
        String description
) {}
