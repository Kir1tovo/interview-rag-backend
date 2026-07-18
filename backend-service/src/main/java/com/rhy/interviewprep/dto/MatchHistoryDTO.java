package com.rhy.interviewprep.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchHistoryDTO {

    private Long id;

    private Long jdId;

    private String jdCompany;

    private String jdPosition;

    private Double totalScore;

    private Double techScore;

    private Double softSkillScore;

    private List<String> masteredSkills;

    private List<String> needImproveSkills;

    private List<String> notKnownSkills;

    private String analysisReport;

    private LocalDateTime createdAt;
}