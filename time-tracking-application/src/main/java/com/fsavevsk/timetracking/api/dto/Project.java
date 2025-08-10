package com.fsavevsk.timetracking.api.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Project {

    private Long id;
    private String name;
    private String description;

}
