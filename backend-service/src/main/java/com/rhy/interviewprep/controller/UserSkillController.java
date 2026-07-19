package com.rhy.interviewprep.controller;

import com.rhy.interviewprep.common.Result;
import com.rhy.interviewprep.constants.CommonSkills;
import com.rhy.interviewprep.dto.AddSkillDTO;
import com.rhy.interviewprep.dto.UpdateSkillDTO;
import com.rhy.interviewprep.entity.UserSkill;
import com.rhy.interviewprep.security.SecurityUtils;
import com.rhy.interviewprep.service.UserSkillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户技能控制器
 * 提供技能 CRUD、常用技能列表查询等接口
 */
@Slf4j
@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class UserSkillController {

    private final UserSkillService userSkillService;

    /**
     * 添加用户技能
     *
     * @param dto 技能信息（skillName、level、category）
     * @return 添加后的技能实体
     */
    @PostMapping
    public Result<UserSkill> add(@Valid @RequestBody AddSkillDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        UserSkill skill = userSkillService.addSkill(userId, dto);
        return Result.success(skill);
    }

    /**
     * 更新用户技能
     *
     * @param id  技能 ID
     * @param dto 更新信息（skillName、level、category）
     * @return 更新后的技能实体
     */
    @PutMapping("/{id}")
    public Result<UserSkill> update(@PathVariable Long id, @Valid @RequestBody UpdateSkillDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        UserSkill skill = userSkillService.updateSkill(userId, id, dto);
        return Result.success(skill);
    }

    /**
     * 删除用户技能
     *
     * @param id 技能 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        userSkillService.deleteSkill(userId, id);
        return Result.success();
    }

    /**
     * 获取当前用户的所有技能列表
     *
     * @return 技能列表
     */
    @GetMapping
    public Result<List<UserSkill>> list() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<UserSkill> skills = userSkillService.listSkills(userId);
        return Result.success(skills);
    }

    /**
     * 获取单个技能详情
     *
     * @param id 技能 ID
     * @return 技能详情
     */
    @GetMapping("/{id}")
    public Result<UserSkill> get(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        UserSkill skill = userSkillService.getSkill(userId, id);
        return Result.success(skill);
    }

    /**
     * 获取常用技能列表
     * 返回按分类组织的常见技术技能，供前端技能选择器使用
     *
     * @return 分类技能列表（programmingLanguages、frameworks、databases、cloud、tools、all）
     */
    @GetMapping("/common")
    public Result<Map<String, List<String>>> getCommonSkills() {
        Map<String, List<String>> skills = new HashMap<>();
        skills.put("programmingLanguages", CommonSkills.PROGRAMMING_LANGUAGES);
        skills.put("frameworks", CommonSkills.FRAMEWORKS);
        skills.put("databases", CommonSkills.DATABASES);
        skills.put("cloud", CommonSkills.CLOUD);
        skills.put("tools", CommonSkills.TOOLS);
        skills.put("all", CommonSkills.ALL_COMMON_SKILLS);
        return Result.success(skills);
    }
}