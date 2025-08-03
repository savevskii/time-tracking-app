package com.fsavevsk.timetracking.api.controller;

import com.fsavevsk.timetracking.persistence.entity.ProjectEntity;
import com.fsavevsk.timetracking.persistence.entity.TimeEntry;
import com.fsavevsk.timetracking.persistence.repository.ProjectRepository;
import com.fsavevsk.timetracking.persistence.repository.TimeEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/time-entries")
@RequiredArgsConstructor
@RestController
public class TimeEntryController {

    private final TimeEntryRepository timeEntryRepository;
    private final ProjectRepository projectRepository;

    @GetMapping
    public List<TimeEntry> getAllEntries() {
        return timeEntryRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<TimeEntry> createEntry(@RequestBody TimeEntry entry) {
        ProjectEntity projectEntity = projectRepository.findById(entry.getProjectEntity().getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID"));

        entry.setProjectEntity(projectEntity);
        return ResponseEntity.ok(timeEntryRepository.save(entry));
    }
}
