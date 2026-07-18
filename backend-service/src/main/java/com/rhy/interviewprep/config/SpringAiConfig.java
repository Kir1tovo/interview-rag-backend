package com.rhy.interviewprep.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Spring AI 配置
 * 为 DeepSeek 等大模型调用设置更长的超时时间
 * 学习计划生成等内容较多的场景需要较长超时
 */
@Slf4j
@Configuration
public class SpringAiConfig {

    @Value("${spring.ai.deepseek.read-timeout:180}")
    private int readTimeoutSeconds;

    @Value("${spring.ai.deepseek.connect-timeout:60}")
    private int connectTimeoutSeconds;

    @Bean
    public RestClient.Builder restClientBuilder() {
        log.info("配置 Spring AI RestClient 超时: connectTimeout={}s, readTimeout={}s",
                connectTimeoutSeconds, readTimeoutSeconds);
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(connectTimeoutSeconds));
        factory.setReadTimeout(Duration.ofSeconds(readTimeoutSeconds));
        return RestClient.builder()
                .requestFactory(factory);
    }
}