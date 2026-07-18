package com.rhy.interviewprep.dto;

import lombok.Data;

/**
 * 面经向量检索请求 DTO
 */
@Data
public class InterviewSearchRequest {

    /** 搜索文本（自然语言问题或关键词） */
    private String query;

    /** 返回结果数量，默认5 */
    private Integer topK = 5;

    /** 最低相似度阈值（0~1），低于此值的结果被过滤，默认0.5 */
    private Double minSimilarity = 0.7;
}