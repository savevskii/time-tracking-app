package com.fsavevsk.timetracking.api.dto.admin;

import java.time.LocalDateTime;

public record ProjectSummaryRow(
        Long projectId,
        String projectName,
        double hoursWeek,
        double hoursMonth,
        long entriesWeek,
        LocalDateTime lastEntryAt
) {}