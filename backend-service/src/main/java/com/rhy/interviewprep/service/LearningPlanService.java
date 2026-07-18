package com.rhy.interviewprep.service;

import com.rhy.interviewprep.dto.LearningPlanDTO;
import com.rhy.interviewprep.entity.LearningPlan;

import java.util.List;

public interface LearningPlanService {
    //生成学习计划
    LearningPlanDTO generate(Long userId, Long matchId);
    //重新生成学习计划
    LearningPlanDTO regenerate(Long userId, Long planId);
    //查询学习计划详情
    LearningPlanDTO getById(Long userId, Long planId);
    //查询用户学习计划列表
    List<LearningPlan> listByUserId(Long userId);
    //查询指定JD的学习计划列表
    List<LearningPlan> listByJdId(Long userId, Long jdId);
    //删除学习计划
    void deleteById(Long userId, Long planId);
}