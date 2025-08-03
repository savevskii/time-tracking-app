package com.fsavevsk.timetracking.api.service;

import com.fsavevsk.timetracking.api.mapper.ProjectMapper;
import com.fsavevsk.timetracking.domain.model.Project;
import com.fsavevsk.timetracking.persistence.entity.ProjectEntity;
import com.fsavevsk.timetracking.persistence.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.BDDMockito.*;
import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @Test
    void shouldReturnAllProjects() {
        // Given
        ProjectEntity entity = new ProjectEntity();
        Project domain = new Project();

        given(projectRepository.findAll()).willReturn(List.of(entity));
        given(projectMapper.toDomain(entity)).willReturn(domain);

        // When
        List<Project> result = projectService.getAllProjects();

        // Then
        then(projectRepository).should().findAll();
        then(projectMapper).should().toDomain(entity);
        assertThat(result).containsExactly(domain);
    }

    @Test
    void shouldCreateProject() {
        // Given
        Project domain = new Project();
        ProjectEntity entity = new ProjectEntity();

        given(projectMapper.toEntity(domain)).willReturn(entity);
        given(projectRepository.save(entity)).willReturn(entity);
        given(projectMapper.toDomain(entity)).willReturn(domain);

        // When
        Project result = projectService.createProject(domain);

        // Then
        then(projectMapper).should().toEntity(domain);
        then(projectRepository).should().save(entity);
        then(projectMapper).should().toDomain(entity);
        assertThat(result).isEqualTo(domain);
    }

    @Test
    void shouldThrowWhenProjectNotFound() {
        // Given
        given(projectRepository.findProjectByName("NotFound")).willReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> projectService.findProjectByName("NotFound"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid project name");

        then(projectRepository).should().findProjectByName("NotFound");
    }
}

