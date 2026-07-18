package com.rhy.interviewprep.service;

import com.rhy.interviewprep.dto.AddSkillDTO;
import com.rhy.interviewprep.dto.UpdateSkillDTO;
import com.rhy.interviewprep.entity.UserSkill;

import java.util.List;

public interface UserSkillService {

    UserSkill addSkill(Long userId, AddSkillDTO dto);

    UserSkill updateSkill(Long userId, Long skillId, UpdateSkillDTO dto);

    void deleteSkill(Long userId, Long skillId);

    List<UserSkill> listSkills(Long userId);

    UserSkill getSkill(Long userId, Long skillId);
}