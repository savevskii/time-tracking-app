package com.fsavevsk.timetracking.service.impl;

import com.fsavevsk.timetracking.api.dto.admin.OverviewReportResponse;
import com.fsavevsk.timetracking.api.dto.admin.ProjectsReportResponse;
import com.fsavevsk.timetracking.persistence.repository.ProjectRepository;
import com.fsavevsk.timetracking.persistence.repository.TimeEntryRepository;
import com.fsavevsk.timetracking.service.AdminReportsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

import static com.fsavevsk.timetracking.util.DateRanges.*;

@Service
@RequiredArgsConstructor
public class AdminReportsServiceImpl implements AdminReportsService {

    private final ProjectRepository projectRepository;
    private final TimeEntryRepository timeEntryRepository;
    private final Clock clock;

    @Override
    public OverviewReportResponse overview(ZoneId zone) {
        LocalDateTime startOfDay = startOfToday(zone, clock);
        LocalDateTime endOfDay = endOfToday(zone, clock);
        LocalDateTime startOfWeek = startOfWeek(zone, clock);
        LocalDateTime endOfWeek = endOfWeek(zone, clock);

        int totalProjects = Math.toIntExact(projectRepository.count());
        double hoursToday = toHours1(timeEntryRepository.sumMinutesBetweenAll(startOfDay, endOfDay));
        double hoursWeek = toHours1(timeEntryRepository.sumMinutesBetweenAll(startOfWeek, endOfWeek));

        // reuse your existing top-projects query if you like
        var topProjects = timeEntryRepository.topProjectsByMinutes(startOfWeek, endOfWeek).stream()
                .map(r -> new OverviewReportResponse.TopProject(
                        (Long) r[0],
                        (String) r[1],
                        toHours1(((Number) r[2]).intValue())
                ))
                .limit(10)
                .toList();

        return new OverviewReportResponse(totalProjects, hoursToday, hoursWeek, topProjects);
    }

    @Override
    public List<ProjectsReportResponse> projectsSummary(ZoneId zone, LocalDate startDate, LocalDate endDate) {
        // week window (for “hoursWeek” & “entriesWeek”)
        LocalDateTime weekStart = startOfWeek(zone, clock);
        LocalDateTime weekEnd = endOfWeek(zone, clock);

        // range window (for “hoursMonth”—your API now allows any range)
        LocalDate today = LocalDate.now(clock.withZone(zone));
        LocalDate from = (startDate != null) ? startDate : today.withDayOfMonth(1);
        LocalDate to = (endDate != null) ? endDate : today;

        // interpret date range as [start 00:00, end 23:59:59.999...] in the given zone
        LocalDateTime rangeStart = from.atStartOfDay();
        LocalDateTime rangeEnd = to.plusDays(1).atStartOfDay().minusNanos(1);

        var aggregates = timeEntryRepository.summarizeAllProjects(weekStart, weekEnd, rangeStart, rangeEnd);

        return aggregates.stream()
                .map(a -> new ProjectsReportResponse(
                        a.getProjectId(),
                        a.getProjectName(),
                        toHours1(a.getMinutesWeek()),
                        toHours1(a.getMinutesRange()),
                        a.getEntriesWeek(),
                        a.getLastEntryAt()
                ))
                .sorted(Comparator.comparing(ProjectsReportResponse::hoursWeek).reversed())
                .toList();
    }

    private static double toHours1(int minutes) {
        return Math.round((minutes / 60.0) * 10.0) / 10.0;
    }

}