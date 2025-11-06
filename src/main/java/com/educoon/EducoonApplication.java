package com.educoon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class EducoonApplication {

    public static void main(String[] args) {
        SpringApplication.run(EducoonApplication.class, args);
    }

}
