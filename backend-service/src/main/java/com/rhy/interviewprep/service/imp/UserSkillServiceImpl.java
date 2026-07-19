package com.rhy.interviewprep.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rhy.interviewprep.common.BusinessException;
import com.rhy.interviewprep.common.ErrorCode;
import com.rhy.interviewprep.dto.AddSkillDTO;
import com.rhy.interviewprep.dto.UpdateSkillDTO;
import com.rhy.interviewprep.entity.UserSkill;
import com.rhy.interviewprep.mapper.UserSkillMapper;
import com.rhy.interviewprep.service.UserSkillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSkillServiceImpl implements UserSkillService {

    private final UserSkillMapper userSkillMapper;

    @Override
    public UserSkill addSkill(Long userId, AddSkillDTO dto) {
        UserSkill skill = new UserSkill();
        skill.setUserId(userId);
        skill.setSkillName(dto.getSkillName().trim());
        skill.setLevel(dto.getLevel());
        skill.setCategory(dto.getCategory() != null ? dto.getCategory() : "tech");
        skill.setCreatedAt(LocalDateTime.now());

        userSkillMapper.insert(skill);
        log.info("用户 {} 添加技能: {}（类型: {}）", userId, skill.getSkillName(), skill.getCategory());
        return skill;
    }

    @Override
    public UserSkill updateSkill(Long userId, Long skillId, UpdateSkillDTO dto) {
        UserSkill skill = getSkill(userId, skillId);

        if (dto.getLevel() != null) {
            skill.setLevel(dto.getLevel());
        }
        if (dto.getCategory() != null) {
            skill.setCategory(dto.getCategory());
        }

        userSkillMapper.updateById(skill);
        log.info("用户 {} 更新技能: {}（类型: {}）", userId, skill.getSkillName(), skill.getCategory());
        return skill;
    }

    @Override
    public void deleteSkill(Long userId, Long skillId) {
        UserSkill skill = getSkill(userId, skillId);
        userSkillMapper.deleteById(skillId);
        log.info("用户 {} 删除技能: {}", userId, skill.getSkillName());
    }

    @Override
    public List<UserSkill> listSkills(Long userId) {
        return userSkillMapper.selectList(
                new LambdaQueryWrapper<UserSkill>()
                        .eq(UserSkill::getUserId, userId)
                        .orderByDesc(UserSkill::getCreatedAt)
        );
    }

    @Override
    public UserSkill getSkill(Long userId, Long skillId) {
        UserSkill skill = userSkillMapper.selectById(skillId);
        if (skill == null || !skill.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.SKILL_NOT_FOUND);
        }
        return skill;
    }
}