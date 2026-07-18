package com.rhy.interviewprep.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 智谱 AI 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "zhipu.ai")
public class ZhipuAiProperties {

    /**
     * API Key
     */
    private String apiKey;

    /**
     * API 基础 URL
     */
    private String baseUrl = "https://open.bigmodel.cn/api/paas/v4/chat/completions";

    /**
     * OCR 模型名称
     */
    private String ocrModel = "glm-4v";

    /**
     * 超时时间（毫秒）
     */
    private Integer timeout = 180000;
}