package com.rhy.interviewprep.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface OcrService {

    /**
     * OCR 识别图片中的文字
     */
    String recognizeText(MultipartFile imageFile) throws IOException;

    /**
     * 批量 OCR 识别多张图片
     */
    String[] batchRecognizeText(MultipartFile[] imageFiles) throws IOException;
}