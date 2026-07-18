package com.rhy.interviewprep.controller;

import com.rhy.interviewprep.common.Result;
import com.rhy.interviewprep.dto.InterviewSearchRequest;
import com.rhy.interviewprep.dto.InterviewSearchVO;
import com.rhy.interviewprep.entity.InterviewQuestion;
import com.rhy.interviewprep.service.InterviewImportService;
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

    /**
     * 向量语义检索
     * POST /api/interview/search
     * 将查询文本转为向量，通过 pgvector 余弦相似度检索最相关的面经题目
     *
     * @param request 检索请求（query, topK, minSimilarity）
     * @return 检索结果列表（含相似度分数）
     */
    @PostMapping("/search")
    public Result<List<InterviewSearchVO>> search(@RequestBody InterviewSearchRequest request) {
        List<InterviewSearchVO> results = interviewSearchService.search(request);
        return Result.success(results);
    }

    /**
     * 查找相似题目
     * GET /api/interview/{id}/similar?topK=5&minSimilarity=0.5
     *
     * @param id           目标题目ID
     * @param topK         返回数量，默认5
     * @param minSimilarity 最低相似度阈值，默认0.5
     * @return 相似题目列表
     */
    @GetMapping("/{id}/similar")
    public Result<List<InterviewSearchVO>> findSimilar(@PathVariable Long id,
                                                        @RequestParam(defaultValue = "5") int topK,
                                                        @RequestParam(defaultValue = "0.8") double minSimilarity) {
        List<InterviewSearchVO> results = interviewSearchService.findSimilar(id, topK, minSimilarity);
        return Result.success(results);
    }
}