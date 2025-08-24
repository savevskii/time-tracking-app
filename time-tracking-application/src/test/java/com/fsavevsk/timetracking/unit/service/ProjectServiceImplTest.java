package com.fsavevsk.timetracking.unit.service;

import com.fsavevsk.timetracking.api.dto.CreateProject;
import com.fsavevsk.timetracking.api.dto.Project;
import com.fsavevsk.timetracking.api.exception.NotFoundException;
import com.fsavevsk.timetracking.api.mapper.ProjectMapper;
import com.fsavevsk.timetracking.persistence.entity.ProjectEntity;
import com.fsavevsk.timetracking.persistence.repository.ProjectRepository;
import com.fsavevsk.timetracking.service.impl.ProjectServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    private final ProjectMapper mapper = Mappers.getMapper(ProjectMapper.class);

    private ProjectServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProjectServiceImpl(projectRepository, mapper);
    }

    @Test
    void createProject_mapsAndReturnsSaved() {
        // given
        CreateProject req = new CreateProject("TrackLight", "Time tracking");
        ProjectEntity saved = entity(1L, "TrackLight", "Time tracking");

        given(projectRepository.save(any(ProjectEntity.class))).willReturn(saved);

        // when
        Project out = service.createProject(req);

        // then
        assertThat(out.id()).isEqualTo(1L);
        assertThat(out.name()).isEqualTo("TrackLight");
        assertThat(out.description()).isEqualTo("Time tracking");

        then(projectRepository).should().save(any(ProjectEntity.class));
        then(projectRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void getAllProjects_mapsList() {
        // given
        given(projectRepository.findAll())
                .willReturn(List.of(
                        entity(1L, "TrackLight", "A"),
                        entity(2L, "SolidTime", "B")
                ));

        // when
        List<Project> list = service.getAllProjects();

        // then
        assertThat(list).hasSize(2);
        assertThat(list.get(0).id()).isEqualTo(1L);
        assertThat(list.get(0).name()).isEqualTo("TrackLight");
        assertThat(list.get(1).id()).isEqualTo(2L);
        assertThat(list.get(1).name()).isEqualTo("SolidTime");

        then(projectRepository).should().findAll();
        then(projectRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void findProjectByName_returnsMapped_whenFound() {
        // given
        given(projectRepository.findProjectByName("TrackLight"))
                .willReturn(Optional.of(entity(1L, "TrackLight", "A")));

        // when
        Project p = service.findProjectByName("TrackLight");

        // then
        assertThat(p.id()).isEqualTo(1L);
        assertThat(p.name()).isEqualTo("TrackLight");

        then(projectRepository).should().findProjectByName("TrackLight");
        then(projectRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void findProjectByName_throwsNotFound_whenMissing() {
        // given
        given(projectRepository.findProjectByName("Nope"))
                .willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> service.findProjectByName("Nope"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Project not found");

        then(projectRepository).should().findProjectByName("Nope");
        then(projectRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void delete_deletes_whenFound() {
        // given
        ProjectEntity existing = entity(5L, "X", "Y");
        given(projectRepository.findById(5L)).willReturn(Optional.of(existing));
        willDoNothing().given(projectRepository).delete(existing);

        // when
        service.delete(5L);

        // then
        then(projectRepository).should().findById(5L);
        then(projectRepository).should().delete(existing);
        then(projectRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    void delete_throwsNotFound_whenMissing() {
        // given
        given(projectRepository.findById(999L)).willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> service.delete(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Project not found");

        then(projectRepository).should().findById(999L);
        then(projectRepository).shouldHaveNoMoreInteractions();
    }

    private ProjectEntity entity(Long id, String name, String desc) {
        ProjectEntity e = new ProjectEntity();
        e.setId(id);
        e.setName(name);
        e.setDescription(desc);
        return e;
    }
}