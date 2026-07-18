package com.rhy.interviewprep.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "learning_plan", autoResultMap = true)
public class LearningPlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long jdId;

    private Long matchId;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private String phasesJson;

    private Integer totalEstimatedHours;

    private LocalDateTime createdAt;
}