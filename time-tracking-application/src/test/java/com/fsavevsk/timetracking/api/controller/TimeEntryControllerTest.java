package com.fsavevsk.timetracking.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fsavevsk.timetracking.api.dto.CreateTimeEntryRequest;
import com.fsavevsk.timetracking.api.dto.TimeEntryResponse;
import com.fsavevsk.timetracking.api.exception.GlobalExceptionHandler;
import com.fsavevsk.timetracking.api.exception.NotFoundException;
import com.fsavevsk.timetracking.service.TimeEntryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TimeEntryController.class)
@Import(GlobalExceptionHandler.class)
class TimeEntryControllerTest {

    @MockitoBean
    TimeEntryService service;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;;

    @BeforeEach
    void setup() {
        om.registerModule(new JavaTimeModule());
    }

    @Nested
    @DisplayName("POST /api/time-entries")
    class CreateEntry {
        @Test
        @DisplayName("returns 200 and body when valid")
        void create_ok() throws Exception {
            var req = validCreateReq();
            var res = sampleResponse();

            given(service.createForCurrentUser(any(CreateTimeEntryRequest.class)))
                    .willReturn(res);

            mvc.perform(post("/api/time-entries")
                            .with(jwt()) // satisfy security
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.projectId").value(10))
                    .andExpect(jsonPath("$.title").value("Feature work"))
                    .andExpect(jsonPath("$.durationMinutes").value(210));

            then(service).should().createForCurrentUser(any(CreateTimeEntryRequest.class));
            then(service).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("returns 400 with fieldErrors when validation fails (missing title)")
        void create_validationError() throws Exception {
            var badReq = new CreateTimeEntryRequest(
                    10L, null,
                    LocalDateTime.of(2025, 8, 10, 9, 0),
                    LocalDateTime.of(2025, 8, 10, 12, 0),
                    "desc"
            );

            mvc.perform(post("/api/time-entries")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(badReq)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.fieldErrors[0].field").value("title"));

            then(service).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("returns 404 when project not found (bubbled from service)")
        void create_projectNotFound() throws Exception {
            var req = validCreateReq();

            given(service.createForCurrentUser(any(CreateTimeEntryRequest.class)))
                    .willThrow(new NotFoundException("Project not found"));

            mvc.perform(post("/api/time-entries")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Project not found"));

            then(service).should().createForCurrentUser(any(CreateTimeEntryRequest.class));
            then(service).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("GET /api/time-entries")
    class ListEntries {
        @Test
        @DisplayName("returns list for current user")
        void list_ok() throws Exception {
            given(service.listForCurrentUser())
                    .willReturn(List.of(sampleResponse()));

            mvc.perform(get("/api/time-entries").with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].title").value("Feature work"));

            then(service).should().listForCurrentUser();
            then(service).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("DELETE /api/time-entries/{id}")
    class DeleteEntry {
        @Test
        @DisplayName("returns 204 when deleted")
        void delete_noContent() throws Exception {
            willDoNothing().given(service).deleteForCurrentUser(1L);

            mvc.perform(delete("/api/time-entries/{id}", 1L).with(jwt()))
                    .andExpect(status().isNoContent());

            then(service).should().deleteForCurrentUser(1L);
            then(service).shouldHaveNoMoreInteractions();
        }

        @Test
        @DisplayName("returns 404 when not found / not owned")
        void delete_notFound() throws Exception {
            willThrow(new NotFoundException("Time entry not found"))
                    .given(service).deleteForCurrentUser(999L);

            mvc.perform(delete("/api/time-entries/{id}", 999L).with(jwt()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Time entry not found"));

            then(service).should().deleteForCurrentUser(999L);
            then(service).shouldHaveNoMoreInteractions();
        }
    }

    private CreateTimeEntryRequest validCreateReq() {
        return new CreateTimeEntryRequest(
                10L,
                "Feature work",
                LocalDateTime.of(2025, 8, 10, 9, 0),
                LocalDateTime.of(2025, 8, 10, 12, 30),
                "Refactor service layer"
        );
    }

    private TimeEntryResponse sampleResponse() {
        return new TimeEntryResponse(
                1L, 10L, "Project A",
                "Feature work",
                LocalDateTime.of(2025, 8, 10, 9, 0),
                LocalDateTime.of(2025, 8, 10, 12, 30),
                210,
                "Refactor service layer"
        );
    }
}