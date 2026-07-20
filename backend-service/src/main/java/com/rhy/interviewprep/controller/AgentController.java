package com.rhy.interviewprep.controller;

import com.rhy.interviewprep.common.Result;
import com.rhy.interviewprep.dto.AgentMessageRequest;
import com.rhy.interviewprep.dto.AgentMessageResponse;
import com.rhy.interviewprep.service.AgentService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 智能体控制器
 * 提供智能体对话接口，后续可扩展工具调用
 */
@RestController
@RequestMapping("/api/agent")
public class AgentController {

    @Resource
    private AgentService agentService;

    @Value("${agent.upload-dir:./uploads/jd}")
    private String uploadDir;

    /**
     * 发送对话消息
     *
     * @param request 消息请求（message、filePath）
     * @return 对话响应（reply）
     */
    @PostMapping("/message")
    public Result<AgentMessageResponse> sendMessage(@RequestBody AgentMessageRequest request) {
        return agentService.chat(request);
    }

    /**
     * 上传JD图片文件，供Agent后续解析使用
     *
     * @param file JD图片文件
     * @return 文件存储路径
     */
    @PostMapping("/upload-jd")
    public Result<String> uploadJdImage(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error(400, "上传文件不能为空");
        }

        try {
            // 转换为绝对路径，避免相对路径解析到Tomcat临时目录
            Path dirPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // 生成唯一文件名，保留原始扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            // 保存文件（先写字节，避免transferTo的目录问题）
            Path filePath = dirPath.resolve(filename);
            Files.write(filePath, file.getBytes());

            return Result.success(filePath.toString());
        } catch (IOException e) {
            return Result.error(500, "文件上传失败: " + e.getMessage());
        }
    }
}