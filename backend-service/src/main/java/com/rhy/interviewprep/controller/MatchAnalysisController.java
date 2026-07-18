package com.rhy.interviewprep.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhy.interviewprep.common.Result;
import com.rhy.interviewprep.dto.MatchHistoryDTO;
import com.rhy.interviewprep.dto.MatchResultDTO;
import com.rhy.interviewprep.entity.JobDescription;
import com.rhy.interviewprep.entity.MatchAnalysis;
import com.rhy.interviewprep.mapper.JobDescriptionMapper;
import com.rhy.interviewprep.service.MatchAnalysisService;
import com.rhy.interviewprep.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
public class MatchAnalysisController {

    private final MatchAnalysisService matchAnalysisService;
    private final JobDescriptionMapper jobDescriptionMapper;
    private final ObjectMapper objectMapper;

    @PostMapping("/analyze/{jdId}")
    public Result<MatchResultDTO> analyze(@PathVariable Long jdId) {
        Long userId = SecurityUtils.getCurrentUserId();
        MatchResultDTO result = matchAnalysisService.analyze(userId, jdId);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    public Result<MatchHistoryDTO> getDetail(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        MatchAnalysis analysis = matchAnalysisService.getById(userId, id);
        
        JobDescription jd = jobDescriptionMapper.selectById(analysis.getJdId());
        List<String> masteredSkills = parseJsonList(analysis.getMasteredJson());
        List<String> needImproveSkills = parseJsonList(analysis.getNeedImproveJson());
        List<String> notKnownSkills = parseJsonList(analysis.getNotKnownJson());

        MatchHistoryDTO dto = MatchHistoryDTO.builder()
                .id(analysis.getId())
                .jdId(analysis.getJdId())
                .jdCompany(jd != null ? jd.getCompany() : "")
                .jdPosition(jd != null ? jd.getPosition() : "")
                .totalScore(analysis.getMatchScore())
                .techScore(analysis.getJdMatchScore())
                .softSkillScore(analysis.getSoftSkillScore())
                .masteredSkills(masteredSkills)
                .needImproveSkills(needImproveSkills)
                .notKnownSkills(notKnownSkills)
                .analysisReport(analysis.getAnalysisReport())
                .createdAt(analysis.getCreatedAt())
                .build();
        
        return Result.success(dto);
    }

    @GetMapping("/list")
    public Result<List<MatchHistoryDTO>> list() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<MatchHistoryDTO> list = matchAnalysisService.listHistoryByUserId(userId);
        return Result.success(list);
    }

    @GetMapping("/list/{jdId}")
    public Result<List<MatchHistoryDTO>> listByJd(@PathVariable Long jdId) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<MatchHistoryDTO> list = matchAnalysisService.listHistoryByJdId(userId, jdId);
        return Result.success(list);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        matchAnalysisService.deleteById(userId, id);
        return Result.success();
    }

    private List<String> parseJsonList(String json) {
        if (json == null) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
}