package com.rhy.interviewprep.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.rhy.interviewprep.dto.InterviewQuestionVO;
import com.rhy.interviewprep.entity.InterviewQuestion;

/**
 * 面经题目服务（列表查询、详情查询）
 */
public interface InterviewQuestionService {

    /**
     * 分页查询面经题目（支持分类、难度、公司筛选）
     *
     * @param page      页码（从1开始）
     * @param size      每页条数
     * @param category  技术分类（可选）
     * @param difficulty 难度等级（可选，1-简单 2-中等 3-困难）
     * @param company   来源公司（可选）
     * @return 分页结果（不含embedding字段）
     */
    IPage<InterviewQuestionVO> list(int page, int size, String category, Integer difficulty, String company);

    /**
     * 查询面经题目详情
     *
     * @param id 题目ID
     * @return 题目详情（不含embedding字段）
     */
    InterviewQuestionVO getDetail(Long id);
}