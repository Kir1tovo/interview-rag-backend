package com.rhy.interviewprep.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPlanDTO {

    private Integer totalEstimatedHours;

    private List<Phase> phases;

    private String suggestion;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Phase {
        private Integer phaseNumber;
        private String phaseName;
        private Integer durationHours;
        private String goal;
        private List<String> skills;
        private List<Content> contents;
        private String milestone;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Content {
        private String topic;
        private String description;
        private List<String> resources;
    }
}