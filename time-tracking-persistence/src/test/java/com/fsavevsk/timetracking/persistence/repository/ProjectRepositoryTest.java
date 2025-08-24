package com.fsavevsk.timetracking.persistence.repository;

import com.fsavevsk.timetracking.persistence.PostgresTestConfig;
import com.fsavevsk.timetracking.persistence.entity.ProjectEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(PostgresTestConfig.class)
@Transactional
public class ProjectRepositoryTest {

    @Autowired
    ProjectRepository projectRepository;

    @Test
    void should_findProjectByName_whenItExists() {
        // given
        var p = new ProjectEntity();
        p.setName("Demo");
        projectRepository.save(p);

        // when
        Optional<ProjectEntity> found = projectRepository.findProjectByName("Demo");

        // then
        assertTrue(found.isPresent(), "Expected project to be found");
        assertEquals("Demo", found.get().getName());
    }

    @Test
    void should_returnEmpty_whenProjectWithNameDoesNotExist() {
        // when
        Optional<ProjectEntity> found = projectRepository.findProjectByName("NotExisting");

        // then
        assertTrue(found.isEmpty(), "Expected project not to be found");
    }

}
