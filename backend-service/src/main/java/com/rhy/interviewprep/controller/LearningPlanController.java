package com.rhy.interviewprep.controller;

import com.rhy.interviewprep.common.Result;
import com.rhy.interviewprep.dto.LearningPlanDTO;
import com.rhy.interviewprep.entity.LearningPlan;
import com.rhy.interviewprep.service.LearningPlanService;
import com.rhy.interviewprep.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学习计划控制器
 * 提供学习计划生成、查询、删除等接口
 */
@RestController
@RequestMapping("/api/learning-plan")
@RequiredArgsConstructor
public class LearningPlanController {

    private final LearningPlanService learningPlanService;

    /**
     * 生成学习计划
     * 基于匹配分析结果，调用 DeepSeek 生成阶段化学习计划
     *
     * @param matchId 匹配分析记录 ID
     * @return 生成的学习计划详情
     */
    @PostMapping("/generate/{matchId}")
    public Result<LearningPlanDTO> generate(@PathVariable Long matchId) {
        Long userId = SecurityUtils.getCurrentUserId();
        LearningPlanDTO plan = learningPlanService.generate(userId, matchId);
        return Result.success(plan);
    }

    /**
     * 重新生成学习计划
     * 基于原有学习计划重新调用大模型生成
     *
     * @param planId 原学习计划 ID
     * @return 重新生成的学习计划详情
     */
    @PostMapping("/regenerate/{planId}")
    public Result<LearningPlanDTO> regenerate(@PathVariable Long planId) {
        Long userId = SecurityUtils.getCurrentUserId();
        LearningPlanDTO plan = learningPlanService.regenerate(userId, planId);
        return Result.success(plan);
    }

    /**
     * 获取学习计划详情
     *
     * @param planId 学习计划 ID
     * @return 学习计划详情（含阶段、学习内容）
     */
    @GetMapping("/{planId}")
    public Result<LearningPlanDTO> getDetail(@PathVariable Long planId) {
        Long userId = SecurityUtils.getCurrentUserId();
        LearningPlanDTO plan = learningPlanService.getById(userId, planId);
        return Result.success(plan);
    }

    /**
     * 获取当前用户的学习计划列表
     *
     * @return 学习计划列表
     */
    @GetMapping("/list")
    public Result<List<LearningPlan>> list() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<LearningPlan> list = learningPlanService.listByUserId(userId);
        return Result.success(list);
    }

    /**
     * 获取指定 JD 的学习计划列表
     *
     * @param jdId JD ID
     * @return 该 JD 关联的学习计划列表
     */
    @GetMapping("/list/{jdId}")
    public Result<List<LearningPlan>> listByJd(@PathVariable Long jdId) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<LearningPlan> list = learningPlanService.listByJdId(userId, jdId);
        return Result.success(list);
    }

    /**
     * 删除学习计划
     *
     * @param planId 学习计划 ID
     * @return 操作结果
     */
    @DeleteMapping("/{planId}")
    public Result<Void> delete(@PathVariable Long planId) {
        Long userId = SecurityUtils.getCurrentUserId();
        learningPlanService.deleteById(userId, planId);
        return Result.success();
    }
}