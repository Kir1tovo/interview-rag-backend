package com.rhy.interviewprep.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rhy.interviewprep.common.BusinessException;
import com.rhy.interviewprep.common.ErrorCode;
import com.rhy.interviewprep.dto.InterviewQuestionVO;
import com.rhy.interviewprep.entity.InterviewQuestion;
import com.rhy.interviewprep.mapper.InterviewQuestionMapper;
import com.rhy.interviewprep.service.InterviewQuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 面经题目服务实现（列表查询、详情查询）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewQuestionServiceImpl implements InterviewQuestionService {

    private final InterviewQuestionMapper interviewQuestionMapper;

    @Override
    public IPage<InterviewQuestionVO> list(int page, int size, String category, Integer difficulty, String company) {
        Page<InterviewQuestion> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<InterviewQuestion> wrapper = new LambdaQueryWrapper<>();

        // 条件筛选
        if (category != null && !category.trim().isEmpty()) {
            wrapper.eq(InterviewQuestion::getCategory, category);
        }
        if (difficulty != null) {
            wrapper.eq(InterviewQuestion::getDifficulty, difficulty);
        }
        if (company != null && !company.trim().isEmpty()) {
            wrapper.eq(InterviewQuestion::getCompany, company);
        }

        // 按创建时间倒序
        wrapper.orderByDesc(InterviewQuestion::getCreatedAt);

        // 查询（不查 embedding 字段，减少数据传输）
        wrapper.select(InterviewQuestion::getId, InterviewQuestion::getQuestion,
                InterviewQuestion::getAnswer, InterviewQuestion::getAnalysis,
                InterviewQuestion::getCategory, InterviewQuestion::getDifficulty,
                InterviewQuestion::getCompany, InterviewQuestion::getCreatedAt);

        IPage<InterviewQuestion> questionPage = interviewQuestionMapper.selectPage(pageParam, wrapper);

        // 转换为 VO
        return questionPage.convert(this::toVO);
    }

    @Override
    public InterviewQuestionVO getDetail(Long id) {
        InterviewQuestion question = interviewQuestionMapper.selectById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.QUESTION_NOT_FOUND, "题目不存在");
        }
        return toVO(question);
    }

    /**
     * Entity 转 VO（排除 embedding 大字段）
     */
    private InterviewQuestionVO toVO(InterviewQuestion question) {
        InterviewQuestionVO vo = new InterviewQuestionVO();
        BeanUtils.copyProperties(question, vo);
        return vo;
    }
}