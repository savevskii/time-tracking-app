package com.fsavevsk.timetracking.persistence.projection;

import java.time.LocalDateTime;

public interface ProjectSummaryAggregate {
    Long getProjectId();
    String getProjectName();
    Integer getMinutesWeek();
    Integer getMinutesRange();
    Long getEntriesWeek();
    LocalDateTime getLastEntryAt();
}

