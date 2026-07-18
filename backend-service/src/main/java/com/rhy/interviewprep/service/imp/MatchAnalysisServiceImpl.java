package com.rhy.interviewprep.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhy.interviewprep.common.BusinessException;
import com.rhy.interviewprep.common.ErrorCode;
import com.rhy.interviewprep.constants.MatchAnalysisPrompt;
import com.rhy.interviewprep.dto.MatchHistoryDTO;
import com.rhy.interviewprep.dto.MatchResultDTO;
import com.rhy.interviewprep.entity.JobDescription;
import com.rhy.interviewprep.entity.MatchAnalysis;
import com.rhy.interviewprep.entity.UserSkill;
import com.rhy.interviewprep.mapper.JobDescriptionMapper;
import com.rhy.interviewprep.mapper.MatchAnalysisMapper;
import com.rhy.interviewprep.mapper.UserSkillMapper;
import com.rhy.interviewprep.service.MatchAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchAnalysisServiceImpl implements MatchAnalysisService {

    private final UserSkillMapper userSkillMapper;
    private final JobDescriptionMapper jobDescriptionMapper;
    private final MatchAnalysisMapper matchAnalysisMapper;
    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    private static final Map<Integer, String> LEVEL_MAP = Map.of(
            1, "了解",
            2, "熟悉",
            3, "精通"
    );

    @Override
    public MatchResultDTO analyze(Long userId, Long jdId) {
        JobDescription jd = jobDescriptionMapper.selectById(jdId);
        if (jd == null) {
            throw new BusinessException(ErrorCode.JD_NOT_FOUND);
        }

        List<UserSkill> userSkills = userSkillMapper.selectList(
                new LambdaQueryWrapper<UserSkill>()
                        .eq(UserSkill::getUserId, userId)
        );

        Map<String, UserSkill> userSkillMap = userSkills.stream()
                .collect(Collectors.toMap(
                        s -> s.getSkillName().toLowerCase(),
                        s -> s,
                        (existing, replacement) -> existing
                ));

        JdRequirements jdRequirements = parseJdRequirements(jd);

        List<MatchResultDTO.SkillMatchDetail> techMatches = matchTechSkills(userSkillMap, jdRequirements.techRequired, jdRequirements.techPreferred);
        List<MatchResultDTO.SkillMatchDetail> softMatches = matchSoftSkills(userSkillMap, jdRequirements.softRequired, jdRequirements.softPreferred);

        double techScore = calculateScore(techMatches);
        double softScore = calculateScore(softMatches);
        double totalScore = techScore * 0.7 + softScore * 0.3;

        List<String> masteredSkills = techMatches.stream()
                .filter(m -> m.getScore() == 100)
                .map(MatchResultDTO.SkillMatchDetail::getSkillName)
                .collect(Collectors.toList());

        List<String> needImproveSkills = techMatches.stream()
                .filter(m -> m.getScore() > 0 && m.getScore() < 100)
                .map(MatchResultDTO.SkillMatchDetail::getSkillName)
                .collect(Collectors.toList());

        List<String> notKnownSkills = techMatches.stream()
                .filter(m -> m.getScore() == 0)
                .map(MatchResultDTO.SkillMatchDetail::getSkillName)
                .collect(Collectors.toList());

        List<MatchResultDTO.PriorityItem> priorityItems = generatePrioritySuggestions(notKnownSkills, needImproveSkills, jdRequirements.techRequired);

        String analysisReport = generateAnalysisReport(jd, userSkills, techMatches, softMatches, techScore, softScore, totalScore, masteredSkills, needImproveSkills, notKnownSkills);

        MatchResultDTO result = MatchResultDTO.builder()
                .totalScore(Math.round(totalScore * 100.0) / 100.0)
                .techScore(Math.round(techScore * 100.0) / 100.0)
                .softSkillScore(Math.round(softScore * 100.0) / 100.0)
                .techMatches(techMatches)
                .softSkillMatches(softMatches)
                .masteredSkills(masteredSkills)
                .needImproveSkills(needImproveSkills)
                .notKnownSkills(notKnownSkills)
                .priorityItems(priorityItems)
                .analysisReport(analysisReport)
                .build();

        saveAnalysis(userId, jdId, result);

        log.info("匹配分析完成，用户: {}, JD: {}, 总体匹配度: {:.1f}%", userId, jdId, totalScore);

        return result;
    }

    private JdRequirements parseJdRequirements(JobDescription jd) {
        JdRequirements req = new JdRequirements();

        try {
            if (jd.getRequirementsJson() != null) {
                JsonNode root = objectMapper.readTree(jd.getRequirementsJson());
                req.techRequired = parseStringList(root, "required");
                req.techPreferred = parseStringList(root, "preferred");
            }
            if (jd.getSoftSkillsJson() != null) {
                JsonNode root = objectMapper.readTree(jd.getSoftSkillsJson());
                req.softRequired = parseStringList(root, "required");
                req.softPreferred = parseStringList(root, "preferred");
            }
        } catch (JsonProcessingException e) {
            log.warn("解析 JD 要求 JSON 失败", e);
        }

        return req;
    }

    private List<String> parseStringList(JsonNode root, String fieldName) {
        List<String> list = new ArrayList<>();
        JsonNode node = root.get(fieldName);
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                list.add(item.asText());
            }
        }
        return list;
    }

    private List<MatchResultDTO.SkillMatchDetail> matchTechSkills(Map<String, UserSkill> userSkillMap,
                                                                  List<String> required,
                                                                  List<String> preferred) {
        List<MatchResultDTO.SkillMatchDetail> matches = new ArrayList<>();

        for (String skill : required) {
            matches.add(createMatchDetail(skill, true, userSkillMap));
        }
        for (String skill : preferred) {
            matches.add(createMatchDetail(skill, false, userSkillMap));
        }

        return matches;
    }

    private List<MatchResultDTO.SkillMatchDetail> matchSoftSkills(Map<String, UserSkill> userSkillMap,
                                                                   List<String> required,
                                                                   List<String> preferred) {
        List<MatchResultDTO.SkillMatchDetail> matches = new ArrayList<>();

        for (String skill : required) {
            boolean hasSkill = userSkillMap.containsKey(skill.toLowerCase());
            matches.add(MatchResultDTO.SkillMatchDetail.builder()
                    .skillName(skill)
                    .jdLevel("了解")
                    .userLevel(hasSkill ? "具备" : "不具备")
                    .score(hasSkill ? 100 : 0)
                    .matchStatus(hasSkill ? "已掌握" : "完全不会")
                    .isRequired(true)
                    .build());
        }
        for (String skill : preferred) {
            boolean hasSkill = userSkillMap.containsKey(skill.toLowerCase());
            matches.add(MatchResultDTO.SkillMatchDetail.builder()
                    .skillName(skill)
                    .jdLevel("了解")
                    .userLevel(hasSkill ? "具备" : "不具备")
                    .score(hasSkill ? 100 : 0)
                    .matchStatus(hasSkill ? "已掌握" : "完全不会")
                    .isRequired(false)
                    .build());
        }

        return matches;
    }

    private MatchResultDTO.SkillMatchDetail createMatchDetail(String skillName, boolean isRequired, Map<String, UserSkill> userSkillMap) {
        UserSkill userSkill = userSkillMap.get(skillName.toLowerCase());
        String jdLevel = isRequired ? "熟悉" : "了解";
        int jdLevelRank = isRequired ? 2 : 1;

        String userLevel;
        int score;
        String matchStatus;

        if (userSkill != null) {
            int userLevelRank = userSkill.getLevel();
            userLevel = LEVEL_MAP.getOrDefault(userLevelRank, "未知");

            if (userLevelRank >= jdLevelRank) {
                score = 100;
                matchStatus = "已掌握";
            } else {
                score = 60;
                matchStatus = "需要加强";
            }
        } else {
            userLevel = "无";
            score = 0;
            matchStatus = "完全不会";
        }

        return MatchResultDTO.SkillMatchDetail.builder()
                .skillName(skillName)
                .jdLevel(jdLevel)
                .userLevel(userLevel)
                .score(score)
                .matchStatus(matchStatus)
                .isRequired(isRequired)
                .build();
    }

    private double calculateScore(List<MatchResultDTO.SkillMatchDetail> matches) {
        if (matches.isEmpty()) {
            return 0.0;
        }

        double sum = 0.0;
        double totalWeight = 0.0;

        for (MatchResultDTO.SkillMatchDetail match : matches) {
            double weight = match.getIsRequired() ? 1.0 : 0.5;
            sum += match.getScore() * weight;
            totalWeight += weight;
        }

        return totalWeight > 0 ? sum / totalWeight : 0.0;
    }

    private List<MatchResultDTO.PriorityItem> generatePrioritySuggestions(List<String> notKnownSkills,
                                                                           List<String> needImproveSkills,
                                                                           List<String> requiredSkills) {
        List<MatchResultDTO.PriorityItem> items = new ArrayList<>();
        int priority = 1;

        for (String skill : notKnownSkills) {
            boolean isRequired = requiredSkills.contains(skill);
            items.add(MatchResultDTO.PriorityItem.builder()
                    .skillName(skill)
                    .priority(priority++)
                    .reason(isRequired ? "岗位必须掌握，建议优先学习" : "岗位加分技能，建议学习")
                    .build());
        }

        for (String skill : needImproveSkills) {
            items.add(MatchResultDTO.PriorityItem.builder()
                    .skillName(skill)
                    .priority(priority++)
                    .reason("当前掌握程度不足，需要加强")
                    .build());
        }

        return items;
    }

    private String generateAnalysisReport(JobDescription jd,
                                          List<UserSkill> userSkills,
                                          List<MatchResultDTO.SkillMatchDetail> techMatches,
                                          List<MatchResultDTO.SkillMatchDetail> softMatches,
                                          double techScore,
                                          double softScore,
                                          double totalScore,
                                          List<String> masteredSkills,
                                          List<String> needImproveSkills,
                                          List<String> notKnownSkills) {
        try {
            String userSkillsStr = userSkills.stream()
                    .map(s -> s.getSkillName() + "(" + LEVEL_MAP.getOrDefault(s.getLevel(), "未知") + ")")
                    .collect(Collectors.joining(", "));

            String userPrompt = String.format(
                    MatchAnalysisPrompt.USER_PROMPT_TEMPLATE,
                    jd.getCompany(),
                    jd.getPosition(),
                    techMatches.stream().filter(MatchResultDTO.SkillMatchDetail::getIsRequired).map(MatchResultDTO.SkillMatchDetail::getSkillName).collect(Collectors.joining(", ")),
                    techMatches.stream().filter(m -> !m.getIsRequired()).map(MatchResultDTO.SkillMatchDetail::getSkillName).collect(Collectors.joining(", ")),
                    softMatches.stream().filter(MatchResultDTO.SkillMatchDetail::getIsRequired).map(MatchResultDTO.SkillMatchDetail::getSkillName).collect(Collectors.joining(", ")),
                    softMatches.stream().filter(m -> !m.getIsRequired()).map(MatchResultDTO.SkillMatchDetail::getSkillName).collect(Collectors.joining(", ")),
                    userSkillsStr,
                    techScore,
                    softScore,
                    totalScore,
                    String.join(", ", masteredSkills),
                    String.join(", ", needImproveSkills),
                    String.join(", ", notKnownSkills)
            );

            List<Message> messages = List.of(
                    new SystemMessage(MatchAnalysisPrompt.SYSTEM_PROMPT),
                    new UserMessage(userPrompt)
            );

            Prompt prompt = new Prompt(messages);
            ChatResponse chatResponse = chatModel.call(prompt);

            String response = chatResponse.getResult().getOutput().getText();
            String json = response.trim();

            if (json.startsWith("```json")) {
                json = json.substring(7);
            } else if (json.startsWith("```")) {
                json = json.substring(3);
            }
            if (json.endsWith("```")) {
                json = json.substring(0, json.length() - 3);
            }
            json = json.trim();

            JsonNode root = objectMapper.readTree(json);
            return root.has("analysis") ? root.get("analysis").asText() : "分析报告生成失败";

        } catch (Exception e) {
            log.error("生成 AI 分析报告失败", e);
            return "分析报告生成失败: " + e.getMessage();
        }
    }

    private void saveAnalysis(Long userId, Long jdId, MatchResultDTO result) {
        MatchAnalysis analysis = new MatchAnalysis();
        analysis.setUserId(userId);
        analysis.setJdId(jdId);
        analysis.setJdMatchScore(result.getTechScore());
        analysis.setSoftSkillScore(result.getSoftSkillScore());
        analysis.setMatchScore(result.getTotalScore());

        try {
            analysis.setMasteredJson(objectMapper.writeValueAsString(result.getMasteredSkills()));
            analysis.setNeedImproveJson(objectMapper.writeValueAsString(result.getNeedImproveSkills()));
            analysis.setNotKnownJson(objectMapper.writeValueAsString(result.getNotKnownSkills()));
            analysis.setPriorityJson(objectMapper.writeValueAsString(result.getPriorityItems()));
        } catch (JsonProcessingException e) {
            log.warn("序列化匹配结果失败", e);
        }

        analysis.setAnalysisReport(result.getAnalysisReport());
        analysis.setCreatedAt(LocalDateTime.now());
        matchAnalysisMapper.insert(analysis);
    }

    @Override
    public MatchAnalysis getById(Long userId, Long id) {
        MatchAnalysis analysis = matchAnalysisMapper.selectById(id);
        if (analysis == null || !analysis.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.MATCH_ANALYSIS_NOT_FOUND);
        }
        return analysis;
    }

    @Override
    public List<MatchHistoryDTO> listHistoryByUserId(Long userId) {
        List<MatchAnalysis> analyses = matchAnalysisMapper.selectList(
                new LambdaQueryWrapper<MatchAnalysis>()
                        .eq(MatchAnalysis::getUserId, userId)
                        .orderByDesc(MatchAnalysis::getCreatedAt)
        );
        return analyses.stream().map(this::convertToHistoryDTO).collect(Collectors.toList());
    }

    @Override
    public List<MatchHistoryDTO> listHistoryByJdId(Long userId, Long jdId) {
        List<MatchAnalysis> analyses = matchAnalysisMapper.selectList(
                new LambdaQueryWrapper<MatchAnalysis>()
                        .eq(MatchAnalysis::getUserId, userId)
                        .eq(MatchAnalysis::getJdId, jdId)
                        .orderByDesc(MatchAnalysis::getCreatedAt)
        );
        return analyses.stream().map(this::convertToHistoryDTO).collect(Collectors.toList());
    }

    private MatchHistoryDTO convertToHistoryDTO(MatchAnalysis analysis) {
        JobDescription jd = jobDescriptionMapper.selectById(analysis.getJdId());

        List<String> masteredSkills = parseJsonList(analysis.getMasteredJson());
        List<String> needImproveSkills = parseJsonList(analysis.getNeedImproveJson());
        List<String> notKnownSkills = parseJsonList(analysis.getNotKnownJson());

        return MatchHistoryDTO.builder()
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
    }

    private List<String> parseJsonList(String json) {
        if (json == null) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.warn("解析 JSON 列表失败", e);
            return List.of();
        }
    }

    @Override
    public void deleteById(Long userId, Long id) {
        MatchAnalysis analysis = getById(userId, id);
        matchAnalysisMapper.deleteById(id);
        log.info("用户 {} 删除匹配分析记录: {}", userId, id);
    }

    private static class JdRequirements {
        List<String> techRequired = new ArrayList<>();
        List<String> techPreferred = new ArrayList<>();
        List<String> softRequired = new ArrayList<>();
        List<String> softPreferred = new ArrayList<>();
    }
}