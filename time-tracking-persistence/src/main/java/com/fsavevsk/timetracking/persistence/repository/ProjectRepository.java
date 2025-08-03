package com.fsavevsk.timetracking.persistence.repository;

import com.fsavevsk.timetracking.persistence.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

    Optional<ProjectEntity> findProjectByName(String name);

}
