package com.rhy.interviewprep.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateSkillDTO {

    @Min(value = 1, message = "掌握程度最小为1")
    @Max(value = 3, message = "掌握程度最大为3")
    private Integer level;

    /** 技能类型：tech-技术栈，soft-软技能 */
    private String category;
}