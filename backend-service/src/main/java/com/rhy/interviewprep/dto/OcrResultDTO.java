package com.rhy.interviewprep.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OCR 识别结果 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcrResultDTO {

    /**
     * 识别出的文字内容
     */
    private String text;

    /**
     * 识别是否成功
     */
    private Boolean success;

    /**
     * 错误信息（如果识别失败）
     */
    private String errorMessage;

    /**
     * 创建成功结果
     *
     * @param text 识别的文字
     * @return 结果 DTO
     */
    public static OcrResultDTO success(String text) {
        return new OcrResultDTO(text, true, null);
    }

    /**
     * 创建失败结果
     *
     * @param errorMessage 错误信息
     * @return 结果 DTO
     */
    public static OcrResultDTO failure(String errorMessage) {
        return new OcrResultDTO(null, false, errorMessage);
    }
}