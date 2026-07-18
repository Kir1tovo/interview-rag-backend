package com.rhy.interviewprep.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateSkillDTO {

    @Pattern(regexp = "^(了解|熟悉|精通)$", message = "掌握程度只能是：了解、熟悉、精通")
    private String skillLevel;

    private Integer experienceYears;
}