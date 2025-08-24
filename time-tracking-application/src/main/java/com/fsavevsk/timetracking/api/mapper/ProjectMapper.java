package com.fsavevsk.timetracking.api.mapper;

import com.fsavevsk.timetracking.api.dto.CreateProject;
import com.fsavevsk.timetracking.api.dto.Project;
import com.fsavevsk.timetracking.persistence.entity.ProjectEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    Project toDomain(ProjectEntity project);

    ProjectEntity toEntity(CreateProject project);

}
