package com.rhy.interviewprep.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

/**
 * 图片处理工具类
 * 用于处理图片上传、Base64 编码解码等操作
 */
@Slf4j
public class ImageUtils {

    /**
     * 支持的图片格式
     */
    private static final String[] ALLOWED_IMAGE_TYPES = {
            "image/jpeg",
            "image/png",
            "image/jpg",
            "image/webp",
            "image/bmp",
            "application/pdf"
    };

    /**
     * 图片大小限制（10MB）
     */
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;

    /**
     * 验证图片文件
     *
     * @param file 上传的文件
     * @return 是否为有效图片
     * @throws IllegalArgumentException 如果文件无效
     */
    public static void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        // 检查文件大小
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("图片大小不能超过 10MB");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (!isValidImageType(contentType)) {
            throw new IllegalArgumentException("不支持的图片格式，仅支持 JPG、PNG、BMP、PDF");
        }
    }

    /**
     * 检查是否为支持的图片类型
     *
     * @param contentType MIME 类型
     * @return 是否支持
     */
    public static boolean isValidImageType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return false;
        }
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (allowedType.equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 将图片文件转换为 Base64 编码字符串
     *
     * @param file 图片文件
     * @return Base64 编码字符串（不包含 data:image/xxx;base64, 前缀）
     * @throws IOException 如果读取文件失败
     */
    public static String imageToBase64(MultipartFile file) throws IOException {
        byte[] bytes = file.getBytes();
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 将图片文件转换为带前缀的 Base64 编码字符串
     * 格式: data:image/xxx;base64,xxx
     *
     * @param file 图片文件
     * @return 带前缀的 Base64 编码字符串
     * @throws IOException 如果读取文件失败
     */
    public static String imageToBase64WithPrefix(MultipartFile file) throws IOException {
        String base64 = imageToBase64(file);
        String contentType = file.getContentType();
        return String.format("data:%s;base64,%s", contentType, base64);
    }

    /**
     * 将 Base64 编码字符串转换为字节数组
     *
     * @param base64 Base64 编码字符串
     * @return 字节数组
     */
    public static byte[] base64ToBytes(String base64) {
        if (!StringUtils.hasText(base64)) {
            throw new IllegalArgumentException("Base64 字符串不能为空");
        }

        // 移除可能存在的 data:image/xxx;base64, 前缀
        String cleanBase64 = base64;
        if (base64.contains(",")) {
            cleanBase64 = base64.substring(base64.indexOf(",") + 1);
        }

        return Base64.getDecoder().decode(cleanBase64);
    }

    /**
     * 从带前缀的 Base64 字符串中提取纯 Base64 编码
     *
     * @param base64WithPrefix 带前缀的 Base64 字符串
     * @return 纯 Base64 编码字符串
     */
    public static String extractBase64(String base64WithPrefix) {
        if (!StringUtils.hasText(base64WithPrefix)) {
            throw new IllegalArgumentException("Base64 字符串不能为空");
        }

        if (base64WithPrefix.contains(",")) {
            return base64WithPrefix.substring(base64WithPrefix.indexOf(",") + 1);
        }
        return base64WithPrefix;
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名
     * @return 扩展名（不包含点）
     */
    public static String getFileExtension(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
}