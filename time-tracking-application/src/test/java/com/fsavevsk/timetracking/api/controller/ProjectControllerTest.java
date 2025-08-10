package com.fsavevsk.timetracking.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fsavevsk.timetracking.api.dto.Project;
import com.fsavevsk.timetracking.api.exception.GlobalExceptionHandler;
import com.fsavevsk.timetracking.api.exception.NotFoundException;
import com.fsavevsk.timetracking.service.ProjectService;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("GET /api/projects")
    class GetAll {
        @Test
        @DisplayName("returns 200 with list of projects")
        void getAll_ok() throws Exception {
            var p1 = sampleProject();
            var p2 = new Project();
            p2.setId(2L);
            p2.setName("SolidTime");
            p2.setDescription("Clone base");

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
    @DisplayName("POST /api/projects")
    class Create {
        @Test
        @DisplayName("returns 200 with created project")
        void create_ok() throws Exception {
            var req = new Project();
            req.setName("TrackLight");
            req.setDescription("Time tracking");

            var created = sampleProject(); // id populated

            given(projectService.createProject(any(Project.class))).willReturn(created);

            mvc.perform(post("/api/projects")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("TrackLight"))
                    .andExpect(jsonPath("$.description").value("Time tracking"));

            then(projectService).should().createProject(any(Project.class));
            then(projectService).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    @DisplayName("GET /api/projects/search?projectName=...")
    class SearchByName {
        @Test
        @DisplayName("returns 200 with project when found")
        void search_ok() throws Exception {
            var found = sampleProject();

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
        @DisplayName("returns 404 when project not found")
        void search_notFound() throws Exception {
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
    @DisplayName("DELETE /api/projects/{projectId}")
    class DeleteById {
        @Test
        @DisplayName("returns 200 when deleted")
        void delete_ok() throws Exception {
            willDoNothing().given(projectService).delete(1L);

            mvc.perform(delete("/api/projects/{projectId}", 1L).with(jwt()))
                    .andExpect(status().isOk());

            then(projectService).should().delete(1L);
            then(projectService).shouldHaveNoMoreInteractions();
        }
    }

    private Project sampleProject() {
        Project p = new Project();
        p.setId(1L);
        p.setName("TrackLight");
        p.setDescription("Time tracking");
        return p;
    }
}
