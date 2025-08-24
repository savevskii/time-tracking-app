package com.fsavevsk.timetracking.service;

import com.fsavevsk.timetracking.api.dto.admin.OverviewReportResponse;
import com.fsavevsk.timetracking.api.dto.admin.ProjectsReportResponse;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public interface AdminReportsService {
    OverviewReportResponse overview(ZoneId zone);

    List<ProjectsReportResponse> projectsSummary(ZoneId zone, LocalDate startDate, LocalDate endDate);
}

