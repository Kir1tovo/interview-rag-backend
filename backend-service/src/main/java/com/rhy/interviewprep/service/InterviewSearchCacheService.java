package com.rhy.interviewprep.service;

import com.rhy.interviewprep.dto.InterviewSearchVO;

import java.util.List;

/**
 * 面经搜索 Redis 缓存服务
 * 缓存热门搜索查询结果，减少向量检索（Embedding + pgvector）的开销
 *
 * 缓存策略：
 * - key 格式：interview:search:{queryHash}:{topK}:{minSimilarity}
 * - TTL：2 小时，热门查询会自动续期
 * - 导入新面经时清除全部搜索缓存
 */
public interface InterviewSearchCacheService {

    /**
     * 从缓存获取搜索结果
     *
     * @param query         搜索文本
     * @param topK          返回数量
     * @param minSimilarity 最低相似度
     * @return 缓存的搜索结果列表，未命中返回 null
     */
    List<InterviewSearchVO> get(String query, int topK, double minSimilarity);

    /**
     * 将搜索结果写入缓存
     *
     * @param query         搜索文本
     * @param topK          返回数量
     * @param minSimilarity 最低相似度
     * @param results       搜索结果列表
     */
    void put(String query, int topK, double minSimilarity, List<InterviewSearchVO> results);

    /**
     * 清除所有面经搜索缓存
     * 在导入新面经数据时调用，确保缓存一致性
     */
    void clearAll();
}