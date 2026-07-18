package com.rhy.interviewprep.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.rhy.interviewprep.config.VectorTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 面经题目表
 */
@Data
@TableName(value = "interview_question", autoResultMap = true)
public class InterviewQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 题目 */
    private String question;

    /** 参考答案 */
    private String answer;

    /** 解析 */
    private String analysis;

    /** 技术分类 */
    private String category;

    /** 难度：1-简单 2-中等 3-困难 */
    private Integer difficulty;

    /** 来源公司 */
    private String company;

    /** 向量（pgvector） */
    @TableField(typeHandler = VectorTypeHandler.class)
    private float[] embedding;

    /** 创建时间 */
    private LocalDateTime createdAt;
}