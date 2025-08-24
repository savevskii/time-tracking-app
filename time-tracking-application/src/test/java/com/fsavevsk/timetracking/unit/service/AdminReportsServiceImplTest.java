package com.fsavevsk.timetracking.unit.service;

import com.fsavevsk.timetracking.api.dto.admin.OverviewReportResponse;
import com.fsavevsk.timetracking.persistence.projection.ProjectSummaryAggregate;
import com.fsavevsk.timetracking.persistence.repository.ProjectRepository;
import com.fsavevsk.timetracking.persistence.repository.TimeEntryRepository;
import com.fsavevsk.timetracking.service.AdminReportsService;
import com.fsavevsk.timetracking.service.impl.AdminReportsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AdminReportsServiceImplTest {

    @Mock
    ProjectRepository projectRepository;
    @Mock
    TimeEntryRepository timeEntryRepository;
    @Mock
    private Clock fixedClock;

    private AdminReportsService service;


    @BeforeEach
    void setUp() {
        // Monday 2025-08-18 12:00 UTC (so week boundaries are consistent)
        Instant fixedInstant = Instant.parse("2025-08-18T12:00:00Z");
        fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC);
        service = new AdminReportsServiceImpl(projectRepository, timeEntryRepository, fixedClock);
    }

    @Test
    void should_mapCountsAndHoursCorrectly_inOverview() {
        when(projectRepository.count()).thenReturn(5L);

        // Stub minutes today & this week
        // The exact from/to aren’t important; we just need to return values.
        when(timeEntryRepository.sumMinutesBetweenAll(any(), any()))
                .thenReturn(90)  // first call: today
                .thenReturn(305) // second call: this week
        ;

        // top projects query: return 3 rows [id, name, minutes]
        when(timeEntryRepository.topProjectsByMinutes(any(), any())).thenReturn(
                java.util.List.of(
                        new Object[]{1L, "A", 200},
                        new Object[]{2L, "B", 150},
                        new Object[]{3L, "C", 60}
                )
        );

        ZoneId zone = ZoneId.of("UTC");
        OverviewReportResponse res = service.overview(zone);

        assertEquals(5, res.totalProjects());
        assertEquals(1.5, res.hoursToday());     // 90m => 1.5h
        assertEquals(5.1, res.hoursThisWeek());  // 305m => 5.1h (rounded to 1 decimal)

        assertEquals(3, res.topProjectsThisWeek().size());
        assertEquals(1L, res.topProjectsThisWeek().getFirst().projectId());
        assertEquals("A", res.topProjectsThisWeek().getFirst().projectName());
        assertEquals(3.3, res.topProjectsThisWeek().getFirst().hours()); // 200m => 3.3h
    }

    @Test
    void should_convertAggregateAndSortByWeekHoursDesc_inProjectsSummary() {
        // mock aggregate rows
        var a1 = agg(10L, "Alpha", 180, 600, 3L, LocalDateTime.parse("2025-08-17T10:00:00"));
        var a2 = agg(20L, "Bravo", 60, 1200, 2L, LocalDateTime.parse("2025-08-16T09:00:00"));
        var a3 = agg(30L, "Charlie", 240, 0, 5L, null);

        when(timeEntryRepository.summarizeAllProjects(any(), any(), any(), any()))
                .thenReturn(List.of(a1, a2, a3));

        var zone = ZoneId.of("UTC");
        var rows = service.projectsSummary(zone, /*startDate*/ null, /*endDate*/ null);

        // expect sorting by hoursWeek desc: Charlie(240m=4.0h), Alpha(180m=3.0h), Bravo(60m=1.0h)
        assertEquals(3, rows.size());
        assertEquals("Charlie", rows.getFirst().projectName());
        assertEquals(4.0, rows.get(0).hoursWeek());
        assertEquals(0.0, rows.get(0).hoursMonth()); // 0 minutes

        assertEquals("Alpha", rows.get(1).projectName());
        assertEquals(3.0, rows.get(1).hoursWeek());
        assertEquals(10.0, rows.get(1).hoursMonth()); // 600m

        assertEquals("Bravo", rows.get(2).projectName());
        assertEquals(1.0, rows.get(2).hoursWeek());
        assertEquals(20.0, rows.get(2).hoursMonth()); // 1200m

        // verify repo called with 4 timestamps (week & range windows)
        verify(timeEntryRepository, times(1)).summarizeAllProjects(any(), any(), any(), any());
    }

    private static ProjectSummaryAggregate agg(Long id, String name, Integer minutesWeek,
                                               Integer minutesRange, Long entriesWeek, LocalDateTime last) {
        return new ProjectSummaryAggregate() {
            public Long getProjectId() {
                return id;
            }

            public String getProjectName() {
                return name;
            }

            public Integer getMinutesWeek() {
                return minutesWeek;
            }

            public Integer getMinutesRange() {
                return minutesRange;
            }

            public Long getEntriesWeek() {
                return entriesWeek;
            }

            public LocalDateTime getLastEntryAt() {
                return last;
            }
        };
    }
}
