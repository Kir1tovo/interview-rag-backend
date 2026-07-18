package com.rhy.interviewprep.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rhy.interviewprep.entity.InterviewQuestion;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InterviewQuestionMapper extends BaseMapper<InterviewQuestion> {

}