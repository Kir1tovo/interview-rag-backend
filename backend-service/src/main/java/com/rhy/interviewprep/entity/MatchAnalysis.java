package com.rhy.interviewprep.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "match_analysis", autoResultMap = true)
public class MatchAnalysis {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long jdId;

    private Double matchScore;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private String matchedSkills;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private String missingSkills;

    private String analysisReport;

    private LocalDateTime createdAt;
}