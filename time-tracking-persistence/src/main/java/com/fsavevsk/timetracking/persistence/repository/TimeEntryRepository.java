package com.fsavevsk.timetracking.persistence.repository;

import com.fsavevsk.timetracking.persistence.entity.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeEntryRepository extends JpaRepository<TimeEntry, Long> {
}

