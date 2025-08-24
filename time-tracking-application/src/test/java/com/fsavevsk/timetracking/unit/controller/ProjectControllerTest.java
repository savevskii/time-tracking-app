package com.fsavevsk.timetracking.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsavevsk.timetracking.api.controller.ProjectController;
import com.fsavevsk.timetracking.api.dto.CreateProject;
import com.fsavevsk.timetracking.api.dto.Project;
import com.fsavevsk.timetracking.api.exception.GlobalExceptionHandler;
import com.fsavevsk.timetracking.api.exception.NotFoundException;
import com.fsavevsk.timetracking.service.ProjectService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProjectController.class)
@Import(GlobalExceptionHandler.class)
class ProjectControllerTest {

    @MockitoBean
    ProjectService projectService;

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    @Nested
    class GetAllProjectsTests {
        @Test
        void should_fetchAllProjects() throws Exception {
            var p1 = new Project(1L, "TrackLight", "Time tracking");
            var p2 = new Project(2L, "SolidTime", "Clone base");

            given(projectService.getAllProjects()).willReturn(List.of(p1, p2));

            mvc.perform(get("/api/projects").with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("TrackLight"))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].name").value("SolidTime"));

            then(projectService).should().getAllProjects();
            then(projectService).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    class CreateProjectTests {
        @Test
        void should_createProject_WhenRequestIsValid() throws Exception {
            var req = new CreateProject("TrackLight", "Time tracking");

            var created = new Project(1L, "TrackLight", "Time tracking");

            given(projectService.createProject(any(CreateProject.class))).willReturn(created);

            mvc.perform(post("/api/projects")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("TrackLight"))
                    .andExpect(jsonPath("$.description").value("Time tracking"));

            then(projectService).should().createProject(any(CreateProject.class));
            then(projectService).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    class SearchProjectByNameTests {

        @Test
        void should_getProjectByName() throws Exception {
            var found = new Project(1L, "TrackLight", "Time tracking");

            given(projectService.findProjectByName("TrackLight")).willReturn(found);

            mvc.perform(get("/api/projects/search")
                            .with(jwt())
                            .param("projectName", "TrackLight"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("TrackLight"));

            then(projectService).should().findProjectByName("TrackLight");
            then(projectService).shouldHaveNoMoreInteractions();
        }

        @Test
        void should_returnNotFound_WhenProjectNotExists() throws Exception {
            given(projectService.findProjectByName("Nope"))
                    .willThrow(new NotFoundException("Project not found"));

            mvc.perform(get("/api/projects/search")
                            .with(jwt())
                            .param("projectName", "Nope"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Project not found"));

            then(projectService).should().findProjectByName("Nope");
            then(projectService).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    class DeleteProjectByIdTests {
        @Test
        void should_deleteProjectById_WhenExists() throws Exception {
            willDoNothing().given(projectService).delete(1L);

            mvc.perform(delete("/api/projects/{projectId}", 1L).with(jwt()))
                    .andExpect(status().isOk());

            then(projectService).should().delete(1L);
            then(projectService).shouldHaveNoMoreInteractions();
        }
    }
}
