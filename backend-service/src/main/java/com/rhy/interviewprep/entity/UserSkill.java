package com.rhy.interviewprep.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_skill")
public class UserSkill {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String skillName;

    private Integer level;

    private LocalDateTime createdAt;
}