package com.fsavevsk.timetracking.domain.service;

import com.fsavevsk.timetracking.domain.model.Project;

import java.util.List;

public interface ProjectService {

    Project createProject(Project project);

    List<Project> getAllProjects();

    Project findProjectByName(String name);

    void delete(Long id);

}
