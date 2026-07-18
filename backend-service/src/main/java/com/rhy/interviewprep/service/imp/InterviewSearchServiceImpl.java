package com.rhy.interviewprep.service.imp;

import com.rhy.interviewprep.common.BusinessException;
import com.rhy.interviewprep.common.ErrorCode;
import com.rhy.interviewprep.dto.InterviewSearchRequest;
import com.rhy.interviewprep.dto.InterviewSearchVO;
import com.rhy.interviewprep.entity.InterviewQuestion;
import com.rhy.interviewprep.mapper.InterviewQuestionMapper;
import com.rhy.interviewprep.service.InterviewSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 面经向量检索服务实现
 * 使用 pgvector 余弦距离进行语义相似度检索
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewSearchServiceImpl implements InterviewSearchService {

    private final InterviewQuestionMapper interviewQuestionMapper;
    private final EmbeddingModel embeddingModel;

    @Override
    public List<InterviewSearchVO> search(InterviewSearchRequest request) {
        if (request.getQuery() == null || request.getQuery().trim().isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "搜索内容不能为空");
        }

        int topK = request.getTopK() != null ? request.getTopK() : 5;
        double minSimilarity = request.getMinSimilarity() != null ? request.getMinSimilarity() : 0.5;

        // 1. 将查询文本转为向量
        String queryVector = generateVectorString(request.getQuery());
        if (queryVector == null) {
            log.warn("查询文本生成向量失败：{}", request.getQuery());
            return Collections.emptyList();
        }

        // 2. 执行 pgvector 向量检索
        List<InterviewSearchVO> results = interviewQuestionMapper.searchByVector(
                queryVector, minSimilarity, topK);

        log.info("向量检索完成：query='{}', topK={}, minSimilarity={}, 命中{}条",
                request.getQuery(), topK, minSimilarity, results.size());

        return results;
    }

    @Override
    public List<InterviewSearchVO> findSimilar(Long questionId, int topK, double minSimilarity) {
        // 1. 查找目标题目及其向量
        InterviewQuestion question = interviewQuestionMapper.selectById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.QUESTION_NOT_FOUND, "题目不存在");
        }
        if (question.getEmbedding() == null || question.getEmbedding().length == 0) {
            log.warn("题目{}无向量数据，无法查找相似题目", questionId);
            return Collections.emptyList();
        }

        // 2. 将 float[] 转为 pgvector 字符串格式
        String queryVector = floatArrayToVectorString(question.getEmbedding());

        // 3. 执行相似度检索（排除自身）
        List<InterviewSearchVO> results = interviewQuestionMapper.findSimilarById(
                questionId, queryVector, minSimilarity, topK);

        log.info("相似题目检索完成：questionId={}, topK={}, 命中{}条", questionId, topK, results.size());

        return results;
    }

    /**
     * 调用 EmbeddingModel 生成向量，并转为 pgvector 字符串格式
     */
    private String generateVectorString(String text) {
        try {
            float[] embedding = embeddingModel.embed(text);
            if (embedding == null || embedding.length == 0) {
                return null;
            }
            return floatArrayToVectorString(embedding);
        } catch (Exception e) {
            log.error("生成Embedding向量失败：{}", text.substring(0, Math.min(50, text.length())), e);
            return null;
        }
    }

    /**
     * 将 float[] 转为 pgvector 字符串格式：[0.1,0.2,0.3]
     */
    private String floatArrayToVectorString(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}