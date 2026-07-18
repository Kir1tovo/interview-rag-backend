package com.rhy.interviewprep.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhy.interviewprep.common.BusinessException;
import com.rhy.interviewprep.common.ErrorCode;
import com.rhy.interviewprep.constants.LearningPlanPrompt;
import com.rhy.interviewprep.dto.LearningPlanDTO;
import com.rhy.interviewprep.entity.JobDescription;
import com.rhy.interviewprep.entity.LearningPlan;
import com.rhy.interviewprep.entity.MatchAnalysis;
import com.rhy.interviewprep.entity.UserSkill;
import com.rhy.interviewprep.mapper.JobDescriptionMapper;
import com.rhy.interviewprep.mapper.LearningPlanMapper;
import com.rhy.interviewprep.mapper.MatchAnalysisMapper;
import com.rhy.interviewprep.mapper.UserSkillMapper;
import com.rhy.interviewprep.service.LearningPlanService;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LearningPlanServiceImpl implements LearningPlanService {

    private final LearningPlanMapper learningPlanMapper;
    private final MatchAnalysisMapper matchAnalysisMapper;
    private final JobDescriptionMapper jobDescriptionMapper;
    private final UserSkillMapper userSkillMapper;
    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    private static final Map<Integer, String> LEVEL_MAP = Map.of(
            1, "了解",
            2, "熟悉",
            3, "精通"
    );

    @Override
    public LearningPlanDTO generate(Long userId, Long matchId) {
        MatchAnalysis analysis = matchAnalysisMapper.selectById(matchId);
        if (analysis == null || !analysis.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.MATCH_ANALYSIS_NOT_FOUND);
        }

        JobDescription jd = jobDescriptionMapper.selectById(analysis.getJdId());
        if (jd == null) {
            throw new BusinessException(ErrorCode.JD_NOT_FOUND);
        }

        List<UserSkill> userSkills = userSkillMapper.selectList(
                new LambdaQueryWrapper<UserSkill>()
                        .eq(UserSkill::getUserId, userId)
        );

        List<String> masteredSkills = parseJsonList(analysis.getMasteredJson());
        List<String> needImproveSkills = parseJsonList(analysis.getNeedImproveJson());
        List<String> notKnownSkills = parseJsonList(analysis.getNotKnownJson());
        List<Map<String, Object>> priorityItems = parsePriorityJson(analysis.getPriorityJson());

        LearningPlanDTO plan = generatePlan(jd, userSkills, analysis, masteredSkills, needImproveSkills, notKnownSkills, priorityItems);

        savePlan(userId, analysis.getJdId(), matchId, plan);

        log.info("学习计划生成完成，用户: {}, 匹配分析: {}", userId, matchId);

        return plan;
    }

    @Override
    public LearningPlanDTO regenerate(Long userId, Long planId) {
        LearningPlan plan = learningPlanMapper.selectById(planId);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.LEARNING_PLAN_NOT_FOUND);
        }

        return generate(userId, plan.getMatchId());
    }

    @Override
    public LearningPlanDTO getById(Long userId, Long planId) {
        LearningPlan plan = learningPlanMapper.selectById(planId);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.LEARNING_PLAN_NOT_FOUND);
        }

        try {
            LearningPlanDTO dto = new LearningPlanDTO();
            dto.setTotalEstimatedHours(plan.getTotalEstimatedHours());
            dto.setPhases(objectMapper.readValue(plan.getPhasesJson(), new TypeReference<List<LearningPlanDTO.Phase>>() {}));
            return dto;
        } catch (JsonProcessingException e) {
            log.error("解析学习计划 JSON 失败", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public List<LearningPlan> listByUserId(Long userId) {
        return learningPlanMapper.selectList(
                new LambdaQueryWrapper<LearningPlan>()
                        .eq(LearningPlan::getUserId, userId)
                        .orderByDesc(LearningPlan::getCreatedAt)
        );
    }

    @Override
    public List<LearningPlan> listByJdId(Long userId, Long jdId) {
        return learningPlanMapper.selectList(
                new LambdaQueryWrapper<LearningPlan>()
                        .eq(LearningPlan::getUserId, userId)
                        .eq(LearningPlan::getJdId, jdId)
                        .orderByDesc(LearningPlan::getCreatedAt)
        );
    }

    @Override
    public void deleteById(Long userId, Long planId) {
        LearningPlan plan = learningPlanMapper.selectById(planId);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.LEARNING_PLAN_NOT_FOUND);
        }
        learningPlanMapper.deleteById(planId);
        log.info("用户 {} 删除学习计划: {}", userId, planId);
    }

    private LearningPlanDTO generatePlan(JobDescription jd,
                                         List<UserSkill> userSkills,
                                         MatchAnalysis analysis,
                                         List<String> masteredSkills,
                                         List<String> needImproveSkills,
                                         List<String> notKnownSkills,
                                         List<Map<String, Object>> priorityItems) {
        try {
            String userSkillsStr = userSkills.stream()
                    .map(s -> s.getSkillName() + "(" + LEVEL_MAP.getOrDefault(s.getLevel(), "未知") + ")")
                    .collect(Collectors.joining(", "));

            String priorityStr = priorityItems.stream()
                    .map(item -> item.get("skillName") + "(优先级:" + item.get("priority") + ")")
                    .collect(Collectors.joining(", "));

            String userPrompt = String.format(
                    LearningPlanPrompt.USER_PROMPT_TEMPLATE,
                    jd.getCompany(),
                    jd.getPosition(),
                    userSkillsStr,
                    analysis.getMatchScore() * 100,
                    analysis.getJdMatchScore() * 100,
                    analysis.getSoftSkillScore() * 100,
                    String.join(", ", masteredSkills),
                    String.join(", ", needImproveSkills),
                    String.join(", ", notKnownSkills),
                    priorityStr
            );

            List<Message> messages = List.of(
                    new SystemMessage(LearningPlanPrompt.SYSTEM_PROMPT),
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

            return objectMapper.readValue(json, LearningPlanDTO.class);

        } catch (Exception e) {
            log.error("生成学习计划失败", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "生成学习计划失败: " + e.getMessage());
        }
    }

    private void savePlan(Long userId, Long jdId, Long matchId, LearningPlanDTO plan) {
        LearningPlan entity = new LearningPlan();
        entity.setUserId(userId);
        entity.setJdId(jdId);
        entity.setMatchId(matchId);
        entity.setTotalEstimatedHours(plan.getTotalEstimatedHours());

        try {
            entity.setPhasesJson(objectMapper.writeValueAsString(plan.getPhases()));
        } catch (JsonProcessingException e) {
            log.warn("序列化学习计划失败", e);
        }

        entity.setCreatedAt(LocalDateTime.now());
        learningPlanMapper.insert(entity);
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

    private List<Map<String, Object>> parsePriorityJson(String json) {
        if (json == null) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
        } catch (JsonProcessingException e) {
            log.warn("解析优先级 JSON 失败", e);
            return List.of();
        }
    }
}