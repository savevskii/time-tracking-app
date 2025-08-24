package com.fsavevsk.timetracking.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fsavevsk.timetracking.api.dto.CreateTimeEntryRequest;
import com.fsavevsk.timetracking.api.dto.TimeEntryResponse;
import com.fsavevsk.timetracking.api.exception.ApiError;
import com.fsavevsk.timetracking.integration.base.AbstractIntegrationTest;
import com.fsavevsk.timetracking.persistence.entity.ProjectEntity;
import com.fsavevsk.timetracking.persistence.entity.TimeEntryEntity;
import com.fsavevsk.timetracking.persistence.repository.ProjectRepository;
import com.fsavevsk.timetracking.persistence.repository.TimeEntryRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller-layer integration tests for Time Entry API.
 */
class TimeEntryControllerIT extends AbstractIntegrationTest {

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    TimeEntryRepository timeEntryRepository;

    private ProjectEntity project;

    @BeforeEach
    public void setup() {
        timeEntryRepository.deleteAll();
    }

    @BeforeAll
    public void seedProject() {
        ProjectEntity entity = generateProjectEntity();
        project = projectRepository.save(entity);
    }

    @Test
    void should_listTimeEntriesForCurrentUserSortedDesc() throws Exception {
        // given
        TimeEntryEntity timeEntry = generateTimeEntryEntity(project);
        timeEntryRepository.save(timeEntry);

        // when
        List<TimeEntryResponse> timeEntries = performGetRequest(
                TIME_ENTRIES_API_ENDPOINT, new TypeReference<>() {}, status().isOk());

        // then
        assertEquals(1, timeEntries.size());
        var first = timeEntries.getFirst();
        assertEquals(timeEntry.getId(), first.id());
        assertEquals(timeEntry.getTitle(), first.title());
        assertEquals(timeEntry.getDescription(), first.description());
        assertEquals(timeEntry.getStartTime(), first.startTime());
        assertEquals(timeEntry.getEndTime(), first.endTime());
    }

    @Test
    void should_ReturnEmptyList_whenNoEntriesForCurrentUser() throws Exception {
        // when
        List<TimeEntryResponse> timeEntries = performGetRequest(
                TIME_ENTRIES_API_ENDPOINT, new TypeReference<>() {}, status().isOk());

        // then
        assertEquals(0, timeEntries.size());
    }

    @Test
    void should_createTimeEntry_whenRequestIsValid() throws Exception {
        // given
        CreateTimeEntryRequest request = generateCreateTimeEntryRequest(project.getId());

        // when
        TimeEntryResponse timeEntry = performPostRequest(TIME_ENTRIES_API_ENDPOINT, request, TimeEntryResponse.class, status().isOk());

        // then
        assertNotNull(timeEntry);
        assertEquals(request.title(), timeEntry.title());
        assertEquals(request.description(), timeEntry.description());
        assertEquals(request.startTime(), timeEntry.startTime());
        assertEquals(request.endTime(), timeEntry.endTime());
        assertEquals(request.projectId(), timeEntry.projectId());
        assertEquals(180, timeEntry.durationMinutes());
    }

    @Test
    void should_returnBadRequest_whenEndTimeIsNotAfterStartTime() throws Exception {
        // given
        var start = LocalDateTime.parse("2025-08-19T10:00:00");
        var end = start.minusHours(1); // invalid
        var request = new CreateTimeEntryRequest(project.getId(), "invalid", start, end, null);

        // when
        ApiError error = performPostRequest(TIME_ENTRIES_API_ENDPOINT, request, ApiError.class, status().is4xxClientError());

        // then
        assertNotNull(error);
        assertEquals(BAD_REQUEST.value(), error.status());
        assertEquals("Bad Request", error.error());
        assertEquals("Time entry end time must be after start time", error.message());
        assertEquals(TIME_ENTRIES_API_ENDPOINT, error.path());
    }

    @Test
    void should_returnBadRequest_whenRequestContainsInvalidFields() throws Exception {
        // given
        var start = LocalDateTime.parse("2025-08-19T10:00:00");
        var end = start.plusMinutes(15);
        var request = new CreateTimeEntryRequest(project.getId(), "", start, end, "desc");

        // when
        ApiError error = performPostRequest(TIME_ENTRIES_API_ENDPOINT, request, ApiError.class, status().is4xxClientError());

        // then
        assertNotNull(error);
        assertEquals(BAD_REQUEST.value(), error.status());
        assertEquals("Validation Failed", error.error());
        assertEquals("One or more fields are invalid", error.message());
        assertEquals(TIME_ENTRIES_API_ENDPOINT, error.path());
        assertNotNull(error.fieldErrors());
        assertEquals(1, error.fieldErrors().size());
    }

    @Test
    void should_returnNotFound_whenProjectNotExists() throws Exception {
        // given
        var start = LocalDateTime.parse("2025-08-19T10:00:00");
        var end = start.plusMinutes(15);
        var request = new CreateTimeEntryRequest(999_999L, "x", start, end, null);

        // when
        ApiError error = performPostRequest(TIME_ENTRIES_API_ENDPOINT, request, ApiError.class, status().isNotFound());

        // then
        assertNotNull(error);
        assertEquals(NOT_FOUND.value(), error.status());
        assertEquals("Not Found", error.error());
        assertEquals("Project not found", error.message());
        assertEquals(TIME_ENTRIES_API_ENDPOINT, error.path());
    }

    @Test
    void should_deleteSuccessfully_whenExists() throws Exception {
        // given
        TimeEntryEntity timeEntry = generateTimeEntryEntity(project);
        timeEntryRepository.save(timeEntry);

        // when
        performDeleteRequestNoContent(TIME_ENTRIES_API_ENDPOINT + "/" + timeEntry.getId(), status().isNoContent());

        // then
        assertFalse(timeEntryRepository.existsById(timeEntry.getId()));
    }

    @Test
    void should_returnNotFound_whenDeleteNotExisting() throws Exception {
        // when & then
        performDeleteRequestNoContent(TIME_ENTRIES_API_ENDPOINT + "/" + 1234, status().isNotFound());
    }
}