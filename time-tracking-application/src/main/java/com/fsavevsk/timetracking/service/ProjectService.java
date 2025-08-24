package com.fsavevsk.timetracking.service;

import com.fsavevsk.timetracking.api.dto.CreateProject;
import com.fsavevsk.timetracking.api.dto.Project;

import java.util.List;

public interface ProjectService {

    Project createProject(CreateProject project);

    List<Project> getAllProjects();

    Project findProjectByName(String name);

    void delete(Long id);

}
