package com.fsavevsk.timetracking.service.impl;

import com.fsavevsk.timetracking.api.dto.CreateTimeEntryRequest;
import com.fsavevsk.timetracking.api.dto.TimeEntryResponse;
import com.fsavevsk.timetracking.api.exception.NotFoundException;
import com.fsavevsk.timetracking.api.mapper.TimeEntryMapper;
import com.fsavevsk.timetracking.persistence.entity.ProjectEntity;
import com.fsavevsk.timetracking.persistence.entity.TimeEntryEntity;
import com.fsavevsk.timetracking.persistence.repository.ProjectRepository;
import com.fsavevsk.timetracking.persistence.repository.TimeEntryRepository;
import com.fsavevsk.timetracking.security.CurrentUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class TimeEntryServiceImplTest {

    @Mock
    private TimeEntryRepository timeEntryRepo;
    @Mock
    private ProjectRepository projectRepo;
    @Mock
    private CurrentUserService currentUser;

    private final TimeEntryMapper mapper = Mappers.getMapper(TimeEntryMapper.class);

    private TimeEntryServiceImpl service;

    @BeforeEach
    void init() {
        service = new TimeEntryServiceImpl(timeEntryRepo, projectRepo, mapper, currentUser);
    }

    @Test
    void createForCurrentUser_ok_setsUserProjectAndDuration() {
        // given
        given(currentUser.userId()).willReturn("user-123");

        ProjectEntity project = new ProjectEntity();
        project.setId(10L);
        project.setName("Project A");
        given(projectRepo.findById(10L)).willReturn(Optional.of(project));

        CreateTimeEntryRequest req = new CreateTimeEntryRequest(
                10L, "Feature work",
                LocalDateTime.of(2025, 8, 10, 9, 0),
                LocalDateTime.of(2025, 8, 10, 12, 30),
                "Refactor module"
        );

        willAnswer(inv -> {
            TimeEntryEntity e = inv.getArgument(0);
            Field f = TimeEntryEntity.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(e, 1L); // simulate JPA id assignment
            return e;
        }).given(timeEntryRepo).save(any(TimeEntryEntity.class));

        // when
        TimeEntryResponse res = service.createForCurrentUser(req);

        // then
        assertThat(res.id()).isEqualTo(1L);
        assertThat(res.projectId()).isEqualTo(10L);
        assertThat(res.projectName()).isEqualTo("Project A");
        assertThat(res.title()).isEqualTo("Feature work");
        assertThat(res.durationMinutes()).isEqualTo(210);

        ArgumentCaptor<TimeEntryEntity> captor = ArgumentCaptor.forClass(TimeEntryEntity.class);
        then(timeEntryRepo).should().save(captor.capture());
        TimeEntryEntity saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo("user-123");
        assertThat(saved.getProject()).isSameAs(project);
        assertThat(saved.getDurationMinutes()).isEqualTo(210);
        then(projectRepo).should().findById(10L);
        then(currentUser).should().userId();
        then(timeEntryRepo).shouldHaveNoMoreInteractions();
    }

    @Test
    void createForCurrentUser_throwsWhenEndBeforeStart() {
        // given
        CreateTimeEntryRequest req = new CreateTimeEntryRequest(
                10L, "Bad times",
                LocalDateTime.of(2025, 8, 10, 12, 0),
                LocalDateTime.of(2025, 8, 10, 9, 0),
                null
        );

        // when / then
        assertThatThrownBy(() -> service.createForCurrentUser(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("endTime must be after startTime");

        then(projectRepo).shouldHaveNoInteractions();
        then(timeEntryRepo).shouldHaveNoInteractions();
        then(currentUser).shouldHaveNoInteractions();
    }

    @Test
    void createForCurrentUser_throwsWhenProjectMissing() {
        // given
        given(currentUser.userId()).willReturn("user-123");
        given(projectRepo.findById(999L)).willReturn(Optional.empty());

        CreateTimeEntryRequest req = new CreateTimeEntryRequest(
                999L, "Feature",
                LocalDateTime.of(2025, 8, 10, 9, 0),
                LocalDateTime.of(2025, 8, 10, 10, 0),
                null
        );

        // when / then
        assertThatThrownBy(() -> service.createForCurrentUser(req))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Project not found");

        then(projectRepo).should().findById(999L);
        then(timeEntryRepo).shouldHaveNoInteractions();
    }

    @Test
    void listForCurrentUser_mapsEntities() {
        // given
        given(currentUser.userId()).willReturn("user-123");

        ProjectEntity project = new ProjectEntity();
        project.setId(10L);
        project.setName("Project A");

        TimeEntryEntity e = new TimeEntryEntity();
        e.setProject(project);
        e.setUserId("user-123");
        e.setTitle("Docs");
        e.setStartTime(LocalDateTime.of(2025, 8, 10, 9, 0));
        e.setEndTime(LocalDateTime.of(2025, 8, 10, 10, 15));
        e.setDurationMinutes(75);

        given(timeEntryRepo.findByUserIdOrderByStartTimeDesc("user-123"))
                .willReturn(List.of(e));

        // when
        var list = service.listForCurrentUser();

        // then
        assertThat(list).hasSize(1);
        assertThat(list.get(0).projectId()).isEqualTo(10L);
        assertThat(list.get(0).projectName()).isEqualTo("Project A");
        assertThat(list.get(0).title()).isEqualTo("Docs");
        assertThat(list.get(0).durationMinutes()).isEqualTo(75);

        then(currentUser).should().userId();
        then(timeEntryRepo).should().findByUserIdOrderByStartTimeDesc("user-123");
        then(timeEntryRepo).shouldHaveNoMoreInteractions();
    }

    @Test
    void deleteForCurrentUser_deletesWhenOwner() {
        // given
        given(currentUser.userId()).willReturn("user-123");

        TimeEntryEntity e = new TimeEntryEntity();
        e.setUserId("user-123");

        given(timeEntryRepo.findByIdAndUserId(1L, "user-123"))
                .willReturn(Optional.of(e));

        // when
        service.deleteForCurrentUser(1L);

        // then
        then(timeEntryRepo).should().findByIdAndUserId(1L, "user-123");
        then(timeEntryRepo).should().delete(e);
        then(timeEntryRepo).shouldHaveNoMoreInteractions();
    }

    @Test
    void deleteForCurrentUser_404WhenNotFound() {
        // given
        given(currentUser.userId()).willReturn("user-123");
        given(timeEntryRepo.findByIdAndUserId(999L, "user-123"))
                .willReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> service.deleteForCurrentUser(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Time entry not found");

        then(timeEntryRepo).should().findByIdAndUserId(999L, "user-123");
        then(timeEntryRepo).shouldHaveNoMoreInteractions();
    }
}