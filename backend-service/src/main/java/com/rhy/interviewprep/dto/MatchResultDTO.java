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
public class MatchResultDTO {

    private Double totalScore;

    private Double techScore;

    private Double softSkillScore;

    private List<SkillMatchDetail> techMatches;

    private List<SkillMatchDetail> softSkillMatches;

    private List<String> masteredSkills;

    private List<String> needImproveSkills;

    private List<String> notKnownSkills;

    private List<PriorityItem> priorityItems;

    private String analysisReport;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillMatchDetail {
        private String skillName;
        private String jdLevel;
        private String userLevel;
        private Integer score;
        private String matchStatus;
        private Boolean isRequired;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriorityItem {
        private String skillName;
        private Integer priority;
        private String reason;
    }
}