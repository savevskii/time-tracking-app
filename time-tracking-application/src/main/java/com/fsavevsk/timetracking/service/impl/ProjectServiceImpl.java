package com.fsavevsk.timetracking.service.impl;

import com.fsavevsk.timetracking.api.exception.NotFoundException;
import com.fsavevsk.timetracking.api.mapper.ProjectMapper;
import com.fsavevsk.timetracking.api.dto.Project;
import com.fsavevsk.timetracking.service.ProjectService;
import com.fsavevsk.timetracking.persistence.entity.ProjectEntity;
import com.fsavevsk.timetracking.persistence.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    @Override
    public Project createProject(Project project) {
        ProjectEntity saved = projectRepository.save(projectMapper.toEntity(project));
        return projectMapper.toDomain(saved);
    }

    @Override
    public List<Project> getAllProjects() {
        return projectRepository.findAll()
                .stream()
                .map(projectMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Project findProjectByName(String name) {
        ProjectEntity project = projectRepository.findProjectByName(name)
                .orElseThrow(() -> new NotFoundException("Project with name " + name + " not found"));
        return projectMapper.toDomain(project);
    }

    @Override
    public void delete(Long id) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Project not found"));
        projectRepository.delete(project);
    }
}
