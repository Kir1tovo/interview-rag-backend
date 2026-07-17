package com.rhy.interviewprep.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "job_description", autoResultMap = true)
public class JobDescription {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String company;

    private String department;

    private String position;

    private String location;

    private String education;

    private String experience;

    private String salary;

    private String rawText;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private String requirementsJson;

    @TableField(typeHandler = JacksonTypeHandler.class)
    private String softSkillsJson;

    private String responsibilities;

    private LocalDateTime createdAt;
}