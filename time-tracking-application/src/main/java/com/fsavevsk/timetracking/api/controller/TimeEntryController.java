package com.fsavevsk.timetracking.api.controller;

import com.fsavevsk.timetracking.api.dto.CreateTimeEntryRequest;
import com.fsavevsk.timetracking.api.dto.TimeEntryResponse;
import com.fsavevsk.timetracking.service.TimeEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/time-entries")
@RequiredArgsConstructor
public class TimeEntryController {

    private final TimeEntryService service;

    @GetMapping
    public List<TimeEntryResponse> list() {
        return service.listForCurrentUser();
    }

    @PostMapping
    public ResponseEntity<TimeEntryResponse> create(@Valid @RequestBody CreateTimeEntryRequest req) {
        return ResponseEntity.ok(service.createForCurrentUser(req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteForCurrentUser(id);
        return ResponseEntity.noContent().build();
    }
}
