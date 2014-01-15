package com.jayway.serviceregistry.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.EnableHypermediaSupport;

import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.HAL;

@Configuration
@ComponentScan("com.jayway.serviceregistry")
@EnableHypermediaSupport(type = HAL)
@EnableAutoConfiguration
public class ServiceRegistryStart extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ServiceRegistryStart.class);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        application.sources(getClass());
        return application;
    }
}