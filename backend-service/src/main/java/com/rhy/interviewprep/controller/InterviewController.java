package com.rhy.interviewprep.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.rhy.interviewprep.common.Result;
import com.rhy.interviewprep.dto.InterviewQuestionVO;
import com.rhy.interviewprep.dto.InterviewSearchRequest;
import com.rhy.interviewprep.dto.InterviewSearchVO;
import com.rhy.interviewprep.entity.InterviewQuestion;
import com.rhy.interviewprep.service.InterviewImportService;
import com.rhy.interviewprep.service.InterviewQuestionService;
import com.rhy.interviewprep.service.InterviewSearchService;
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
    private final InterviewSearchService interviewSearchService;
    private final InterviewQuestionService interviewQuestionService;

    /**
     * 批量导入面经 MD 文件
     * POST /api/interview/import
     */
    @PostMapping("/import")
    public Result<List<InterviewQuestion>> importFromMd(@RequestParam("file") MultipartFile file) {
        List<InterviewQuestion> imported = interviewImportService.importFromMdFile(file);
        return Result.success(imported);
    }

    /**
     * 向量语义检索
     * POST /api/interview/search
     */
    @PostMapping("/search")
    public Result<List<InterviewSearchVO>> search(@RequestBody InterviewSearchRequest request) {
        List<InterviewSearchVO> results = interviewSearchService.search(request);
        return Result.success(results);
    }

    /**
     * 查找相似题目
     * GET /api/interview/{id}/similar?topK=5&minSimilarity=0.8
     */
    @GetMapping("/{id}/similar")
    public Result<List<InterviewSearchVO>> findSimilar(@PathVariable Long id,
                                                        @RequestParam(defaultValue = "5") int topK,
                                                        @RequestParam(defaultValue = "0.8") double minSimilarity) {
        List<InterviewSearchVO> results = interviewSearchService.findSimilar(id, topK, minSimilarity);
        return Result.success(results);
    }

    /**
     * 面经题目列表查询（分类、难度、公司筛选 + 分页）
     * GET /api/interview/list?page=1&size=10&category=Java基础&difficulty=2&company=京东
     */
    @GetMapping("/list")
    public Result<IPage<InterviewQuestionVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String company) {
        IPage<InterviewQuestionVO> result = interviewQuestionService.list(page, size, category, difficulty, company);
        return Result.success(result);
    }

    /**
     * 面经题目详情
     * GET /api/interview/{id}
     */
    @GetMapping("/{id}")
    public Result<InterviewQuestionVO> getDetail(@PathVariable Long id) {
        InterviewQuestionVO detail = interviewQuestionService.getDetail(id);
        return Result.success(detail);
    }
}