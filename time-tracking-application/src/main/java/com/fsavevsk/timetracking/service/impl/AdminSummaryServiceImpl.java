package com.fsavevsk.timetracking.service.impl;

import com.fsavevsk.timetracking.api.dto.admin.AdminOverviewResponse;
import com.fsavevsk.timetracking.api.dto.admin.ProjectSummaryRow;
import com.fsavevsk.timetracking.persistence.entity.ProjectEntity;
import com.fsavevsk.timetracking.persistence.repository.ProjectRepository;
import com.fsavevsk.timetracking.persistence.repository.TimeEntryRepository;
import com.fsavevsk.timetracking.service.AdminSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

import static com.fsavevsk.timetracking.util.DateRanges.*;

@Service
@RequiredArgsConstructor
public class AdminSummaryServiceImpl implements AdminSummaryService {

    private final ProjectRepository projectRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final Clock clock = Clock.systemUTC();

    @Override
    public AdminOverviewResponse overview(ZoneId zone) {
        LocalDateTime startOfDay = startOfToday(zone, clock);
        LocalDateTime endOfDay   = endOfToday(zone, clock);
        LocalDateTime startOfWeek = startOfWeek(zone, clock);
        LocalDateTime endOfWeek   = endOfWeek(zone, clock);

        int totalProjects = Math.toIntExact(projectRepository.count());
        double hoursToday = toHours1(nvl(timeEntryRepository.sumMinutesBetweenAll(startOfDay, endOfDay)));
        double hoursThisWeek = toHours1(nvl(timeEntryRepository.sumMinutesBetweenAll(startOfWeek, endOfWeek)));

        List<AdminOverviewResponse.TopProject> topProjects = timeEntryRepository
                .topProjectsByMinutes(startOfWeek, endOfWeek).stream()
                .map(r -> new AdminOverviewResponse.TopProject(
                        (Long) r[0],
                        (String) r[1],
                        toHours1(((Number) r[2]).intValue())
                ))
                .limit(10)
                .toList();

        return new AdminOverviewResponse(totalProjects, hoursToday, hoursThisWeek, topProjects);
    }

    @Override
    public List<ProjectSummaryRow> projectsSummary(ZoneId zone, String fromIso, String toIso) {
        // Defaults: current week + current month (can be overridden via from/to for the month window)
        LocalDateTime startOfWeek = startOfWeek(zone, clock);
        LocalDateTime endOfWeek   = endOfWeek(zone, clock);
        LocalDateTime startOfMonth = startOfMonth(zone, clock);
        LocalDateTime endOfMonth   = endOfMonth(zone, clock);

        LocalDateTime fromMonth = parseOr(startOfMonth, fromIso);
        LocalDateTime toMonth   = parseOr(endOfMonth, toIso);

        List<ProjectEntity> projects = projectRepository.findAll();

        return projects.stream()
                .map(p -> {
                    int weekMin   = nvl(timeEntryRepository.sumProjectMinutesBetween(p.getId(), startOfWeek, endOfWeek));
                    int monthMin  = nvl(timeEntryRepository.sumProjectMinutesBetween(p.getId(), fromMonth, toMonth));
                    long entriesW = nvl(timeEntryRepository.countEntriesForProjectBetween(p.getId(), startOfWeek, endOfWeek));
                    LocalDateTime last = timeEntryRepository.lastEntryAtForProject(p.getId());

                    return new ProjectSummaryRow(
                            p.getId(),
                            p.getName(),
                            toHours1(weekMin),
                            toHours1(monthMin),
                            entriesW,
                            last
                    );
                })
                .sorted(Comparator.comparing(ProjectSummaryRow::hoursWeek).reversed())
                .toList();
    }

    private static double toHours1(int minutes) {
        return Math.round((minutes / 60.0) * 10.0) / 10.0;
    }

    private static int nvl(Integer v) {
        return v == null ? 0 : v;
    }

    private static long nvl(Long v) {
        return v == null ? 0L : v;
    }
}