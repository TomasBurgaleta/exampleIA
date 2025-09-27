package com.tomasburgaleta.exampleia.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(scanBasePackages = "com.tomasburgaleta.exampleia")
@ConfigurationPropertiesScan("com.tomasburgaleta.exampleia.infrastructure.config")
public class ExampleIAApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleIAApplication.class, args);
    }
}