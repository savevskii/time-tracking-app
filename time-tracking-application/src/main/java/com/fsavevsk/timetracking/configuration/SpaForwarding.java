package com.fsavevsk.timetracking.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SpaForwarding implements WebMvcConfigurer {
    @Override public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/{path:(?!api|actuator|.*\\..*).*$}")
                .setViewName("forward:/index.html");
        registry.addViewController("/**/{path:(?!api|actuator|.*\\..*).*$}")
                .setViewName("forward:/index.html");
    }
}

