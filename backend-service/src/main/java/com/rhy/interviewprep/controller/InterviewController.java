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
 * 提供面经题目导入、向量语义检索、列表查询、详情查看、相似推荐等接口
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
     * 解析 MD 文件（按 ## 二级标题拆分）→ 大模型分类 → 生成 Embedding 向量 → 去重入库
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
     * 将查询文本通过 Embedding 模型转为向量，使用 pgvector 余弦距离检索最相关的面经题目
     *
     * @param request 检索请求（query: 查询文本, topK: 返回数量, minSimilarity: 最低相似度阈值）
     * @return 检索结果列表（含相似度分数）
     */
    @PostMapping("/search")
    public Result<List<InterviewSearchVO>> search(@RequestBody InterviewSearchRequest request) {
        List<InterviewSearchVO> results = interviewSearchService.search(request);
        return Result.success(results);
    }

    /**
     * 查找相似题目
     * 基于指定题目的 Embedding 向量，通过 pgvector 检索语义相似的其他题目
     *
     * @param id           目标题目 ID
     * @param topK         返回数量，默认 5
     * @param minSimilarity 最低相似度阈值，默认 0.8
     * @return 相似题目列表（含相似度分数）
     */
    @GetMapping("/{id}/similar")
    public Result<List<InterviewSearchVO>> findSimilar(@PathVariable Long id,
                                                        @RequestParam(defaultValue = "5") int topK,
                                                        @RequestParam(defaultValue = "0.8") double minSimilarity) {
        List<InterviewSearchVO> results = interviewSearchService.findSimilar(id, topK, minSimilarity);
        return Result.success(results);
    }

    /**
     * 面经题目列表查询（支持技术分类、难度、公司筛选 + 分页）
     *
     * @param page      页码（从 1 开始），默认 1
     * @param size      每页条数，默认 10
     * @param category  技术分类（可选，如 "Java基础"、"Spring"）
     * @param difficulty 难度等级（可选，1-简单 2-中等 3-困难）
     * @param company   来源公司（可选，如 "京东"）
     * @return 分页题目列表（不含 embedding 字段）
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
     *
     * @param id 题目 ID
     * @return 题目详情（不含 embedding 字段）
     */
    @GetMapping("/{id}")
    public Result<InterviewQuestionVO> getDetail(@PathVariable Long id) {
        InterviewQuestionVO detail = interviewQuestionService.getDetail(id);
        return Result.success(detail);
    }
}