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
        /** 匹配到的用户技能名称（语义匹配时可能不同于JD技能名，如JD"数据库"匹配用户"MySQL"） */
        private String matchedUserSkill;
        /** 语义匹配类型：exact/subset/superset/related/none */
        private String matchType;
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