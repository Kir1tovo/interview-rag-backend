package com.rhy.interviewprep.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息返回体。
 * password 字段脱敏返回（不暴露 BCrypt 密文）。
 */
@Data
@Builder
public class UserVO {
    private String username;
    /** 脱敏：固定返回 ****** */
    private String password;
    private LocalDateTime createdAt;
}