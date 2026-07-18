package com.rhy.interviewprep.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Configuration;

/**
 * Embedding 模型配置
 * 使用 Spring AI OpenAI Embedding 模型，指向千问/DashScope 兼容端点
 * 配置项在 application.yml 的 spring.ai.openai.embedding 下
 */
@Slf4j
@Configuration
public class EmbeddingConfig {

    private final EmbeddingModel embeddingModel;

    public EmbeddingConfig(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
        log.info("EmbeddingModel 已注入: {}", embeddingModel.getClass().getSimpleName());
    }
}