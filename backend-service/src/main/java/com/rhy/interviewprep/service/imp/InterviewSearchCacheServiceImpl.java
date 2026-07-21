package com.rhy.interviewprep.service.imp;

import com.rhy.interviewprep.dto.InterviewSearchVO;
import com.rhy.interviewprep.service.InterviewSearchCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 面经搜索 Redis 缓存服务实现
 * 缓存热门搜索查询结果，减少向量检索（Embedding + pgvector）的开销
 *
 * 缓存策略：
 * - key 格式：interview:search:{queryHash}:{topK}:{minSimilarity}
 * - TTL：2 小时，热门查询会自动续期
 * - 导入新面经时清除全部搜索缓存
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewSearchCacheServiceImpl implements InterviewSearchCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    /** 缓存 key 前缀 */
    private static final String CACHE_PREFIX = "interview:search:";

    /** 默认缓存过期时间（小时） */
    private static final long DEFAULT_TTL_HOURS = 2;

    /**
     * 生成缓存 key
     * 格式：interview:search:{queryHash}:{topK}:{minSimilarity}
     *
     * @param query         搜索文本
     * @param topK          返回数量
     * @param minSimilarity 最低相似度
     * @return 缓存 key
     */
    private String buildCacheKey(String query, int topK, double minSimilarity) {
        // 使用 query 的 hashCode 避免过长 key
        int queryHash = query.trim().toLowerCase().hashCode();
        return CACHE_PREFIX + queryHash + ":" + topK + ":" + minSimilarity;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<InterviewSearchVO> get(String query, int topK, double minSimilarity) {
        String key = buildCacheKey(query, topK, minSimilarity);
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached instanceof List) {
                log.debug("缓存命中：key={}", key);
                return (List<InterviewSearchVO>) cached;
            }
        } catch (Exception e) {
            // 缓存异常不影响主流程，降级到数据库查询
            log.warn("读取缓存异常，降级到数据库查询：key={}, error={}", key, e.getMessage());
        }
        return null;
    }

    @Override
    public void put(String query, int topK, double minSimilarity, List<InterviewSearchVO> results) {
        if (results == null || results.isEmpty()) {
            // 空结果不缓存，避免缓存穿透
            return;
        }
        String key = buildCacheKey(query, topK, minSimilarity);
        try {
            redisTemplate.opsForValue().set(key, results, DEFAULT_TTL_HOURS, TimeUnit.HOURS);
            log.debug("缓存写入：key={}, 结果数={}", key, results.size());
        } catch (Exception e) {
            log.warn("写入缓存异常：key={}, error={}", key, e.getMessage());
        }
    }

    @Override
    public void clearAll() {
        try {
            var keys = redisTemplate.keys(CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("清除面经搜索缓存：共{}个key", keys.size());
            }
        } catch (Exception e) {
            log.warn("清除缓存异常：error={}", e.getMessage());
        }
    }
}