package com.rhy.interviewprep.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddSkillDTO {

    @NotBlank(message = "技能名称不能为空")
    private String skillName;

    @NotNull(message = "掌握程度不能为空")
    @Min(value = 1, message = "掌握程度最小为1")
    @Max(value = 3, message = "掌握程度最大为3")
    private Integer level;
}