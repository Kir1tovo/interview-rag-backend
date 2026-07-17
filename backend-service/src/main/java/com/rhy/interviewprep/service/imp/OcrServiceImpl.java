package com.rhy.interviewprep.service.imp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhy.interviewprep.config.ZhipuAiProperties;
import com.rhy.interviewprep.dto.ZhipuAiErrorResponse;
import com.rhy.interviewprep.dto.ZhipuAiOcrRequest;
import com.rhy.interviewprep.dto.ZhipuAiOcrResponse;
import com.rhy.interviewprep.service.OcrService;
import com.rhy.interviewprep.utils.ImageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class OcrServiceImpl implements OcrService {

    @Autowired
    private ZhipuAiProperties zhipuAiProperties;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public String recognizeText(MultipartFile imageFile) throws IOException {
        log.info("开始 OCR 识别，文件名: {}, 大小: {} bytes", imageFile.getOriginalFilename(), imageFile.getSize());

        try {
            ImageUtils.validateImage(imageFile);

            String dataUrl = ImageUtils.imageToBase64WithPrefix(imageFile);

            return recognizeTextFromDataUrl(dataUrl);

        } catch (Exception e) {
            log.error("OCR 识别失败", e);
            throw new RuntimeException("OCR 识别失败: " + e.getMessage(), e);
        }
    }

    private String recognizeTextFromDataUrl(String dataUrl) {
        try {
            ZhipuAiOcrRequest request = buildOcrRequest(dataUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(zhipuAiProperties.getApiKey());

            HttpEntity<ZhipuAiOcrRequest> httpEntity = new HttpEntity<>(request, headers);

            log.debug("调用智谱 AI OCR 模型进行识别，URL: {}", zhipuAiProperties.getBaseUrl());
            ResponseEntity<String> response = restTemplate.exchange(
                    zhipuAiProperties.getBaseUrl(),
                    HttpMethod.POST,
                    httpEntity,
                    String.class
            );

            String responseBody = response.getBody();
            log.debug("智谱 AI 响应: {}", responseBody);

            if (response.getStatusCode() == HttpStatus.OK && responseBody != null) {
                try {
                    ZhipuAiOcrResponse ocrResponse = objectMapper.readValue(responseBody, ZhipuAiOcrResponse.class);
                    if (ocrResponse.getChoices() != null && !ocrResponse.getChoices().isEmpty()) {
                        String recognizedText = ocrResponse.getChoices().get(0).getMessage().getContent();
                        log.info("OCR 识别完成，识别文字长度: {} 字符", recognizedText != null ? recognizedText.length() : 0);
                        return recognizedText;
                    }
                } catch (Exception e) {
                    log.warn("解析成功响应失败，尝试解析为错误响应", e);
                }

                try {
                    ZhipuAiErrorResponse errorResponse = objectMapper.readValue(responseBody, ZhipuAiErrorResponse.class);
                    if (errorResponse.getError() != null) {
                        throw new RuntimeException("智谱 AI 错误: " + errorResponse.getError().getMessage());
                    }
                } catch (Exception e) {
                    log.warn("解析错误响应失败", e);
                }

                log.warn("无法解析响应，返回原始内容");
                return responseBody;
            } else {
                throw new RuntimeException("智谱 AI 调用失败，HTTP 状态码: " + response.getStatusCode());
            }

        } catch (RestClientException e) {
            log.error("调用智谱 AI OCR 模型失败", e);
            throw new RuntimeException("调用智谱 AI 失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("OCR 识别失败", e);
            throw new RuntimeException("OCR 识别失败: " + e.getMessage(), e);
        }
    }

    private ZhipuAiOcrRequest buildOcrRequest(String dataUrl) {
        String systemPrompt = "请识别图片中的所有文字内容。要求：1. 保持原文的格式和换行；2. 准确识别所有文字，包括中文、英文、数字和标点符号；3. 只返回识别的文字，不要添加任何解释或额外内容。";

        List<ZhipuAiOcrRequest.ContentPart> contentParts = List.of(
                new ZhipuAiOcrRequest.ContentPart("text", systemPrompt, null),
                new ZhipuAiOcrRequest.ContentPart("image_url", null,
                        new ZhipuAiOcrRequest.ImageUrl(dataUrl, "high"))
        );
        ZhipuAiOcrRequest.Message message = new ZhipuAiOcrRequest.Message("user", contentParts);

        return new ZhipuAiOcrRequest(
                zhipuAiProperties.getOcrModel(),
                Collections.singletonList(message),
                0.3,
                1024
        );
    }

    @Override
    public String[] batchRecognizeText(MultipartFile[] imageFiles) throws IOException {
        if (imageFiles == null || imageFiles.length == 0) {
            return new String[0];
        }

        log.info("开始批量 OCR 识别，图片数量: {}", imageFiles.length);

        String[] results = new String[imageFiles.length];
        for (int i = 0; i < imageFiles.length; i++) {
            results[i] = recognizeText(imageFiles[i]);
        }

        log.info("批量 OCR 识别完成");
        return results;
    }
}