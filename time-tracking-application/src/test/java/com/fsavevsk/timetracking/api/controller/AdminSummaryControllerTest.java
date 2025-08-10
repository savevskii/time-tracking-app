package com.fsavevsk.timetracking.api.controller;

import com.fsavevsk.timetracking.api.dto.admin.AdminOverviewResponse;
import com.fsavevsk.timetracking.api.dto.admin.ProjectSummaryRow;
import com.fsavevsk.timetracking.api.exception.GlobalExceptionHandler;
import com.fsavevsk.timetracking.service.AdminSummaryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminSummaryController.class)
@Import(GlobalExceptionHandler.class)
class AdminSummaryControllerTest {

    @MockitoBean
    AdminSummaryService service;

    @Autowired
    MockMvc mvc;

    @Test
    void overview_ok_defaultZone() throws Exception {
        var top = List.of(new AdminOverviewResponse.TopProject(3L, "TrackLight", 4.3));
        var resp = new AdminOverviewResponse(12, 18.5, 240.0, top);

        given(service.overview(any(ZoneId.class))).willReturn(resp);

        mvc.perform(get("/api/admin/overview").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalProjects").value(12))
                .andExpect(jsonPath("$.hoursToday").value(18.5))
                .andExpect(jsonPath("$.hoursThisWeek").value(240.0))
                .andExpect(jsonPath("$.topProjectsThisWeek[0].projectName").value("TrackLight"));

        then(service).should().overview(any(ZoneId.class));
        then(service).shouldHaveNoMoreInteractions();
    }

    @Test
    void overview_ok_explicitZone() throws Exception {
        var resp = new AdminOverviewResponse(2, 3.0, 12.0, List.of());
        given(service.overview(ZoneId.of("Europe/Skopje"))).willReturn(resp);

        mvc.perform(get("/api/admin/overview")
                        .with(jwt())
                        .param("tz", "Europe/Skopje"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjects").value(2))
                .andExpect(jsonPath("$.hoursThisWeek").value(12.0));

        then(service).should().overview(ZoneId.of("Europe/Skopje"));
        then(service).shouldHaveNoMoreInteractions();
    }

    @Test
    void projectsSummary_ok_withDefaults() throws Exception {
        var rows = List.of(
                new ProjectSummaryRow(3L, "TrackLight", 42.5, 156.0, 38, LocalDateTime.of(2025,8,10,16,45)),
                new ProjectSummaryRow(7L, "SolidTime", 31.0, 120.5, 22, LocalDateTime.of(2025,8,10,15,10))
        );
        given(service.projectsSummary(any(ZoneId.class), isNull(), isNull())).willReturn(rows);

        mvc.perform(get("/api/admin/projects/summary").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].projectName").value("TrackLight"))
                .andExpect(jsonPath("$[1].hoursWeek").value(31.0));

        then(service).should().projectsSummary(any(ZoneId.class), isNull(), isNull());
        then(service).shouldHaveNoMoreInteractions();
    }

    @Test
    void projectsSummary_ok_withExplicitParams() throws Exception {
        var rows = List.of(new ProjectSummaryRow(3L, "TrackLight", 10.0, 40.0, 8, LocalDateTime.of(2025,8,10,10,0)));
        given(service.projectsSummary(ZoneId.of("Europe/Skopje"), "2025-07-01T00:00", "2025-07-31T23:59"))
                .willReturn(rows);

        mvc.perform(get("/api/admin/projects/summary")
                        .with(jwt())
                        .param("tz", "Europe/Skopje")
                        .param("from", "2025-07-01T00:00")
                        .param("to",   "2025-07-31T23:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].projectId").value(3));

        then(service).should().projectsSummary(ZoneId.of("Europe/Skopje"), "2025-07-01T00:00", "2025-07-31T23:59");
        then(service).shouldHaveNoMoreInteractions();
    }
}