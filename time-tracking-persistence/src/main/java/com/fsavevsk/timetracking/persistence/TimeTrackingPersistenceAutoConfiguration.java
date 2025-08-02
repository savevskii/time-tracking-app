package com.fsavevsk.timetracking.persistence;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Import(DataSourceAutoConfiguration.class)
@EntityScan
@ComponentScan
@EnableJpaRepositories
@Configuration
public class TimeTrackingPersistenceAutoConfiguration {
}
