package com.fsavevsk.timetracking.unit.controller;

import com.fsavevsk.timetracking.api.controller.AdminSummaryController;
import com.fsavevsk.timetracking.api.dto.admin.OverviewReportResponse;
import com.fsavevsk.timetracking.api.dto.admin.ProjectsReportResponse;
import com.fsavevsk.timetracking.api.exception.GlobalExceptionHandler;
import com.fsavevsk.timetracking.service.AdminReportsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminSummaryController.class)
@Import(GlobalExceptionHandler.class)
class AdminSummaryControllerTest {

    @MockitoBean
    AdminReportsService service;

    @Autowired
    MockMvc mvc;

    @Test
    void should_returnOverview_withDefaultZoneUtc() throws Exception {
        var top = List.of(new OverviewReportResponse.TopProject(3L, "TrackLight", 4.3));
        var resp = new OverviewReportResponse(12, 18.5, 240.0, top);

        // expect controller to call service.overview(ZoneId.of("UTC")) when timezone param is omitted
        given(service.overview(any(ZoneId.class))).willReturn(resp);

        mvc.perform(get("/api/admin/reports/overview").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalProjects").value(12))
                .andExpect(jsonPath("$.hoursToday").value(18.5))
                .andExpect(jsonPath("$.hoursThisWeek").value(240.0))
                .andExpect(jsonPath("$.topProjectsThisWeek[0].projectName").value("TrackLight"));

        then(service).should().overview(ZoneId.of("UTC"));
        then(service).shouldHaveNoMoreInteractions();
    }

    @Test
    void should_returnOverview_withExplicitZone() throws Exception {
        var resp = new OverviewReportResponse(2, 3.0, 12.0, List.of());
        given(service.overview(ZoneId.of("Europe/Skopje"))).willReturn(resp);

        mvc.perform(get("/api/admin/reports/overview")
                        .with(jwt())
                        .param("timezone", "Europe/Skopje"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjects").value(2))
                .andExpect(jsonPath("$.hoursThisWeek").value(12.0));

        then(service).should().overview(ZoneId.of("Europe/Skopje"));
        then(service).shouldHaveNoMoreInteractions();
    }

    @Test
    void should_returnProjectsSummary_withDefaultWindow_andUtc() throws Exception {
        var rows = List.of(
                new ProjectsReportResponse(3L, "TrackLight", 42.5, 156.0, 38, LocalDateTime.of(2025, 8, 10, 16, 45)),
                new ProjectsReportResponse(7L, "SolidTime", 31.0, 120.5, 22, LocalDateTime.of(2025, 8, 10, 15, 10))
        );
        given(service.projectsSummary(any(ZoneId.class), any(LocalDate.class), any(LocalDate.class)))
                .willReturn(rows);

        mvc.perform(get("/api/admin/reports/projects").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].projectName").value("TrackLight"))
                .andExpect(jsonPath("$[1].hoursWeek").value(31.0));

        // capture computed defaults (to = today, from = to - 29 days) and zone
        ArgumentCaptor<ZoneId> zoneCap = ArgumentCaptor.forClass(ZoneId.class);
        ArgumentCaptor<LocalDate> fromCap = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> toCap = ArgumentCaptor.forClass(LocalDate.class);

        then(service).should().projectsSummary(zoneCap.capture(), fromCap.capture(), toCap.capture());
        then(service).shouldHaveNoMoreInteractions();

        assertEquals(ZoneId.of("UTC"), zoneCap.getValue());
        // controller default: last 30 days => to == from + 29
        assertEquals(fromCap.getValue().plusDays(29), toCap.getValue());
    }

    @Test
    void should_returnProjectsSummary_withExplicitParams() throws Exception {
        var startDate = LocalDate.parse("2025-07-01");
        var endDate = LocalDate.parse("2025-07-31");
        var rows = List.of(new ProjectsReportResponse(3L, "TrackLight", 10.0, 40.0, 8, LocalDateTime.of(2025, 8, 10, 10, 0)));

        given(service.projectsSummary(ZoneId.of("Europe/Skopje"), startDate, endDate))
                .willReturn(rows);

        mvc.perform(get("/api/admin/reports/projects")
                        .with(jwt())
                        .param("timezone", "Europe/Skopje")
                        .param("startDate", "2025-07-01")
                        .param("endDate", "2025-07-31"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].projectId").value(3));

        then(service).should().projectsSummary(ZoneId.of("Europe/Skopje"), startDate, endDate);
        then(service).shouldHaveNoMoreInteractions();
    }

    @Test
    void should_returnBadRequest_whenEndDateBeforeStartDate() throws Exception {
        mvc.perform(get("/api/admin/reports/projects")
                        .with(jwt())
                        .param("timezone", "UTC")
                        .param("startDate", "2025-08-10")
                        .param("endDate", "2025-08-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("endDate must be on or after startDate"))
                .andExpect(jsonPath("$.path").value("/api/admin/reports/projects"));

        then(service).shouldHaveNoInteractions();
    }

    @Test
    void should_returnBadRequest_whenTimezoneIsInvalid() throws Exception {
        mvc.perform(get("/api/admin/reports/overview")
                        .with(jwt())
                        .param("timezone", "Not/AZone"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.path").value("/api/admin/reports/overview"));

        then(service).shouldHaveNoInteractions();
    }
}