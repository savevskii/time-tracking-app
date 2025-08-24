package com.fsavevsk.timetracking.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateProject(
        @NotBlank(message = "Project name must not be blank")
        String name,
        String description
) {}
