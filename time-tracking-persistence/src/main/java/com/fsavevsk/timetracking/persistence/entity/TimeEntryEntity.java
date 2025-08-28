package com.fsavevsk.timetracking.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "time_entries")
@Getter
@Setter
public class TimeEntryEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ProjectEntity project;

    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "title", length = 100)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    public void calculateDuration() {
        if (startTime != null && endTime != null) {
            this.durationMinutes = (int) Duration.between(startTime, endTime).toMinutes();
        }
    }
}

