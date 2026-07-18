package com.rhy.interviewprep.service;

import com.rhy.interviewprep.entity.InterviewQuestion;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 面经题目导入服务
 */
public interface InterviewImportService {

    /**
     * 批量导入面经 MD 文件
     * 解析 MD 文件 → 按 ## 拆分 → 大模型分类 → 生成向量 → 入库（去重）
     *
     * @param file 上传的 MD 文件
     * @return 导入成功的题目列表
     */
    List<InterviewQuestion> importFromMdFile(MultipartFile file);
}