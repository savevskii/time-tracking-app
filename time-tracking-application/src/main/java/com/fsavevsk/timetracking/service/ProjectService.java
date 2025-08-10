package com.fsavevsk.timetracking.service;

import com.fsavevsk.timetracking.api.dto.Project;

import java.util.List;

public interface ProjectService {

    Project createProject(Project project);

    List<Project> getAllProjects();

    Project findProjectByName(String name);

    void delete(Long id);

}
