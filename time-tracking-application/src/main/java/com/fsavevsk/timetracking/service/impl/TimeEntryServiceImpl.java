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
import com.fsavevsk.timetracking.service.TimeEntryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TimeEntryServiceImpl implements TimeEntryService {

    private final TimeEntryRepository timeEntryRepo;
    private final ProjectRepository projectRepo;
    private final TimeEntryMapper mapper;
    private final CurrentUserService currentUser;

    @Override
    public List<TimeEntryResponse> listForCurrentUser() {
        String userId = currentUser.userId();
        return timeEntryRepo.findByUserIdOrderByStartTimeDesc(userId)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    public TimeEntryResponse createForCurrentUser(CreateTimeEntryRequest req) {
        if (!req.endTime().isAfter(req.startTime())) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }
        String userId = currentUser.userId();
        ProjectEntity project = projectRepo.findById(req.projectId())
                .orElseThrow(() -> new NotFoundException("Project not found"));

        TimeEntryEntity entity = mapper.toEntity(req, userId, project);
        TimeEntryEntity saved  = timeEntryRepo.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    public void deleteForCurrentUser(Long entryId) {
        String userId = currentUser.userId();
        TimeEntryEntity e = timeEntryRepo.findByIdAndUserId(entryId, userId)
                .orElseThrow(() -> new NotFoundException("Time entry not found"));

        timeEntryRepo.delete(e);
    }
}
