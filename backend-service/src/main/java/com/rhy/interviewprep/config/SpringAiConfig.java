package com.rhy.interviewprep.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Spring AI 配置
 * 为 DeepSeek 等大模型调用设置更长的超时时间
 * 修复 DashScope Embedding API 返回 content-type: application/octet-stream 的问题
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
                .requestFactory(factory)
                .messageConverters(converters -> {
                    // 修复 DashScope Embedding API 返回 content-type: application/octet-stream 而非 application/json
                    // 让 Jackson converter 也处理 octet-stream 响应，避免反序列化失败
                    converters.stream()
                            .filter(c -> c instanceof MappingJackson2HttpMessageConverter)
                            .map(c -> (MappingJackson2HttpMessageConverter) c)
                            .forEach(c -> {
                                List<MediaType> mediaTypes = new ArrayList<>(c.getSupportedMediaTypes());
                                if (!mediaTypes.contains(MediaType.APPLICATION_OCTET_STREAM)) {
                                    mediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
                                    c.setSupportedMediaTypes(mediaTypes);
                                    log.info("已为 MappingJackson2HttpMessageConverter 添加 application/octet-stream 支持");
                                }
                            });
                });
    }
}