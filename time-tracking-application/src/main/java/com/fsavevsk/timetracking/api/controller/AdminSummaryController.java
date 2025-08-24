package com.fsavevsk.timetracking.api.controller;

import com.fsavevsk.timetracking.api.dto.admin.OverviewReportResponse;
import com.fsavevsk.timetracking.api.dto.admin.ProjectsReportResponse;
import com.fsavevsk.timetracking.service.AdminReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.List;

@RequestMapping("/api/admin/reports")
@RestController
@RequiredArgsConstructor
@Validated
public class AdminSummaryController {

    private final AdminReportsService service;

    @GetMapping("/overview")
    public ResponseEntity<OverviewReportResponse> overview(
            @RequestParam(name = "timezone", required = false, defaultValue = "UTC") String timezone) {
        return ResponseEntity.ok(service.overview(resolveZone(timezone)));
    }

    @GetMapping("/projects")
    public ResponseEntity<List<ProjectsReportResponse>> projectsSummary(
            @RequestParam(name = "timezone", required = false, defaultValue = "UTC") String timezone,
            @RequestParam(name = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        ZoneId zone = resolveZone(timezone);

        // default window: last 30 days ending today in the given zone
        LocalDate today = LocalDate.now(zone);
        LocalDate from = startDate != null ? startDate : today.minusDays(29);
        LocalDate to = endDate != null ? endDate : today;

        if (to.isBefore(from)) {
            throw new IllegalArgumentException("endDate must be on or after startDate");
        }

        return ResponseEntity.ok(service.projectsSummary(zone, from, to));
    }

    private ZoneId resolveZone(String tz) {
        try {
            return ZoneId.of(tz);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid timezone: " + tz);
        }
    }
}
