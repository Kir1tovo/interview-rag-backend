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

        // 按category区分用户技能：技术栈和软技能
        List<UserSkill> techSkills = userSkills.stream()
                .filter(s -> "soft".equalsIgnoreCase(s.getCategory()))
                .toList();
        List<UserSkill> softSkills = userSkills.stream()
                .filter(s -> !"soft".equalsIgnoreCase(s.getCategory()))
                .toList();

        // 技术栈技能Map（精确匹配用）
        Map<String, UserSkill> techSkillMap = techSkills.stream()
                .collect(Collectors.toMap(
                        s -> s.getSkillName().toLowerCase(),
                        s -> s,
                        (existing, replacement) -> existing
                ));

        // 软技能Map（精确匹配用）
        Map<String, UserSkill> softSkillMap = softSkills.stream()
                .collect(Collectors.toMap(
                        s -> s.getSkillName().toLowerCase(),
                        s -> s,
                        (existing, replacement) -> existing
                ));

        JdRequirements jdRequirements = parseJdRequirements(jd);

        // 合并所有JD技能，一次LLM调用完成语义匹配
        List<String> allJdSkills = new ArrayList<>();
        allJdSkills.addAll(jdRequirements.techRequired);
        allJdSkills.addAll(jdRequirements.techPreferred);
        allJdSkills.addAll(jdRequirements.softRequired);
        allJdSkills.addAll(jdRequirements.softPreferred);

        // LLM语义匹配：将所有用户技能传给LLM
        Map<String, LlmSkillMatch> llmMatchMap = matchSkillsWithLlm(allJdSkills, userSkills);

        // 技术栈匹配：只用用户的技术栈技能
        List<MatchResultDTO.SkillMatchDetail> techMatches = matchTechSkills(techSkillMap, jdRequirements.techRequired, jdRequirements.techPreferred, llmMatchMap);
        // 软技能匹配：只用用户的软技能
        List<MatchResultDTO.SkillMatchDetail> softMatches = matchSoftSkills(softSkillMap, jdRequirements.softRequired, jdRequirements.softPreferred, llmMatchMap);

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
                                                                  List<String> preferred,
                                                                  Map<String, LlmSkillMatch> llmMatchMap) {
        List<MatchResultDTO.SkillMatchDetail> matches = new ArrayList<>();

        for (String skill : required) {
            matches.add(createMatchDetail(skill, true, userSkillMap, llmMatchMap));
        }
        for (String skill : preferred) {
            matches.add(createMatchDetail(skill, false, userSkillMap, llmMatchMap));
        }

        return matches;
    }

    private List<MatchResultDTO.SkillMatchDetail> matchSoftSkills(Map<String, UserSkill> userSkillMap,
                                                                   List<String> required,
                                                                   List<String> preferred,
                                                                   Map<String, LlmSkillMatch> llmMatchMap) {
        List<MatchResultDTO.SkillMatchDetail> matches = new ArrayList<>();

        for (String skill : required) {
            matches.add(createSoftMatchDetail(skill, true, userSkillMap, llmMatchMap));
        }
        for (String skill : preferred) {
            matches.add(createSoftMatchDetail(skill, false, userSkillMap, llmMatchMap));
        }

        return matches;
    }

    /**
     * 创建技术技能匹配详情（支持LLM语义匹配）
     * <p>匹配优先级：精确匹配 > LLM语义匹配 > 无匹配</p>
     * <p>LLM匹配类型与评分：
     * <ul>
     *   <li>exact：精确匹配，按等级比较（100/60）</li>
     *   <li>subset：用户技能是JD要求的子集（如MySQL→数据库），按等级比较</li>
     *   <li>superset：用户技能比JD要求更宽泛（如数据库→MySQL），70分</li>
     *   <li>related：相关技能（如PostgreSQL→MySQL），40分</li>
     *   <li>none：无匹配，0分</li>
     * </ul>
     * </p>
     */
    private MatchResultDTO.SkillMatchDetail createMatchDetail(String skillName, boolean isRequired,
                                                               Map<String, UserSkill> userSkillMap,
                                                               Map<String, LlmSkillMatch> llmMatchMap) {
        String jdLevel = isRequired ? "熟悉" : "了解";
        int jdLevelRank = isRequired ? 2 : 1;

        // 先尝试精确字符串匹配
        UserSkill userSkill = userSkillMap.get(skillName.toLowerCase());

        // 如果精确匹配失败，尝试LLM语义匹配
        LlmSkillMatch llmMatch = llmMatchMap.get(skillName.toLowerCase());

        String userLevel;
        int score;
        String matchStatus;
        String matchedUserSkill = null;
        String matchType = "none";

        if (userSkill != null) {
            // 精确匹配成功
            int userLevelRank = userSkill.getLevel();
            userLevel = LEVEL_MAP.getOrDefault(userLevelRank, "未知");
            matchType = "exact";
            matchedUserSkill = userSkill.getSkillName();

            if (userLevelRank >= jdLevelRank) {
                score = 100;
                matchStatus = "已掌握";
            } else {
                score = 60;
                matchStatus = "需要加强";
            }
        } else if (llmMatch != null && llmMatch.matchedUserSkill != null
                && !"none".equals(llmMatch.matchType)) {
            // LLM语义匹配成功
            matchType = llmMatch.matchType;
            matchedUserSkill = llmMatch.matchedUserSkill;

            // 查找匹配到的用户技能对象
            UserSkill matchedSkill = userSkillMap.get(matchedUserSkill.toLowerCase());

            if (matchedSkill != null) {
                int userLevelRank = matchedSkill.getLevel();
                userLevel = matchedSkill.getSkillName() + "(" + LEVEL_MAP.getOrDefault(userLevelRank, "未知") + ")";
                score = calculateMatchScore(matchType, userLevelRank, jdLevelRank, llmMatch.confidence);
            } else {
                // LLM匹配到了技能名但用户技能表中找不到（名称不完全一致）
                userLevel = matchedUserSkill;
                score = calculateMatchScore(matchType, 1, jdLevelRank, llmMatch.confidence);
            }

            matchStatus = score >= 100 ? "已掌握" : (score > 0 ? "需要加强" : "完全不会");
        } else {
            // 无匹配
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
                .matchedUserSkill(matchedUserSkill)
                .matchType(matchType)
                .build();
    }

    /**
     * 创建软技能匹配详情（支持LLM语义匹配）
     * <p>软技能为二值判断（具备/不具备），语义匹配即视为具备</p>
     */
    private MatchResultDTO.SkillMatchDetail createSoftMatchDetail(String skillName, boolean isRequired,
                                                                    Map<String, UserSkill> userSkillMap,
                                                                    Map<String, LlmSkillMatch> llmMatchMap) {
        // 先尝试精确匹配
        boolean hasSkill = userSkillMap.containsKey(skillName.toLowerCase());

        // 如果精确匹配失败，尝试LLM语义匹配
        LlmSkillMatch llmMatch = llmMatchMap.get(skillName.toLowerCase());

        String userLevel;
        int score;
        String matchStatus;
        String matchedUserSkill = null;
        String matchType = "none";

        if (hasSkill) {
            userLevel = "具备";
            score = 100;
            matchStatus = "已掌握";
            matchType = "exact";
            matchedUserSkill = skillName;
        } else if (llmMatch != null && llmMatch.matchedUserSkill != null
                && !"none".equals(llmMatch.matchType)) {
            matchType = llmMatch.matchType;
            matchedUserSkill = llmMatch.matchedUserSkill;
            userLevel = "具备(" + matchedUserSkill + ")";
            score = 100;
            matchStatus = "已掌握";
        } else {
            userLevel = "不具备";
            score = 0;
            matchStatus = "完全不会";
        }

        return MatchResultDTO.SkillMatchDetail.builder()
                .skillName(skillName)
                .jdLevel("了解")
                .userLevel(userLevel)
                .score(score)
                .matchStatus(matchStatus)
                .isRequired(isRequired)
                .matchedUserSkill(matchedUserSkill)
                .matchType(matchType)
                .build();
    }

    /**
     * 根据LLM匹配类型计算技能匹配分数
     *
     * @param matchType     匹配类型：exact/subset/superset/related/none
     * @param userLevelRank 用户技能等级（1=了解,2=熟悉,3=精通）
     * @param jdLevelRank   JD要求等级
     * @param confidence    LLM匹配置信度（0~1）
     * @return 匹配分数（0~100）
     */
    private int calculateMatchScore(String matchType, int userLevelRank, int jdLevelRank, double confidence) {
        int baseScore;
        switch (matchType) {
            case "exact":
                // 精确匹配：按等级比较
                baseScore = userLevelRank >= jdLevelRank ? 100 : 60;
                break;
            case "subset":
                // 用户技能是JD要求的子集（如用户有MySQL，JD要求数据库）→ 满足要求
                baseScore = userLevelRank >= jdLevelRank ? 100 : 60;
                break;
            case "superset":
                // 用户技能比JD要求更宽泛（如用户有数据库，JD要求MySQL）→ 部分满足
                baseScore = 70;
                break;
            case "related":
                // 相关技能（如用户有PostgreSQL，JD要求MySQL）→ 有一定基础
                baseScore = 40;
                break;
            default:
                baseScore = 0;
        }
        // 按置信度调整分数
        return (int) Math.round(baseScore * confidence);
    }

    /**
     * 使用LLM进行技能语义匹配
     * <p>将所有JD技能和用户技能一次性发送给LLM，获取语义匹配关系。
     * 解决"数据库"vs"MySQL"等模糊/上下级技能无法精确字符串匹配的问题。</p>
     * <p>如果LLM调用失败，返回空Map，调用方将回退到精确字符串匹配。</p>
     *
     * @param jdSkills   JD中的技能要求列表
     * @param userSkills 用户自定义技能列表
     * @return JD技能名(小写) → LLM匹配结果的映射
     */
    private Map<String, LlmSkillMatch> matchSkillsWithLlm(List<String> jdSkills, List<UserSkill> userSkills) {
        if (jdSkills.isEmpty() || userSkills.isEmpty()) {
            return Map.of();
        }

        try {
            String jdSkillsStr = String.join(", ", jdSkills);
            String userSkillsStr = userSkills.stream()
                    .map(s -> s.getSkillName() + "(" + LEVEL_MAP.getOrDefault(s.getLevel(), "未知") + ")")
                    .collect(Collectors.joining(", "));

            String userPrompt = String.format(
                    MatchAnalysisPrompt.SKILL_MATCH_USER_PROMPT_TEMPLATE,
                    jdSkillsStr, userSkillsStr);

            List<Message> messages = List.of(
                    new SystemMessage(MatchAnalysisPrompt.SKILL_MATCH_SYSTEM_PROMPT),
                    new UserMessage(userPrompt));

            Prompt prompt = new Prompt(messages);
            ChatResponse chatResponse = chatModel.call(prompt);

            String response = chatResponse.getResult().getOutput().getText();
            String json = response.trim();

            // 去除markdown代码块标记
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
            JsonNode matchesNode = root.get("matches");

            Map<String, LlmSkillMatch> result = new HashMap<>();
            if (matchesNode != null && matchesNode.isArray()) {
                for (JsonNode matchNode : matchesNode) {
                    LlmSkillMatch match = new LlmSkillMatch();
                    match.jdSkill = matchNode.get("jdSkill").asText();
                    match.matchedUserSkill =
                            matchNode.has("matchedUserSkill") && !matchNode.get("matchedUserSkill").isNull()
                                    ? matchNode.get("matchedUserSkill").asText() : null;
                    match.matchType = matchNode.get("matchType").asText("none");
                    match.confidence = matchNode.has("confidence") ? matchNode.get("confidence").asDouble() : 0.5;
                    result.put(match.jdSkill.toLowerCase(), match);
                }
            }

            log.info("LLM技能语义匹配完成，共匹配{}个JD技能", result.size());
            return result;

        } catch (Exception e) {
            log.warn("LLM技能语义匹配失败，将回退到精确字符串匹配", e);
            return Map.of();
        }
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

    /** LLM技能语义匹配结果 */
    private static class LlmSkillMatch {
        String jdSkill;
        String matchedUserSkill;
        String matchType;
        double confidence;
    }

    private static class JdRequirements {
        List<String> techRequired = new ArrayList<>();
        List<String> techPreferred = new ArrayList<>();
        List<String> softRequired = new ArrayList<>();
        List<String> softPreferred = new ArrayList<>();
    }
}