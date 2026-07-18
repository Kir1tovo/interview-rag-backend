package com.rhy.interviewprep.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rhy.interviewprep.dto.InterviewSearchVO;
import com.rhy.interviewprep.entity.InterviewQuestion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface InterviewQuestionMapper extends BaseMapper<InterviewQuestion> {

    /**
     * pgvector 向量相似度检索（余弦距离）
     * 使用 <=> 操作符计算 cosine distance，相似度 = 1 - distance
     *
     * @param queryVector  查询向量字符串，格式：[0.1,0.2,0.3]
     * @param minSimilarity 最低相似度阈值
     * @param limit        返回数量
     * @return 检索结果列表（含相似度分数）
     */
    @Select("SELECT id, question, answer, analysis, category, difficulty, company, created_at, " +
            "1 - (embedding <=> '${queryVector}'::vector) AS similarity " +
            "FROM interview_question " +
            "WHERE embedding IS NOT NULL " +
            "AND 1 - (embedding <=> '${queryVector}'::vector) >= #{minSimilarity} " +
            "ORDER BY embedding <=> '${queryVector}'::vector " +
            "LIMIT #{limit}")
    List<InterviewSearchVO> searchByVector(@Param("queryVector") String queryVector,
                                           @Param("minSimilarity") double minSimilarity,
                                           @Param("limit") int limit);

    /**
     * 根据题目ID查找相似题目（排除自身）
     *
     * @param questionId   目标题目ID
     * @param queryVector  目标题目的向量
     * @param minSimilarity 最低相似度阈值
     * @param limit        返回数量
     * @return 相似题目列表
     */
    @Select("SELECT id, question, answer, analysis, category, difficulty, company, created_at, " +
            "1 - (embedding <=> '${queryVector}'::vector) AS similarity " +
            "FROM interview_question " +
            "WHERE embedding IS NOT NULL " +
            "AND id != #{questionId} " +
            "AND 1 - (embedding <=> '${queryVector}'::vector) >= #{minSimilarity} " +
            "ORDER BY embedding <=> '${queryVector}'::vector " +
            "LIMIT #{limit}")
    List<InterviewSearchVO> findSimilarById(@Param("questionId") Long questionId,
                                            @Param("queryVector") String queryVector,
                                            @Param("minSimilarity") double minSimilarity,
                                            @Param("limit") int limit);
}