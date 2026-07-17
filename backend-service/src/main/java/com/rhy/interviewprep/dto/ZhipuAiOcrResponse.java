package com.rhy.interviewprep.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 智谱 AI OCR 响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZhipuAiOcrResponse {

    /**
     * 请求 ID
     */
    @JsonProperty("id")
    private String id;

    /**
     * 创建时间
     */
    @JsonProperty("created")
    private Long created;

    /**
     * 模型名称
     */
    @JsonProperty("model")
    private String model;

    /**
     * 选择理由
     */
    @JsonProperty("choices")
    private List<Choice> choices;

    /**
     * 使用情况
     */
    @JsonProperty("usage")
    private Usage usage;

    /**
     * 选择结果
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Choice {

        /**
         * 消息索引
         */
        @JsonProperty("index")
        private Integer index;

        /**
         * 消息内容
         */
        @JsonProperty("message")
        private Message message;

        /**
         * 完成原因
         */
        @JsonProperty("finish_reason")
        private String finishReason;
    }

    /**
     * 消息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {

        /**
         * 角色
         */
        @JsonProperty("role")
        private String role;

        /**
         * 内容
         */
        @JsonProperty("content")
        private String content;
    }

    /**
     * 使用情况
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Usage {

        /**
         * 提示 tokens
         */
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;

        /**
         * 完成 tokens
         */
        @JsonProperty("completion_tokens")
        private Integer completionTokens;

        /**
         * 总 tokens
         */
        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }
}