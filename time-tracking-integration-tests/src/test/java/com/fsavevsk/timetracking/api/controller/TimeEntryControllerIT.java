package com.fsavevsk.timetracking.api.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fsavevsk.timetracking.api.dto.CreateTimeEntryRequest;
import com.fsavevsk.timetracking.api.dto.TimeEntryResponse;
import com.fsavevsk.timetracking.api.exception.ApiError;
import com.fsavevsk.timetracking.base.AbstractWebIT;
import com.fsavevsk.timetracking.persistence.entity.ProjectEntity;
import com.fsavevsk.timetracking.persistence.entity.TimeEntryEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller-layer integration tests for Time Entry API.
 */
class TimeEntryControllerIT extends AbstractWebIT {

    private static final String TIME_ENTRIES_API_ENDPOINT = "/api/time-entries";

    @Test
    void should_listTimeEntriesForCurrentUserSortedDesc() throws Exception {
        // given
        ProjectEntity project = seedProject();
        TimeEntryEntity timeEntry = generateTimeEntry(project);
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
        ProjectEntity project = seedProject();
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
        ProjectEntity project = seedProject();
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
        ProjectEntity project = seedProject();
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
        ProjectEntity project = seedProject();
        TimeEntryEntity timeEntry = generateTimeEntry(project);
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

    private TimeEntryEntity generateTimeEntry(ProjectEntity projectEntity) {
        TimeEntryEntity timeEntryEntity = new TimeEntryEntity();
        timeEntryEntity.setProject(projectEntity);
        timeEntryEntity.setTitle("Test Time");
        timeEntryEntity.setDescription("Test description");
        timeEntryEntity.setStartTime(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
        timeEntryEntity.setEndTime(LocalDateTime.now().plusHours(3).truncatedTo(ChronoUnit.MICROS));
        timeEntryEntity.setUserId("it-user");
        return timeEntryEntity;
    }

    private CreateTimeEntryRequest generateCreateTimeEntryRequest(Long projectId) {
        return new CreateTimeEntryRequest(
                projectId,
                "Implement MockMvc IT",
                LocalDateTime.now().truncatedTo(ChronoUnit.MICROS),
                LocalDateTime.now().plusHours(3).truncatedTo(ChronoUnit.MICROS),
                "End-to-end controller test"
        );
    }

    private ProjectEntity seedProject() {
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setName("Test Project");
        projectEntity.setDescription("Test description");
        return projectRepository.save(projectEntity);
    }
}