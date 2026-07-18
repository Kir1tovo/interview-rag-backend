package com.rhy.interviewprep.controller;

import com.rhy.interviewprep.common.Result;
import com.rhy.interviewprep.entity.InterviewQuestion;
import com.rhy.interviewprep.service.InterviewImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 面经知识库控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewImportService interviewImportService;

    /**
     * 批量导入面经 MD 文件
     * POST /api/interview/import
     *
     * @param file 上传的 MD 文件（文件名格式：公司名.md，如 "京东.md"）
     * @return 导入成功的题目列表
     */
    @PostMapping("/import")
    public Result<List<InterviewQuestion>> importFromMd(@RequestParam("file") MultipartFile file) {
        List<InterviewQuestion> imported = interviewImportService.importFromMdFile(file);
        return Result.success(imported);
    }
}