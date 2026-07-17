package com.rhy.interviewprep.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.rhy.interviewprep.common.Result;
import com.rhy.interviewprep.entity.JobDescription;
import com.rhy.interviewprep.security.SecurityUtils;
import com.rhy.interviewprep.service.JdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * JD 解析管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/jd")
@RequiredArgsConstructor
public class JdController {

    private final JdService jdService;

    /**
     * 上传 JD 图片并解析
     * POST /api/jd/parse
     *
     * @param file 上传的 JD 图片文件
     * @return 解析后的 JD 信息
     */
    @PostMapping("/parse")
    public Result<JobDescription> parse(@RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtils.getCurrentUserId();
        JobDescription jd = jdService.parseFromImage(file, userId);
        return Result.success(jd);
    }

    /**
     * 分页查询当前用户的 JD 列表
     * GET /api/jd/list?page=1&size=10
     *
     * @param page 页码（从 1 开始）
     * @param size 每页条数
     * @return 分页 JD 列表
     */
    @GetMapping("/list")
    public Result<IPage<JobDescription>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        IPage<JobDescription> result = jdService.listByUserId(userId, page, size);
        return Result.success(result);
    }

    /**
     * 查询 JD 详情
     * GET /api/jd/{id}
     *
     * @param id JD ID
     * @return JD 详情
     */
    @GetMapping("/{id}")
    public Result<JobDescription> detail(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        JobDescription jd = jdService.getById(id, userId);
        return Result.success(jd);
    }

    /**
     * 删除 JD 记录
     * DELETE /api/jd/{id}
     *
     * @param id JD ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        jdService.deleteById(id, userId);
        return Result.success();
    }
}