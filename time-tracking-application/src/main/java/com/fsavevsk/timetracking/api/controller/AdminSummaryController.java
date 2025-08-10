package com.fsavevsk.timetracking.api.controller;

import com.fsavevsk.timetracking.api.dto.admin.AdminOverviewResponse;
import com.fsavevsk.timetracking.api.dto.admin.ProjectSummaryRow;
import com.fsavevsk.timetracking.service.AdminSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.List;

@RequestMapping("/api/admin")
@RestController
@RequiredArgsConstructor
public class AdminSummaryController {

    private final AdminSummaryService service;

    @GetMapping("/overview")
    public ResponseEntity<AdminOverviewResponse> overview(
            @RequestParam(required = false) String tz
    ) {
        return ResponseEntity.ok(service.overview(resolveZone(tz)));
    }

    @GetMapping("/projects/summary")
    public ResponseEntity<List<ProjectSummaryRow>> projectsSummary(
            @RequestParam(required = false) String tz,
            @RequestParam(required = false) String from, // ISO LocalDateTime (optional)
            @RequestParam(required = false) String to    // ISO LocalDateTime (optional)
    ) {
        return ResponseEntity.ok(service.projectsSummary(resolveZone(tz), from, to));
    }

    private ZoneId resolveZone(String tz) {
        return (tz == null || tz.isBlank()) ? ZoneId.systemDefault() : ZoneId.of(tz);
    }
}
