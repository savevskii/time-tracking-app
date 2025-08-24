package com.fsavevsk.timetracking.integration.base;

import com.fsavevsk.timetracking.api.dto.CreateTimeEntryRequest;
import com.fsavevsk.timetracking.persistence.entity.ProjectEntity;
import com.fsavevsk.timetracking.persistence.entity.TimeEntryEntity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TestFactory {

    public static final String PROJECTS_API_ENDPOINT = "/api/projects";

    public static final String TIME_ENTRIES_API_ENDPOINT = "/api/time-entries";

    public static final String ADMIN_REPORTS_OVERVIEW_ENDPOINT = "/api/admin/reports/overview";
    public static final String ADMIN_REPORTS_PROJECTS_ENDPOINT = "/api/admin/reports/projects";

    public ProjectEntity generateProjectEntity() {
        ProjectEntity projectEntity = new ProjectEntity();
        projectEntity.setName("Test Project");
        projectEntity.setDescription("Test description");
        return projectEntity;
    }

    public TimeEntryEntity generateTimeEntryEntity(ProjectEntity projectEntity) {
        TimeEntryEntity timeEntryEntity = new TimeEntryEntity();
        timeEntryEntity.setProject(projectEntity);
        timeEntryEntity.setTitle("Test Time");
        timeEntryEntity.setDescription("Test description");
        timeEntryEntity.setStartTime(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS));
        timeEntryEntity.setEndTime(LocalDateTime.now().plusHours(3).truncatedTo(ChronoUnit.MICROS));
        timeEntryEntity.setUserId("it-user");
        return timeEntryEntity;
    }

    public CreateTimeEntryRequest generateCreateTimeEntryRequest(Long projectId) {
        return new CreateTimeEntryRequest(
                projectId,
                "Implement MockMvc IT",
                LocalDateTime.now().truncatedTo(ChronoUnit.MICROS),
                LocalDateTime.now().plusHours(3).truncatedTo(ChronoUnit.MICROS),
                "End-to-end controller test"
        );
    }
}
