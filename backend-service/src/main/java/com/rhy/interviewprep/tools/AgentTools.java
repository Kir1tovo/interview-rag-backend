package com.rhy.interviewprep.tools;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.rhy.interviewprep.dto.InterviewQuestionVO;
import com.rhy.interviewprep.dto.InterviewSearchRequest;
import com.rhy.interviewprep.dto.InterviewSearchVO;
import com.rhy.interviewprep.service.InterviewQuestionService;
import com.rhy.interviewprep.service.InterviewSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Agent 本地工具集
 * 使用 Spring AI @Tool 注解，让 ReactAgent 可以直接调用业务服务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentTools {

    private final InterviewQuestionService interviewQuestionService;
    private final InterviewSearchService interviewSearchService;

    @Tool(description = "按条件筛选面经题目。支持按技术分类、难度等级、来源公司进行筛选，返回分页结果。" +
            "当用户想浏览特定类别的面试题时使用此工具，例如'查看Java题目'、'中等难度的题目'、'字节跳动的面经'等。")
    public String searchInterviewByKeyword(
            @ToolParam(description = "页码，从1开始，默认1") int page,
            @ToolParam(description = "每页条数，默认5") int size,
            @ToolParam(description = "技术分类，如 Java、MySQL、Redis、Spring、计算机网络 等，可选") String category,
            @ToolParam(description = "难度等级：1-简单 2-中等 3-困难，可选") Integer difficulty,
            @ToolParam(description = "来源公司名称，如 字节跳动、阿里巴巴、腾讯 等，可选") String company
    ) {
        log.info("Agent调用面经条件查询: page={}, size={}, category={}, difficulty={}, company={}",
                page, size, category, difficulty, company);

        int p = Math.max(page, 1);
        int s = Math.max(Math.min(size, 20), 1);

        IPage<InterviewQuestionVO> result = interviewQuestionService.list(p, s, category, difficulty, company);

        if (result.getRecords().isEmpty()) {
            return "未找到符合条件的面经题目。请尝试调整筛选条件。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("找到 %d 条面经题目（第%d页，共%d页）：\n\n",
                result.getTotal(), result.getCurrent(), result.getPages()));

        for (InterviewQuestionVO q : result.getRecords()) {
            sb.append(String.format("【ID:%d】%s\n", q.getId(), q.getQuestion()));
            sb.append(String.format("  分类: %s | 难度: %s | 公司: %s\n",
                    q.getCategory(),
                    difficultyLabel(q.getDifficulty()),
                    q.getCompany() != null ? q.getCompany() : "未知"));
            if (q.getAnswer() != null && !q.getAnswer().isEmpty()) {
                String answerPreview = q.getAnswer().length() > 150
                        ? q.getAnswer().substring(0, 150) + "..."
                        : q.getAnswer();
                sb.append(String.format("  答案: %s\n", answerPreview));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    @Tool(description = "语义搜索面经题目。根据自然语言描述进行向量相似度检索，返回最相关的面试题。" +
            "当用户用自然语言提问或描述问题时使用此工具，例如'HashMap底层原理'、'TCP三次握手'、'Redis持久化方式'等。" +
            "比关键词筛选更智能，能理解语义相似的问题。")
    public String searchInterviewBySemantic(
            @ToolParam(description = "搜索文本，自然语言问题或关键词描述") String query,
            @ToolParam(description = "返回结果数量，默认5，最大10") Integer topK,
            @ToolParam(description = "最低相似度阈值(0~1)，低于此值的结果被过滤，默认0.5") Double minSimilarity
    ) {
        log.info("Agent调用面经语义搜索: query={}, topK={}, minSimilarity={}", query, topK, minSimilarity);

        InterviewSearchRequest request = new InterviewSearchRequest();
        request.setQuery(query);
        request.setTopK(topK != null ? Math.min(topK, 10) : 5);
        request.setMinSimilarity(minSimilarity != null ? minSimilarity : 0.5);

        List<InterviewSearchVO> results = interviewSearchService.search(request);

        if (results.isEmpty()) {
            return "未找到与 \"" + query + "\" 相关的面经题目。请尝试换一种描述方式。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("语义搜索 \"%s\" 找到 %d 条相关题目：\n\n", query, results.size()));

        for (InterviewSearchVO r : results) {
            sb.append(String.format("【ID:%d】%s (相似度: %.2f)\n", r.getId(), r.getQuestion(), r.getSimilarity()));
            sb.append(String.format("  分类: %s | 难度: %s | 公司: %s\n",
                    r.getCategory(),
                    difficultyLabel(r.getDifficulty()),
                    r.getCompany() != null ? r.getCompany() : "未知"));
            if (r.getAnswer() != null && !r.getAnswer().isEmpty()) {
                String answerPreview = r.getAnswer().length() > 150
                        ? r.getAnswer().substring(0, 150) + "..."
                        : r.getAnswer();
                sb.append(String.format("  答案: %s\n", answerPreview));
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private String difficultyLabel(Integer difficulty) {
        if (difficulty == null) return "未知";
        return switch (difficulty) {
            case 1 -> "简单";
            case 2 -> "中等";
            case 3 -> "困难";
            default -> "未知";
        };
    }
}