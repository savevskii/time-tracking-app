package com.fsavevsk.timetracking.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateTimeEntryRequest(
        @NotNull Long projectId,
        @NotNull @Size(min = 1, max = 120) String title,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime,
        @Size(max = 500) String description
) {}