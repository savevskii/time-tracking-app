package com.fsavevsk.timetracking.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "time_entry")
@Data
public class TimeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private LocalDateTime fromTime;

    private LocalDateTime toTime;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private ProjectEntity projectEntity;

}

