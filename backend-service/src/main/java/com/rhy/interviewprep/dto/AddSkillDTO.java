package com.rhy.interviewprep.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AddSkillDTO {

    @NotBlank(message = "技能名称不能为空")
    private String skillName;

    @NotBlank(message = "掌握程度不能为空")
    @Pattern(regexp = "^(了解|熟悉|精通)$", message = "掌握程度只能是：了解、熟悉、精通")
    private String skillLevel;

    @NotNull(message = "经验年限不能为空")
    private Integer experienceYears;
}