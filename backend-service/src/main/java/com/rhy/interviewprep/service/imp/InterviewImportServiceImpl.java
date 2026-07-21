package com.rhy.interviewprep.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhy.interviewprep.common.BusinessException;
import com.rhy.interviewprep.common.ErrorCode;
import com.rhy.interviewprep.constants.InterviewImportPrompt;
import com.rhy.interviewprep.entity.InterviewQuestion;
import com.rhy.interviewprep.mapper.InterviewQuestionMapper;
import com.rhy.interviewprep.service.InterviewImportService;
import com.rhy.interviewprep.service.InterviewSearchCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 面经题目导入服务实现
 * 流程：读取MD文件 → 按##拆分 → 大模型分类 → 生成Embedding → 去重入库
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewImportServiceImpl implements InterviewImportService {

    private final InterviewQuestionMapper interviewQuestionMapper;
    private final ChatModel chatModel;
    private final EmbeddingModel embeddingModel;
    private final ObjectMapper objectMapper;
    private final InterviewSearchCacheService searchCacheService;

    @Override
    public List<InterviewQuestion> importFromMdFile(MultipartFile file) {
        // 1. 验证文件
        validateFile(file);

        // 2. 从文件名提取公司名
        String company = extractCompanyFromFilename(file.getOriginalFilename());

        // 3. 读取文件内容
        String content = readFileContent(file);

        // 4. 按 ## 拆分为题目块
        List<String> chunks = splitByH2(content);
        if (chunks.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "MD文件中未找到以 ## 开头的题目");
        }

        log.info("MD文件拆分为 {} 个题目块，公司：{}", chunks.size(), company);

        // 5. 逐条处理：分类 + 生成向量 + 去重入库
        List<InterviewQuestion> imported = new ArrayList<>();
        int skipped = 0;

        for (String chunk : chunks) {
            try {
                // 解析 question 和 answer
                String[] lines = chunk.split("\n", 2);
                String question = lines[0].replaceFirst("^##\\s*", "").trim();
                String answer = lines.length > 1 ? lines[1].trim() : "";

                if (question.isEmpty()) {
                    continue;
                }

                // 去重：按 question 文本查重
                if (isDuplicate(question)) {
                    skipped++;
                    log.debug("跳过重复题目：{}", question);
                    continue;
                }

                // 大模型判定 category 和 difficulty
                Map<String, Object> classification = classifyQuestion(question, answer);
                String category = (String) classification.getOrDefault("category", "其他");
                Integer difficulty = (Integer) classification.getOrDefault("difficulty", 2);

                // 生成 Embedding 向量
                float[] embedding = generateEmbedding(question);

                // 构建实体
                InterviewQuestion iq = new InterviewQuestion();
                iq.setQuestion(question);
                iq.setAnswer(answer);
                iq.setAnalysis("");  // 解析字段暂空，后续可扩展
                iq.setCategory(category);
                iq.setDifficulty(difficulty);
                iq.setCompany(company);
                iq.setEmbedding(embedding);
                iq.setCreatedAt(LocalDateTime.now());

                // 入库
                interviewQuestionMapper.insert(iq);
                imported.add(iq);

            } catch (Exception e) {
                log.error("处理题目块失败：{}", chunk.substring(0, Math.min(50, chunk.length())), e);
            }
        }

        log.info("导入完成：成功 {} 条，跳过重复 {} 条，公司：{}", imported.size(), skipped, company);

        // 导入新数据后清除搜索缓存，确保缓存一致性
        if (!imported.isEmpty()) {
            searchCacheService.clearAll();
        }

        return imported;
    }

    /**
     * 验证上传文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件不能为空");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".md")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "仅支持 .md 文件");
        }
        // 限制文件大小 5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件大小不能超过5MB");
        }
    }

    /**
     * 从文件名提取公司名
     * 例如 "京东.md" → "京东"，"阿里巴巴面经.md" → "阿里巴巴面经"
     */
    private String extractCompanyFromFilename(String filename) {
        if (filename == null) {
            return "";
        }
        // 去掉 .md 后缀
        String name = filename.replaceAll("(?i)\\.md$", "").trim();
        // 去掉常见的后缀词
        name = name.replaceAll("(面经|面试|题目|题库)$", "").trim();
        return name.isEmpty() ? filename : name;
    }

    /**
     * 读取文件内容
     */
    private String readFileContent(MultipartFile file) {
        try {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文件读取失败");
        }
    }

    /**
     * 按 ## 二级标题拆分 MD 内容
     * 每个块以 ## 开头，包含标题行和后续内容
     */
    private List<String> splitByH2(String content) {
        List<String> chunks = new ArrayList<>();
        String[] lines = content.split("\n");
        StringBuilder currentChunk = null;

        for (String line : lines) {
            if (line.startsWith("## ") && !line.startsWith("### ")) {
                // 遇到新的二级标题，保存上一个块并开始新块
                if (currentChunk != null && currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                }
                currentChunk = new StringBuilder(line);
            } else if (currentChunk != null) {
                currentChunk.append("\n").append(line);
            }
            // 忽略 ## 之前的内容（文件头部说明等）
        }

        // 添加最后一个块
        if (currentChunk != null && currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    /**
     * 去重检查：按 question 文本查询是否已存在
     */
    private boolean isDuplicate(String question) {
        LambdaQueryWrapper<InterviewQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InterviewQuestion::getQuestion, question);
        return interviewQuestionMapper.selectCount(wrapper) > 0;
    }

    /**
     * 调用 DeepSeek 大模型判定 category 和 difficulty
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> classifyQuestion(String question, String answer) {
        try {
            String userPrompt = String.format(InterviewImportPrompt.USER_PROMPT_TEMPLATE, question);
            List<Message> messages = List.of(
                    new SystemMessage(InterviewImportPrompt.SYSTEM_PROMPT),
                    new UserMessage(userPrompt)
            );
            Prompt prompt = new Prompt(messages);
            ChatResponse chatResponse = chatModel.call(prompt);
            String response = chatResponse.getResult().getOutput().getText();

            // 清理 markdown 代码块标记
            response = cleanMarkdownCodeBlock(response);

            return objectMapper.readValue(response, Map.class);
        } catch (Exception e) {
            log.warn("大模型分类失败，使用默认值。题目：{}，错误：{}", question, e.getMessage());
            return Map.of("category", "其他", "difficulty", 2);
        }
    }

    /**
     * 清理 markdown 代码块标记
     */
    private String cleanMarkdownCodeBlock(String text) {
        if (text == null) return "";
        return text.replaceAll("^```(?:json)?\\s*", "")
                    .replaceAll("\\s*```$", "")
                    .trim();
    }

    /**
     * 生成 Embedding 向量
     */
    private float[] generateEmbedding(String text) {
        try {
            var embedding = embeddingModel.embed(text);
            if (embedding != null) {
                return embedding;
            }
        } catch (Exception e) {
            log.error("生成Embedding向量失败：{}", text.substring(0, Math.min(50, text.length())), e);
        }
        return null;
    }
}