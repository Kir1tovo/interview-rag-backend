package com.rhy.interviewprep.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhy.interviewprep.common.BusinessException;
import com.rhy.interviewprep.common.ErrorCode;
import com.rhy.interviewprep.constants.JdParsingPrompt;
import com.rhy.interviewprep.dto.JdParsedResult;
import com.rhy.interviewprep.entity.JobDescription;
import com.rhy.interviewprep.mapper.JobDescriptionMapper;
import com.rhy.interviewprep.service.JdService;
import com.rhy.interviewprep.service.OcrService;
import com.rhy.interviewprep.utils.ImageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

/**
 * JD 解析服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JdServiceImpl implements JdService {

    private final OcrService ocrService;
    private final JobDescriptionMapper jobDescriptionMapper;
    private final ChatModel chatModel;
    private final ObjectMapper objectMapper;

    @Override
    public JobDescription parseFromImage(MultipartFile file, Long userId) {
        // 1. 验证图片
        try {
            ImageUtils.validateImage(file);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.OCR_IMAGE_INVALID, e.getMessage());
        }

        // 2. OCR 识别图片文本
        String rawText;
        try {
            rawText = ocrService.recognizeText(file);
        } catch (IOException e) {
            log.error("OCR 识别失败", e);
            throw new BusinessException(ErrorCode.OCR_RECOGNITION_ERROR, "OCR 识别失败: " + e.getMessage());
        }

        if (rawText == null || rawText.isBlank()) {
            throw new BusinessException(ErrorCode.OCR_RECOGNITION_ERROR, "OCR 识别结果为空，请上传清晰的 JD 图片");
        }

        // 3. 校验识别文本长度
        if (rawText.length() < 50) {
            throw new BusinessException(ErrorCode.OCR_RECOGNITION_ERROR, "识别文本过短（少于50字），请上传更完整的 JD 图片");
        }

        log.info("OCR 识别完成，文本长度: {} 字符", rawText.length());

        // 4. 调用 DeepSeek 解析结构化信息
        JdParsedResult parsedResult = parseJdText(rawText);

        // 5. 构建实体并保存
        JobDescription jd = buildJobDescription(parsedResult, rawText, userId);
        jobDescriptionMapper.insert(jd);

        log.info("JD 解析并保存成功，ID: {}, 公司: {}, 岗位: {}", jd.getId(), jd.getCompany(), jd.getPosition());

        return jd;
    }

    /**
     * 调用 DeepSeek 大模型解析 JD 文本
     */
    private JdParsedResult parseJdText(String rawText) {
        try {
            String userPrompt = String.format(JdParsingPrompt.USER_PROMPT_TEMPLATE, rawText);

            List<Message> messages = List.of(
                    new SystemMessage(JdParsingPrompt.SYSTEM_PROMPT),
                    new UserMessage(userPrompt)
            );

            Prompt prompt = new Prompt(messages);
            ChatResponse chatResponse = chatModel.call(prompt);

            String response = chatResponse.getResult().getOutput().getText();

            log.debug("DeepSeek 原始响应: {}", response);

            // 清理响应中可能存在的 markdown 代码块标记
            String json = response.trim();
            if (json.startsWith("```json")) {
                json = json.substring(7);
            } else if (json.startsWith("```")) {
                json = json.substring(3);
            }
            if (json.endsWith("```")) {
                json = json.substring(0, json.length() - 3);
            }
            json = json.trim();

            return objectMapper.readValue(json, JdParsedResult.class);

        } catch (JsonProcessingException e) {
            log.error("解析 DeepSeek 响应失败", e);
            throw new BusinessException(ErrorCode.JD_PARSE_ERROR, "JD 解析结果格式错误");
        } catch (Exception e) {
            log.error("调用 DeepSeek 解析 JD 失败", e);
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "AI 服务调用失败: " + e.getMessage());
        }
    }

    /**
     * 将解析结果转换为 JobDescription 实体
     */
    private JobDescription buildJobDescription(JdParsedResult parsedResult, String rawText, Long userId) {
        JobDescription jd = new JobDescription();
        jd.setUserId(userId);
        jd.setCompany(parsedResult.getCompany());
        jd.setDepartment(parsedResult.getDepartment());
        jd.setPosition(parsedResult.getPosition());
        jd.setLocation(parsedResult.getLocation());
        jd.setEducation(parsedResult.getEducation());
        jd.setExperience(parsedResult.getExperience());
        jd.setSalary(parsedResult.getSalary());
        jd.setRawText(rawText);
        jd.setResponsibilities(parsedResult.getResponsibilities());
        jd.setCreatedAt(LocalDateTime.now());

        // 将技术栈和软技能序列化为 JSON
        try {
            if (parsedResult.getRequirements() != null) {
                jd.setRequirementsJson(objectMapper.writeValueAsString(parsedResult.getRequirements()));
            }
            if (parsedResult.getSoftSkills() != null) {
                jd.setSoftSkillsJson(objectMapper.writeValueAsString(parsedResult.getSoftSkills()));
            }
        } catch (JsonProcessingException e) {
            log.warn("序列化技术栈/软技能失败", e);
        }

        return jd;
    }

    @Override
    public JobDescription parseFromFilePath(String filePath, Long userId) {
        Path path = Paths.get(filePath);
        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new BusinessException(ErrorCode.OCR_IMAGE_INVALID, "文件不存在或不是有效文件: " + filePath);
        }

        try {
            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            byte[] content = Files.readAllBytes(path);
            String originalFilename = path.getFileName().toString();

            MultipartFile multipartFile = new PathMultipartFile(path, contentType, content);

            JobDescription jd = parseFromImage(multipartFile, userId);

            // 解析成功后删除临时图片文件（数据已存入数据库，图片不再需要）
            try {
                Files.deleteIfExists(path);
                log.info("临时图片已清理: {}", filePath);
            } catch (IOException e) {
                log.warn("临时图片清理失败（不影响业务）: {}", filePath, e);
            }

            return jd;

        } catch (IOException e) {
            log.error("读取文件失败: {}", filePath, e);
            throw new BusinessException(ErrorCode.OCR_IMAGE_INVALID, "读取文件失败: " + e.getMessage());
        }
    }

    @Override
    public JobDescription getById(Long id, Long userId) {
        JobDescription jd = jobDescriptionMapper.selectById(id);
        if (jd == null || !jd.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.JD_NOT_FOUND);
        }
        return jd;
    }

    @Override
    public IPage<JobDescription> listByUserId(Long userId, int page, int size) {
        Page<JobDescription> pageObj = new Page<>(page, size);
        return jobDescriptionMapper.selectPage(pageObj,
                new LambdaQueryWrapper<JobDescription>()
                        .eq(JobDescription::getUserId, userId)
                        .orderByDesc(JobDescription::getCreatedAt)
        );
    }

    @Override
    public void deleteById(Long id, Long userId) {
        JobDescription jd = jobDescriptionMapper.selectById(id);
        if (jd == null || !jd.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.JD_NOT_FOUND);
        }
        jobDescriptionMapper.deleteById(id);
        log.info("JD 记录已删除，ID: {}", id);
    }

    /**
     * 基于 Path 的 MultipartFile 简单实现，用于从文件路径构造 MultipartFile
     */
    private static class PathMultipartFile implements MultipartFile {

        private final Path path;
        private final String contentType;
        private final byte[] content;

        PathMultipartFile(Path path, String contentType, byte[] content) {
            this.path = path;
            this.contentType = contentType;
            this.content = content;
        }

        @Override
        public String getName() {
            return "file";
        }

        @Override
        public String getOriginalFilename() {
            return path.getFileName().toString();
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content == null || content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(java.io.File dest) throws IOException {
            Files.write(dest.toPath(), content);
        }
    }
}