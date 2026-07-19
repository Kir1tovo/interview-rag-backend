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

/**
 * 匹配分析控制器
 * 提供技能匹配分析、分析历史查询、详情查看、删除等接口
 */
@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
public class MatchAnalysisController {

    private final MatchAnalysisService matchAnalysisService;
    private final JobDescriptionMapper jobDescriptionMapper;
    private final ObjectMapper objectMapper;

    /**
     * 执行匹配分析
     * 基于用户技能和指定 JD，调用 DeepSeek 计算匹配度并生成分析报告
     *
     * @param jdId JD ID
     * @return 匹配分析结果（含匹配度分数、技能分类、优先级建议、AI 报告）
     */
    @PostMapping("/analyze/{jdId}")
    public Result<MatchResultDTO> analyze(@PathVariable Long jdId) {
        Long userId = SecurityUtils.getCurrentUserId();
        MatchResultDTO result = matchAnalysisService.analyze(userId, jdId);
        return Result.success(result);
    }

    /**
     * 获取匹配分析详情
     * 查询分析记录并关联 JD 信息，组装为前端展示 DTO
     *
     * @param id 匹配分析记录 ID
     * @return 匹配分析详情（含 JD 公司/职位、技能分类列表、AI 报告）
     */
    @GetMapping("/{id}")
    public Result<MatchHistoryDTO> getDetail(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        MatchAnalysis analysis = matchAnalysisService.getById(userId, id);

        // 关联查询 JD 信息
        JobDescription jd = jobDescriptionMapper.selectById(analysis.getJdId());

        // 解析 JSON 格式的技能列表
        List<String> masteredSkills = parseJsonList(analysis.getMasteredJson());
        List<String> needImproveSkills = parseJsonList(analysis.getNeedImproveJson());
        List<String> notKnownSkills = parseJsonList(analysis.getNotKnownJson());

        // 组装历史详情 DTO
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

    /**
     * 获取当前用户的匹配分析历史列表
     *
     * @return 匹配分析历史列表
     */
    @GetMapping("/list")
    public Result<List<MatchHistoryDTO>> list() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<MatchHistoryDTO> list = matchAnalysisService.listHistoryByUserId(userId);
        return Result.success(list);
    }

    /**
     * 获取指定 JD 的匹配分析历史列表
     *
     * @param jdId JD ID
     * @return 该 JD 关联的匹配分析历史列表
     */
    @GetMapping("/list/{jdId}")
    public Result<List<MatchHistoryDTO>> listByJd(@PathVariable Long jdId) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<MatchHistoryDTO> list = matchAnalysisService.listHistoryByJdId(userId, jdId);
        return Result.success(list);
    }

    /**
     * 删除匹配分析记录
     *
     * @param id 匹配分析记录 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        matchAnalysisService.deleteById(userId, id);
        return Result.success();
    }

    /**
     * 解析 JSON 格式的技能列表字符串
     *
     * @param json JSON 字符串，如 ["Java","Spring"]
     * @return 技能列表，解析失败返回空列表
     */
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