package com.fsavevsk.timetracking.api.dto;

import java.time.LocalDateTime;

public record TimeEntryResponse(
        Long id,
        Long projectId,
        String projectName,
        String title,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer durationMinutes,
        String description
) {}