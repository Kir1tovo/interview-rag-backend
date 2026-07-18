package com.rhy.interviewprep.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 面经题目 VO（列表/详情展示，不含 embedding 向量数据）
 */
@Data
public class InterviewQuestionVO {

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

    /** 创建时间 */
    private LocalDateTime createdAt;
}