package com.fsavevsk.timetracking.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fsavevsk.timetracking.api.dto.admin.OverviewReportResponse;
import com.fsavevsk.timetracking.api.dto.admin.ProjectsReportResponse;
import com.fsavevsk.timetracking.api.exception.ApiError;
import com.fsavevsk.timetracking.integration.base.AbstractIntegrationTest;
import com.fsavevsk.timetracking.persistence.entity.ProjectEntity;
import com.fsavevsk.timetracking.persistence.entity.TimeEntryEntity;
import com.fsavevsk.timetracking.persistence.repository.ProjectRepository;
import com.fsavevsk.timetracking.persistence.repository.TimeEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller-layer integration tests for Admin Summary API.
 */
class AdminSummaryControllerIT extends AbstractIntegrationTest {

    @Autowired
    ProjectRepository projectRepository;
    @Autowired
    TimeEntryRepository timeEntryRepository;

    // Use a fixed "now" to make week/day windows deterministic:
    // Monday, 2025-08-18 12:00:00 UTC
    static final Instant FIXED_NOW = Instant.parse("2025-08-18T12:00:00Z");

    @TestConfiguration
    static class FixedClockConfig {
        @Bean(name = "fixedClockForTests")  // different name than "clock"
        @Primary
        Clock fixedClock() {
            return Clock.fixed(FIXED_NOW, ZoneOffset.UTC);
        }
    }

    @BeforeEach
    void setup() {
        timeEntryRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @Test
    void should_returnOverview_withTotalsAndTopProjects() throws Exception {
        // given
        ZoneId zone = ZoneId.of("UTC");
        ProjectEntity a = saveProject("Alpha");
        ProjectEntity b = saveProject("Beta");

        // Today (Mon 2025-08-18 UTC): Beta 90 minutes
        saveEntry(b, LocalDateTime.ofInstant(FIXED_NOW, zone).withHour(10).withMinute(0), 90);

        // Same week: Alpha 120 minutes (Tue)
        saveEntry(a, LocalDateTime.parse("2025-08-19T09:00:00"), 120);

        // Outside this week: Alpha 300 minutes (previous week) – should NOT count in "this week"
        saveEntry(a, LocalDateTime.parse("2025-08-10T12:00:00"), 300);

        // when
        OverviewReportResponse res = performGetRequest(
                ADMIN_REPORTS_OVERVIEW_ENDPOINT + "?timezone=UTC",
                OverviewReportResponse.class,
                status().isOk()
        );

        // then
        assertEquals(2, res.totalProjects());
        assertEquals(1.5, res.hoursToday());     // 90m -> 1.5h
        assertEquals(3.5, res.hoursThisWeek());  // 120 + 90 = 210m -> 3.5h

        assertNotNull(res.topProjectsThisWeek());
        assertFalse(res.topProjectsThisWeek().isEmpty());
        // Top by weekly minutes: Alpha (120m = 2.0h) before Beta (90m = 1.5h)
        assertEquals("Alpha", res.topProjectsThisWeek().get(0).projectName());
        assertEquals(2.0, res.topProjectsThisWeek().get(0).hours());
        assertEquals("Beta", res.topProjectsThisWeek().get(1).projectName());
        assertEquals(1.5, res.topProjectsThisWeek().get(1).hours());
    }

    @Test
    void should_returnOverview_withZeros_whenNoData() throws Exception {
        // when
        OverviewReportResponse res = performGetRequest(
                ADMIN_REPORTS_OVERVIEW_ENDPOINT + "?timezone=UTC",
                OverviewReportResponse.class,
                status().isOk()
        );

        // then
        assertEquals(0, res.totalProjects());
        assertEquals(0.0, res.hoursToday());
        assertEquals(0.0, res.hoursThisWeek());
        assertTrue(res.topProjectsThisWeek().isEmpty());
    }

    @Test
    void should_returnProjectsSummary_forGivenRange() throws Exception {
        // given
        ProjectEntity a = saveProject("Alpha");
        ProjectEntity b = saveProject("Beta");

        // Week window (starting Mon 2025-08-18 in UTC): make Alpha=180m, Beta=60m
        saveEntry(a, LocalDateTime.parse("2025-08-18T08:00:00"), 60);  // Mon
        saveEntry(a, LocalDateTime.parse("2025-08-20T09:00:00"), 120); // Wed
        saveEntry(b, LocalDateTime.parse("2025-08-19T11:00:00"), 60);  // Tue

        // Month range: "from" to "to" (use whole Aug 2025)
        // Add extra minutes in August for Beta so hoursMonth differs from hoursWeek
        saveEntry(b, LocalDateTime.parse("2025-08-05T14:00:00"), 540); // 9h earlier in month

        String from = "2025-08-01";
        String to = "2025-08-31";

        // when
        List<ProjectsReportResponse> rows = performGetRequest(
                ADMIN_REPORTS_PROJECTS_ENDPOINT + "?timezone=UTC&startDate=" + from + "&endDate=" + to,
                new TypeReference<>() {
                },
                status().isOk()
        );

        // then
        assertEquals(2, rows.size());
        // Sorted by hoursWeek desc: Alpha (180m=3.0h) before Beta (60m=1.0h)
        ProjectsReportResponse first = rows.get(0);
        ProjectsReportResponse second = rows.get(1);

        assertEquals("Alpha", first.projectName());
        assertEquals(3.0, first.hoursWeek());
        assertEquals(3.0, first.hoursMonth()); // only week entries for Alpha (180m)

        assertEquals("Beta", second.projectName());
        assertEquals(1.0, second.hoursWeek()); // 60m
        assertEquals(10.0, second.hoursMonth()); // 60m + 540m = 600m -> 10.0h
        assertNotNull(second.lastEntryAt());
    }

    @Test
    void should_returnBadRequest_whenTimezoneIsInvalid() throws Exception {
        // when
        ApiError error = performGetRequest(
                ADMIN_REPORTS_OVERVIEW_ENDPOINT + "?timezone=Not/AZone",
                ApiError.class,
                status().is4xxClientError()
        );

        // then
        assertNotNull(error);
        assertEquals(400, error.status());
        assertEquals("Bad Request", error.error());
        assertEquals(ADMIN_REPORTS_OVERVIEW_ENDPOINT, error.path());
    }

    // ---------- helpers ----------

    private ProjectEntity saveProject(String name) {
        ProjectEntity p = new ProjectEntity();
        p.setName(name);
        return projectRepository.saveAndFlush(p);
    }

    private void saveEntry(ProjectEntity project, LocalDateTime start, int minutes) {
        TimeEntryEntity te = new TimeEntryEntity();
        te.setProject(project);
        te.setTitle("work");
        te.setStartTime(start);
        te.setEndTime(start.plusMinutes(minutes));
        te.setDurationMinutes(minutes);
        timeEntryRepository.saveAndFlush(te);
    }
}
