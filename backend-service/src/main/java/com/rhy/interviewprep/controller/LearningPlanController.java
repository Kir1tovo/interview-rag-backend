package com.rhy.interviewprep.controller;

import com.rhy.interviewprep.common.Result;
import com.rhy.interviewprep.dto.LearningPlanDTO;
import com.rhy.interviewprep.entity.LearningPlan;
import com.rhy.interviewprep.service.LearningPlanService;
import com.rhy.interviewprep.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning-plan")
@RequiredArgsConstructor
public class LearningPlanController {

    private final LearningPlanService learningPlanService;
    //生成学习计划
    @PostMapping("/generate/{matchId}")
    public Result<LearningPlanDTO> generate(@PathVariable Long matchId) {
        Long userId = SecurityUtils.getCurrentUserId();
        LearningPlanDTO plan = learningPlanService.generate(userId, matchId);
        return Result.success(plan);
    }
    //重新生成学习计划
    @PostMapping("/regenerate/{planId}")
    public Result<LearningPlanDTO> regenerate(@PathVariable Long planId) {
        Long userId = SecurityUtils.getCurrentUserId();
        LearningPlanDTO plan = learningPlanService.regenerate(userId, planId);
        return Result.success(plan);
    }
    //获取学习计划详情
    @GetMapping("/{planId}")
    public Result<LearningPlanDTO> getDetail(@PathVariable Long planId) {
        Long userId = SecurityUtils.getCurrentUserId();
        LearningPlanDTO plan = learningPlanService.getById(userId, planId);
        return Result.success(plan);
    }
    //获取用户学习计划列表
    @GetMapping("/list")
    public Result<List<LearningPlan>> list() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<LearningPlan> list = learningPlanService.listByUserId(userId);
        return Result.success(list);
    }
    //获取指定JD的学习计划列表
    @GetMapping("/list/{jdId}")
    public Result<List<LearningPlan>> listByJd(@PathVariable Long jdId) {
        Long userId = SecurityUtils.getCurrentUserId();
        List<LearningPlan> list = learningPlanService.listByJdId(userId, jdId);
        return Result.success(list);
    }
    //删除学习计划
    @DeleteMapping("/{planId}")
    public Result<Void> delete(@PathVariable Long planId) {
        Long userId = SecurityUtils.getCurrentUserId();
        learningPlanService.deleteById(userId, planId);
        return Result.success();
    }
}