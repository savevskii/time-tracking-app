package com.fsavevsk.timetracking.domain.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Project {

    private Long id;
    private String name;
    private String description;

}
