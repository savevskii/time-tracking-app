package com.fsavevsk.timetracking.service;

import com.fsavevsk.timetracking.api.dto.CreateTimeEntryRequest;
import com.fsavevsk.timetracking.api.dto.TimeEntryResponse;

import java.util.List;

public interface TimeEntryService {

    List<TimeEntryResponse> listForCurrentUser();
    TimeEntryResponse createForCurrentUser(CreateTimeEntryRequest req);
    void deleteForCurrentUser(Long entryId);

}
