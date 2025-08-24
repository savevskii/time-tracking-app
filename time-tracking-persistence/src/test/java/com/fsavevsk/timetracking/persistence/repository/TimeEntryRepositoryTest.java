package com.fsavevsk.timetracking.persistence.repository;

import com.fsavevsk.timetracking.persistence.PostgresTestConfig;
import com.fsavevsk.timetracking.persistence.entity.ProjectEntity;
import com.fsavevsk.timetracking.persistence.entity.TimeEntryEntity;
import com.fsavevsk.timetracking.persistence.projection.ProjectSummaryAggregate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(PostgresTestConfig.class)
@Transactional
class TimeEntryRepositoryTest {

    @Autowired
    TimeEntryRepository timeEntryRepository;
    @Autowired
    ProjectRepository projectRepository;

    @Test
    void should_returnEntriesForUser_orderedByStartTimeDesc() {
        var p = project("Acme");

        var u1 = "alice";
        var u2 = "bob";

        var t1 = entry(p, u1, "2025-08-10T10:00:00", 30);
        var t2 = entry(p, u1, "2025-08-10T12:00:00", 15);
        var t3 = entry(p, u2, "2025-08-10T09:00:00", 45); // different user

        var result = timeEntryRepository.findByUserIdOrderByStartTimeDesc(u1);

        assertThat(result).extracting(TimeEntryEntity::getId)
                .containsExactly(t2.getId(), t1.getId()); // DESC
        assertThat(result).doesNotContain(t3);
    }

    @Test
    void should_findEntityOnlyWhen_idAndUserIdMatch() {
        var p = project("Demo");
        var u1 = "alice";
        var u2 = "bob";

        var t = entry(p, u1, "2025-08-10T10:00:00", 30);

        Optional<TimeEntryEntity> ok = timeEntryRepository.findByIdAndUserId(t.getId(), u1);
        Optional<TimeEntryEntity> wrongUser = timeEntryRepository.findByIdAndUserId(t.getId(), u2);
        Optional<TimeEntryEntity> wrongId = timeEntryRepository.findByIdAndUserId(t.getId() + 999, u1);

        assertThat(ok).isPresent();
        assertThat(wrongUser).isEmpty();
        assertThat(wrongId).isEmpty();
    }

    @Test
    void should_sumMinutesBetweenAll_withInclusiveLowerExclusiveUpper() {
        var p = project("SumProj");

        save(p, "2025-08-10T00:00:00", 10);  // boundary IN
        save(p, "2025-08-10T10:00:00", 60);  // inside
        save(p, "2025-08-10T15:00:00", 30);  // inside
        save(p, "2025-08-10T23:59:59", 20);  // inside
        save(p, "2025-08-11T00:00:00", 999); // upper boundary OUT

        var from = LocalDateTime.parse("2025-08-10T00:00:00");
        var to   = LocalDateTime.parse("2025-08-11T00:00:00");

        Integer minutes = timeEntryRepository.sumMinutesBetweenAll(from, to);
        assertThat(minutes).isEqualTo(120); // 10 + 60 + 30 + 20
    }

    @Test
    void should_returnTopProjectsByTotalMinutes_desc() {
        var p1 = project("Alpha");
        var p2 = project("Beta");
        var p3 = project("Gamma");

        // Window [10th, 11th)
        rngSave(p1, "2025-08-10T09:00:00", 30);
        rngSave(p1, "2025-08-10T12:00:00", 45);
        rngSave(p2, "2025-08-10T13:00:00", 90);
        rngSave(p3, "2025-08-09T23:59:59", 10); // outside (lower)
        rngSave(p3, "2025-08-11T00:00:00", 10); // outside (upper/exclusive)

        var from = LocalDateTime.parse("2025-08-10T00:00:00");
        var to   = LocalDateTime.parse("2025-08-11T00:00:00");

        List<Object[]> rows = timeEntryRepository.topProjectsByMinutes(from, to);

        // Expect Beta (90), Alpha (75). Gamma absent (0 mins and not produced by INNER grouping).
        assertThat(rows).hasSize(2);

        // row: [projectId, projectName, totalMinutes]
        assertThat(rows.get(0)[1]).isEqualTo("Beta");
        assertThat(rows.get(0)[2]).isEqualTo(90L);

        assertThat(rows.get(1)[1]).isEqualTo("Alpha");
        assertThat(rows.get(1)[2]).isEqualTo(75L);
    }

