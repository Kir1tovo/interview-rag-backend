package com.rhy.interviewprep.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 智谱 AI OCR 请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZhipuAiOcrRequest {

    /**
     * 模型名称
     */
    @JsonProperty("model")
    private String model;

    /**
     * 消息列表
     */
    @JsonProperty("messages")
    private List<Message> messages;

    /**
     * 温度参数
     */
    @JsonProperty("temperature")
    private Double temperature = 0.3;

    /**
     * 最大生成 tokens
     */
    @JsonProperty("max_tokens")
    private Integer maxTokens = 1024;

    /**
     * 消息对象
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {

        /**
         * 角色（system, user, assistant）
         */
        @JsonProperty("role")
        private String role;

        /**
         * 消息内容数组（智谱 GLM-4V 要求 content 为数组）
         */
        @JsonProperty("content")
        private List<ContentPart> content;
    }

    /**
     * 消息内容部分（文本或图片）
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ContentPart {

        /**
         * 类型：text 或 image_url
         */
        @JsonProperty("type")
        private String type;

        /**
         * 文本内容（type=text 时使用）
         */
        @JsonProperty("text")
        private String text;

        /**
         * 图片 URL（type=image_url 时使用）
         */
        @JsonProperty("image_url")
        private ImageUrl imageUrl;
    }

    /**
     * 图片 URL
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageUrl {

        /**
         * 图片 URL（支持 data URL 格式）
         */
        @JsonProperty("url")
        private String url;

        /**
         * 图片详情级别（low, high, auto）
         */
        @JsonProperty("detail")
        private String detail = "high";
    }
}