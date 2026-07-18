package com.rhy.interviewprep.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 面经题目向量检索结果 VO
 * 不包含 embedding 向量数据（体积大，前端不需要）
 */
@Data
public class InterviewSearchVO {

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

    /** 相似度分数（0~1，1表示完全相同） */
    private Double similarity;
}