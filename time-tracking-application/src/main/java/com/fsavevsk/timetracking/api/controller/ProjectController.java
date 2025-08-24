package com.fsavevsk.timetracking.api.controller;

import com.fsavevsk.timetracking.api.dto.CreateProject;
import com.fsavevsk.timetracking.api.dto.Project;
import com.fsavevsk.timetracking.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/projects")
@RequiredArgsConstructor
@RestController
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        List<Project> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@Valid @RequestBody CreateProject project) {
        Project createdProject = projectService.createProject(project);
        return ResponseEntity.ok(createdProject);
    }

    @GetMapping("/search")
    public ResponseEntity<Project> getProjectByName(@RequestParam String projectName) {
        Project project = projectService.findProjectByName(projectName);
        return ResponseEntity.ok(project);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProjectByName(@PathVariable Long projectId) {
        projectService.delete(projectId);
        return ResponseEntity.ok().build();
    }
}
