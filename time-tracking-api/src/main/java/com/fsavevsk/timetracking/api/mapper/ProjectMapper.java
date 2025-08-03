package com.fsavevsk.timetracking.api.mapper;

import com.fsavevsk.timetracking.domain.model.Project;
import com.fsavevsk.timetracking.persistence.entity.ProjectEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    Project toDomain(ProjectEntity project);
    ProjectEntity toEntity(Project project);
}
