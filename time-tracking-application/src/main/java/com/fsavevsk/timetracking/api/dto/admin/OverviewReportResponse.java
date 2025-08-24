package com.fsavevsk.timetracking.api.dto.admin;

import java.util.List;

public record OverviewReportResponse(
        int totalProjects,
        double hoursToday,
        double hoursThisWeek,
        List<TopProject> topProjectsThisWeek
) {
    public record TopProject(Long projectId, String projectName, double hours) {}
}