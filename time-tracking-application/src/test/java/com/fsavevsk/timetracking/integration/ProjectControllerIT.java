package com.fsavevsk.timetracking.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fsavevsk.timetracking.api.dto.CreateProject;
import com.fsavevsk.timetracking.api.dto.Project;
import com.fsavevsk.timetracking.api.exception.ApiError;
import com.fsavevsk.timetracking.integration.base.AbstractIntegrationTest;
import com.fsavevsk.timetracking.persistence.entity.ProjectEntity;
import com.fsavevsk.timetracking.persistence.repository.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller-layer integration tests for Projects API.
 */
class ProjectControllerIT extends AbstractIntegrationTest {

    @Autowired
    ProjectRepository projectRepository;

    @BeforeEach
    void setup() {
        projectRepository.deleteAll();
    }

    @Test
    void should_listProjects() throws Exception {
        // given
        ProjectEntity p = generateProjectEntity();
        projectRepository.save(p);

        // when
        List<Project> projects = performGetRequest(
                PROJECTS_API_ENDPOINT, new TypeReference<>() {}, status().isOk());

        // then
        assertEquals(1, projects.size());
        Project first = projects.getFirst();
        assertEquals(p.getId(), first.id());
        assertEquals(p.getName(), first.name());
    }

    @Test
    void should_returnEmptyList_whenNoProjects() throws Exception {
        // when
        List<Project> projects = performGetRequest(
                PROJECTS_API_ENDPOINT, new TypeReference<>() {}, status().isOk());

        // then
        assertNotNull(projects);
        assertTrue(projects.isEmpty());
    }

    @Test
    void should_createProject_whenRequestIsValid() throws Exception {
        // given
        CreateProject request = new CreateProject("New Project", "");

        // when
        Project created = performPostRequest(
                PROJECTS_API_ENDPOINT, request, Project.class, status().isOk());

        // then
        assertNotNull(created);
        assertNotNull(created.id());
        assertEquals("New Project", created.name());

        // and the entity is persisted
        assertTrue(projectRepository.existsById(created.id()));
    }

    @Test
    void should_returnBadRequest_whenRequestContainsInvalidFields() throws Exception {
        // given
        CreateProject request = new CreateProject("", "");

        // when
        ApiError error = performPostRequest(
                PROJECTS_API_ENDPOINT, request, ApiError.class, status().is4xxClientError());

        // then
        assertNotNull(error);
        assertEquals(BAD_REQUEST.value(), error.status());
        assertEquals("Validation Failed", error.error());
        assertEquals("One or more fields are invalid", error.message());
        assertEquals(PROJECTS_API_ENDPOINT, error.path());
        assertNotNull(error.fieldErrors());
        assertFalse(error.fieldErrors().isEmpty());
    }

    @Test
    void should_getProjectByName_whenExists() throws Exception {
        // given
        ProjectEntity p = generateProjectEntity();
        p.setName("Alpha");
        p = projectRepository.save(p);

        // when
        Project found = performGetRequest(
                PROJECTS_API_ENDPOINT + "/search?projectName=Alpha", Project.class, status().isOk());

        // then
        assertNotNull(found);
        assertEquals(p.getId(), found.id());
        assertEquals("Alpha", found.name());
    }

    @Test
    void should_returnNotFound_whenGetByNameNotExisting() throws Exception {
        // when
        ApiError error = performGetRequest(
                PROJECTS_API_ENDPOINT + "/search?projectName=DoesNotExist", ApiError.class, status().isNotFound());

        // then
        assertNotNull(error);
        assertEquals(NOT_FOUND.value(), error.status());
        assertEquals("Not Found", error.error());
        assertEquals("Project not found", error.message());
        assertEquals(PROJECTS_API_ENDPOINT + "/search", error.path());
    }

    @Test
    void should_deleteSuccessfully_whenExists() throws Exception {
        // given
        ProjectEntity p = generateProjectEntity();
        p = projectRepository.save(p);

        // when
        performDeleteRequestNoContent(PROJECTS_API_ENDPOINT + "/" + p.getId(), status().isOk());

        // then
        assertFalse(projectRepository.existsById(p.getId()));
    }

    @Test
    void should_returnNotFound_whenDeleteNotExisting() throws Exception {
        // when & then
        performDeleteRequestNoContent(PROJECTS_API_ENDPOINT + "/" + 123456L, status().isNotFound());
    }
}