    @Test
    void should_summarizeAllProjects_withinWindows_andReturnLeftJoin() {
        var p1 = project("Alpha");
        var p2 = project("Beta");
        var p3 = project("Empty"); // will have no entries

        var weekStart  = LocalDateTime.parse("2025-08-11T00:00:00"); // Monday
        var weekEnd    = LocalDateTime.parse("2025-08-18T00:00:00"); // next Monday (exclusive)
        var rangeStart = LocalDateTime.parse("2025-08-01T00:00:00");
        var rangeEnd   = LocalDateTime.parse("2025-09-01T00:00:00");

        // Alpha: 2 entries within week + 1 outside week but inside range
        var a1 = save(p1, "2025-08-12T10:00:00", 60);
        var a2 = save(p1, "2025-08-15T15:00:00", 30);
        save(p1, "2025-08-02T12:00:00", 45); // in range, outside week

        // Beta: 1 entry in week, 1 outside range
        var b1 = save(p2, "2025-08-13T09:00:00", 25);
        save(p2, "2025-09-02T09:00:00", 999); // outside range

        // p3: no entries

        List<ProjectSummaryAggregate> rows = timeEntryRepository.summarizeAllProjects(
                weekStart, weekEnd, rangeStart, rangeEnd
        );

        // should include ALL projects (left join)
        assertThat(rows).extracting(ProjectSummaryAggregate::getProjectName)
                .containsExactlyInAnyOrder("Alpha", "Beta", "Empty");

        var alpha = rows.stream().filter(r -> r.getProjectName().equals("Alpha")).findFirst().orElseThrow();
        var beta  = rows.stream().filter(r -> r.getProjectName().equals("Beta")).findFirst().orElseThrow();
        var empty = rows.stream().filter(r -> r.getProjectName().equals("Empty")).findFirst().orElseThrow();

        // Alpha assertions
        assertThat(alpha.getMinutesWeek()).isEqualTo(60 + 30);
        assertThat(alpha.getEntriesWeek()).isEqualTo(2);
        assertThat(alpha.getMinutesRange()).isEqualTo(60 + 30 + 45);
        assertThat(alpha.getLastEntryAt()).isEqualTo(LocalDateTime.parse("2025-08-15T15:00:00"));

        // Beta assertions
        assertThat(beta.getMinutesWeek()).isEqualTo(25);
        assertThat(beta.getEntriesWeek()).isEqualTo(1);
        assertThat(beta.getMinutesRange()).isEqualTo(25);
        assertThat(beta.getLastEntryAt()).isEqualTo(LocalDateTime.parse("2025-08-13T09:00:00"));

        // Empty project: zeros/null lastEntryAt
        assertThat(empty.getMinutesWeek()).isEqualTo(0);
        assertThat(empty.getEntriesWeek()).isEqualTo(0);
        assertThat(empty.getMinutesRange()).isEqualTo(0);
        assertThat(empty.getLastEntryAt()).isNull();
    }

    // ---------- helpers ----------

    private ProjectEntity project(String name) {
        var p = new ProjectEntity();
        p.setName(name);
        return projectRepository.save(p);
    }

    private TimeEntryEntity entry(ProjectEntity p, String userId, String startIso, int minutes) {
        var te = new TimeEntryEntity();
        te.setProject(p);
        te.setUserId(userId);
        te.setTitle("work");
        te.setStartTime(LocalDateTime.parse(startIso));
        te.setEndTime(LocalDateTime.parse(startIso).plusMinutes(minutes));
        te.setDurationMinutes(minutes);
        return timeEntryRepository.save(te);
    }

    private TimeEntryEntity save(ProjectEntity p, String startIso, int minutes) {
        return entry(p, "alice", startIso, minutes);
    }

    private void rngSave(ProjectEntity p, String startIso, int minutes) {
        save(p, startIso, minutes);
    }
}
