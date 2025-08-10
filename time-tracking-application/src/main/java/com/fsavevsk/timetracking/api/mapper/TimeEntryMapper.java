package com.fsavevsk.timetracking.api.mapper;

import com.fsavevsk.timetracking.api.dto.CreateTimeEntryRequest;
import com.fsavevsk.timetracking.api.dto.TimeEntryResponse;
import com.fsavevsk.timetracking.persistence.entity.ProjectEntity;
import com.fsavevsk.timetracking.persistence.entity.TimeEntryEntity;
import org.mapstruct.*;

import java.time.Duration;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TimeEntryMapper {

    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "projectName", source = "project.name")
    TimeEntryResponse toResponse(TimeEntryEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "durationMinutes", ignore = true)
    TimeEntryEntity toEntity(CreateTimeEntryRequest req, @Context String userId, @Context ProjectEntity project);

    @AfterMapping
    default void afterCreate(
            @MappingTarget TimeEntryEntity entity,
            CreateTimeEntryRequest req,
            @Context String userId,
            @Context ProjectEntity project
    ) {
        entity.setUserId(userId);
        entity.setProject(project);
        entity.calculateDuration();
    }
}
