package com.fsavevsk.timetracking.service.impl;

import com.fsavevsk.timetracking.api.dto.admin.AdminOverviewResponse;
import com.fsavevsk.timetracking.api.dto.admin.ProjectSummaryRow;
import com.fsavevsk.timetracking.persistence.entity.ProjectEntity;
import com.fsavevsk.timetracking.persistence.repository.ProjectRepository;
import com.fsavevsk.timetracking.persistence.repository.TimeEntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AdminSummaryServiceImplTest {

    @org.mockito.Mock ProjectRepository projectRepo;
    @org.mockito.Mock TimeEntryRepository timeRepo;

    private static final ZoneId SKOPJE = ZoneId.of("Europe/Skopje");

    @Test
    void overview_convertsMinutesToHours_andMapsTopProjects() {
        // Given
        var svc = new AdminSummaryServiceImpl(projectRepo, timeRepo);

        given(projectRepo.count()).willReturn(12L);

        // First call: today minutes (3h = 180), second call: week minutes (13h = 780)
        given(timeRepo.sumMinutesBetweenAll(any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(180, 780);

        // top projects (minutes)
        given(timeRepo.topProjectsByMinutes(any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(List.of(
                        new Object[]{3L, "TrackLight", 255}, // 4.25h → 4.3 rounded
                        new Object[]{7L, "SolidTime", 120}   // 2h
                ));

        // When
        AdminOverviewResponse res = svc.overview(SKOPJE);

        // Then
        assertThat(res.totalProjects()).isEqualTo(12);
        assertThat(res.hoursToday()).isEqualTo(3.0);
        assertThat(res.hoursThisWeek()).isEqualTo(13.0);
        assertThat(res.topProjectsThisWeek()).hasSize(2);
        assertThat(res.topProjectsThisWeek().get(0).projectId()).isEqualTo(3L);
        assertThat(res.topProjectsThisWeek().get(0).projectName()).isEqualTo("TrackLight");
        assertThat(res.topProjectsThisWeek().get(0).hours()).isEqualTo(4.3);

        then(projectRepo).should().count();
        then(timeRepo).should(times(2)).sumMinutesBetweenAll(any(LocalDateTime.class), any(LocalDateTime.class));
        then(timeRepo).should().topProjectsByMinutes(any(LocalDateTime.class), any(LocalDateTime.class));
        then(timeRepo).shouldHaveNoMoreInteractions();
    }

    @Test
    void projectsSummary_aggregates_perProject_andSortsByWeekHours() {
        // Given
        var svc = new AdminSummaryServiceImpl(projectRepo, timeRepo);

        var p1 = new ProjectEntity(); p1.setId(1L); p1.setName("A");
        var p2 = new ProjectEntity(); p2.setId(2L); p2.setName("B");
        given(projectRepo.findAll()).willReturn(List.of(p1, p2));

        // For each project, service calls sumProjectMinutesBetween twice: (week) then (month)
        // Return values in that order:
        given(timeRepo.sumProjectMinutesBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(120, 480); // A: week=120 (2.0h), month=480 (8.0h)
        given(timeRepo.sumProjectMinutesBetween(eq(2L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(300, 90);  // B: week=300 (5.0h), month=90  (1.5h)

        given(timeRepo.countEntriesForProjectBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(3L);
        given(timeRepo.countEntriesForProjectBetween(eq(2L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(5L);

        given(timeRepo.lastEntryAtForProject(1L)).willReturn(LocalDateTime.of(2025, 8, 9, 12, 0));
        given(timeRepo.lastEntryAtForProject(2L)).willReturn(LocalDateTime.of(2025, 8, 10, 14, 30));

        // When
        List<ProjectSummaryRow> rows = svc.projectsSummary(ZoneId.of("Europe/Skopje"), null, null);

        // Then
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0).projectId()).isEqualTo(2L);
        assertThat(rows.get(0).hoursWeek()).isEqualTo(5.0);
        assertThat(rows.get(0).hoursMonth()).isEqualTo(1.5);
        assertThat(rows.get(1).projectId()).isEqualTo(1L);
        assertThat(rows.get(1).hoursWeek()).isEqualTo(2.0);
        assertThat(rows.get(1).hoursMonth()).isEqualTo(8.0);

        then(projectRepo).should().findAll();
        then(timeRepo).should(times(2)).sumProjectMinutesBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
        then(timeRepo).should(times(2)).sumProjectMinutesBetween(eq(2L), any(LocalDateTime.class), any(LocalDateTime.class));
        then(timeRepo).should().countEntriesForProjectBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class));
        then(timeRepo).should().countEntriesForProjectBetween(eq(2L), any(LocalDateTime.class), any(LocalDateTime.class));
        then(timeRepo).should().lastEntryAtForProject(1L);
        then(timeRepo).should().lastEntryAtForProject(2L);
        then(timeRepo).shouldHaveNoMoreInteractions();
    }

    @Test
    void projectsSummary_usesExplicitFromTo_forMonthWindow() {
        // Given
        var svc = new AdminSummaryServiceImpl(projectRepo, timeRepo);

        var p = new ProjectEntity(); p.setId(1L); p.setName("Only");
        given(projectRepo.findAll()).willReturn(List.of(p));

        // Week aggregate (any range)
        given(timeRepo.sumProjectMinutesBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(60); // 1.0h this week
        given(timeRepo.countEntriesForProjectBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(2L);
        given(timeRepo.lastEntryAtForProject(1L))
                .willReturn(LocalDateTime.of(2025, 8, 10, 10, 0));

        // Month aggregate should use explicit from/to
        var customFrom = LocalDateTime.parse("2025-07-01T00:00");
        var customTo   = LocalDateTime.parse("2025-07-31T23:59");

        // We want to ensure the month call used EXACTLY customFrom/customTo:
        willReturn(600) // 10h
                .given(timeRepo).sumProjectMinutesBetween(eq(1L), eq(customFrom), eq(customTo));

        // When
        List<ProjectSummaryRow> rows = svc.projectsSummary(
                SKOPJE, "2025-07-01T00:00", "2025-07-31T23:59"
        );

        // Then
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).hoursWeek()).isEqualTo(1.0);
        assertThat(rows.get(0).hoursMonth()).isEqualTo(10.0);

        then(timeRepo).should().sumProjectMinutesBetween(eq(1L), eq(customFrom), eq(customTo));
    }
}
