package com.rhy.interviewprep.controller;

import com.rhy.interviewprep.common.Result;
import com.rhy.interviewprep.dto.OcrResultDTO;
import com.rhy.interviewprep.service.OcrService;
import com.rhy.interviewprep.utils.ImageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * OCR 文字识别控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ocr")
public class OcrController {

    @Autowired
    private OcrService ocrService;

    /**
     * OCR 识别图片文字
     *
     * @param file 上传的图片文件
     * @return 识别结果
     */
    @PostMapping("/recognize")
    public Result<OcrResultDTO> recognize(@RequestParam("file") MultipartFile file) {
        try {
            // 验证图片文件
            ImageUtils.validateImage(file);

            log.info("收到 OCR 识别请求，文件名: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());

            // 调用 OCR 服务
            String recognizedText = ocrService.recognizeText(file);

            // 返回识别结果
            OcrResultDTO result = OcrResultDTO.success(recognizedText);
            return Result.success(result);

        } catch (IllegalArgumentException e) {
            log.warn("OCR 请求参数错误: {}", e.getMessage());
            return Result.error(400, e.getMessage());
        } catch (IOException e) {
            log.error("OCR 识别时读取文件失败", e);
            return Result.error(500, "读取图片失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("OCR 识别失败", e);
            return Result.error(500, "OCR 识别失败: " + e.getMessage());
        }
    }
}