package com.fsavevsk.timetracking.service;

import com.fsavevsk.timetracking.api.dto.admin.AdminOverviewResponse;
import com.fsavevsk.timetracking.api.dto.admin.ProjectSummaryRow;

import java.time.ZoneId;
import java.util.List;

public interface AdminSummaryService {
    AdminOverviewResponse overview(ZoneId zone);
    List<ProjectSummaryRow> projectsSummary(ZoneId zone, String fromIso, String toIso);
}

