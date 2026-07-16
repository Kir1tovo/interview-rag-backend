package com.rhy.interviewprep;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.alexcheng1982.springai.dashscope.autoconfigure.DashscopeAutoConfiguration;

@SpringBootApplication(exclude = {DashscopeAutoConfiguration.class})
public class InterviewPrepApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterviewPrepApplication.class, args);
    }

}
