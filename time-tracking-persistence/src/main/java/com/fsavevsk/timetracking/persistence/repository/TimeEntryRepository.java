package com.fsavevsk.timetracking.persistence.repository;

import com.fsavevsk.timetracking.persistence.entity.TimeEntryEntity;
import com.fsavevsk.timetracking.persistence.projection.ProjectSummaryAggregate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
            select
              p.id as projectId,
              p.name as projectName,
              coalesce(sum(case
                  when te.startTime >= :weekStart and te.startTime < :weekEnd
                  then te.durationMinutes else 0 end), 0) as minutesWeek,
              coalesce(sum(case
                  when te.startTime >= :rangeStart and te.startTime < :rangeEnd
                  then te.durationMinutes else 0 end), 0) as minutesRange,
              coalesce(sum(case
                  when te.startTime >= :weekStart and te.startTime < :weekEnd
                  then 1 else 0 end), 0) as entriesWeek,
              max(case when te.startTime >= :rangeStart and te.startTime < :rangeEnd then te.startTime else null end) as lastEntryAt
            from ProjectEntity p
            left join TimeEntryEntity te on te.project = p
            group by p.id, p.name
            """)
    List<ProjectSummaryAggregate> summarizeAllProjects(
            @Param("weekStart") LocalDateTime weekStart,
            @Param("weekEnd") LocalDateTime weekEnd,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd
    );

}

