package com.fsavevsk.timetracking.persistence.repository;

import com.fsavevsk.timetracking.persistence.entity.TimeEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TimeEntryRepository extends JpaRepository<TimeEntryEntity, Long> {

    List<TimeEntryEntity> findByUserIdOrderByStartTimeDesc(String userId);

    Optional<TimeEntryEntity> findByIdAndUserId(Long entryId, String userId);

    @Query("""
      select coalesce(sum(te.durationMinutes), 0)
      from TimeEntryEntity te
      where te.startTime >= :from and te.startTime < :to
    """)
    Integer sumMinutesBetweenAll(LocalDateTime from, LocalDateTime to);

    @Query("""
      select te.project.id, te.project.name, coalesce(sum(te.durationMinutes), 0)
      from TimeEntryEntity te
      where te.startTime >= :from and te.startTime < :to
      group by te.project.id, te.project.name
      order by sum(te.durationMinutes) desc
    """)
    List<Object[]> topProjectsByMinutes(LocalDateTime from, LocalDateTime to);

    @Query("""
      select coalesce(sum(te.durationMinutes), 0)
      from TimeEntryEntity te
      where te.project.id = :projectId
        and te.startTime >= :from and te.startTime < :to
    """)
    Integer sumProjectMinutesBetween(Long projectId, LocalDateTime from, LocalDateTime to);

    @Query("""
      select count(te)
      from TimeEntryEntity te
      where te.project.id = :projectId
        and te.startTime >= :from and te.startTime < :to
    """)
    Long countEntriesForProjectBetween(Long projectId, LocalDateTime from, LocalDateTime to);

    @Query("""
      select max(te.startTime)
      from TimeEntryEntity te
      where te.project.id = :projectId
    """)
    LocalDateTime lastEntryAtForProject(Long projectId);

}

