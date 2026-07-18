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

@Slf4j
@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class UserSkillController {

    private final UserSkillService userSkillService;

    @PostMapping
    public Result<UserSkill> add(@Valid @RequestBody AddSkillDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        UserSkill skill = userSkillService.addSkill(userId, dto);
        return Result.success(skill);
    }

    @PutMapping("/{id}")
    public Result<UserSkill> update(@PathVariable Long id, @Valid @RequestBody UpdateSkillDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        UserSkill skill = userSkillService.updateSkill(userId, id, dto);
        return Result.success(skill);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        userSkillService.deleteSkill(userId, id);
        return Result.success();
    }

    @GetMapping
    public Result<List<UserSkill>> list() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<UserSkill> skills = userSkillService.listSkills(userId);
        return Result.success(skills);
    }

    @GetMapping("/{id}")
    public Result<UserSkill> get(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        UserSkill skill = userSkillService.getSkill(userId, id);
        return Result.success(skill);
    }

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