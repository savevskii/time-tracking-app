package com.fsavevsk.timetracking.persistence;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan
@ComponentScan
@EnableJpaRepositories
@AutoConfiguration
public class TimeTrackingPersistenceAutoConfiguration {
}
