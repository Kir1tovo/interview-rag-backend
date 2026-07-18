package com.rhy.interviewprep.service;

import com.rhy.interviewprep.dto.InterviewSearchRequest;
import com.rhy.interviewprep.dto.InterviewSearchVO;

import java.util.List;

/**
 * 面经向量检索服务
 */
public interface InterviewSearchService {

    /**
     * 向量语义检索
     * 将查询文本转为向量，通过 pgvector 余弦相似度检索最相关的面经题目
     *
     * @param request 检索请求（query, topK, minSimilarity）
     * @return 检索结果列表（含相似度分数）
     */
    List<InterviewSearchVO> search(InterviewSearchRequest request);

    /**
     * 根据题目ID查找相似题目
     * 使用该题目的 embedding 向量进行相似度检索
     *
     * @param questionId   目标题目ID
     * @param topK         返回数量
     * @param minSimilarity 最低相似度阈值
     * @return 相似题目列表
     */
    List<InterviewSearchVO> findSimilar(Long questionId, int topK, double minSimilarity);
}