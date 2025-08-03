package com.fsavevsk.timetracking.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsavevsk.timetracking.domain.model.Project;
import com.fsavevsk.timetracking.domain.service.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProjectController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
@ContextConfiguration(classes = ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnAllProjects() throws Exception {
        // Given
        Project project = new Project(1L, "Test Project", "Test Description");
        given(projectService.getAllProjects()).willReturn(List.of(project));

        // When / Then
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Project"))
                .andExpect(jsonPath("$[0].description").value("Test Description"));

        then(projectService).should().getAllProjects();
    }

    @Test
    void shouldReturnProjectByName() throws Exception {
        // Given
        String projectName = "My Project";
        String projectDescription = "My Description";
        Project project = new Project(1L, projectName, projectDescription);
        given(projectService.findProjectByName(projectName)).willReturn(project);

        // When / Then
        mockMvc.perform(get("/api/projects/search")
                        .param("projectName", projectName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(projectName))
                .andExpect(jsonPath("$.description").value(projectDescription));

        then(projectService).should().findProjectByName(projectName);
    }

    @Test
    void shouldCreateProject() throws Exception {
        // Given
        Project input = new Project(null, "New Project", "New Description");
        Project created = new Project(1L, "New Project", "New Description");

        given(projectService.createProject(any())).willReturn(created);

        // When / Then
        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("New Project"))
                .andExpect(jsonPath("$.description").value("New Description"));

        then(projectService).should().createProject(any());
    }

    @Test
    void shouldDeleteProject() throws Exception {
        // Given
        long id = 1L;
        willDoNothing().given(projectService).delete(id);

        // When / Then
        mockMvc.perform(delete("/api/projects/{projectId}", id))
                .andExpect(status().isOk());

        then(projectService).should().delete(id);
    }
}
