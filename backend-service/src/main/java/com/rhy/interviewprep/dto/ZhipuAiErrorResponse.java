package com.rhy.interviewprep.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 智谱 AI 错误响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZhipuAiErrorResponse {

    /**
     * 错误对象
     */
    @JsonProperty("error")
    private Error error;

    /**
     * 错误详情
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Error {

        /**
         * 错误代码
         */
        @JsonProperty("code")
        private String code;

        /**
         * 错误消息
         */
        @JsonProperty("message")
        private String message;

        /**
         * 错误类型
         */
        @JsonProperty("type")
        private String type;
    }
}