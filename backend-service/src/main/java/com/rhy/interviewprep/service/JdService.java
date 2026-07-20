package com.rhy.interviewprep.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.rhy.interviewprep.entity.JobDescription;
import org.springframework.web.multipart.MultipartFile;

/**
 * JD 解析服务接口
 */
public interface JdService {

    /**
     * 上传图片并解析 JD
     * 流程：OCR 识别图片文本 → DeepSeek 解析结构化信息 → 保存到数据库
     *
     * @param file    上传的 JD 图片
     * @param userId  当前登录用户 ID
     * @return 解析后的 JD 实体（含 ID）
     */
    JobDescription parseFromImage(MultipartFile file, Long userId);

    /**
     * 从文件路径解析 JD（供 Agent Tool 调用）
     * 流程：读取文件 → OCR 识别 → DeepSeek 解析 → 保存到数据库
     *
     * @param filePath 图片文件路径
     * @param userId   当前登录用户 ID
     * @return 解析后的 JD 实体（含 ID）
     */
    JobDescription parseFromFilePath(String filePath, Long userId);

    /**
     * 根据 ID 查询 JD 详情
     *
     * @param id     JD ID
     * @param userId 当前用户 ID（校验权限）
     * @return JD 实体
     */
    JobDescription getById(Long id, Long userId);

    /**
     * 分页查询当前用户的 JD 列表（按创建时间倒序）
     *
     * @param userId 当前用户 ID
     * @param page   页码（从 1 开始）
     * @param size   每页条数
     * @return 分页结果
     */
    IPage<JobDescription> listByUserId(Long userId, int page, int size);

    /**
     * 删除 JD 记录
     *
     * @param id     JD ID
     * @param userId 当前用户 ID（校验权限）
     */
    void deleteById(Long id, Long userId);
}