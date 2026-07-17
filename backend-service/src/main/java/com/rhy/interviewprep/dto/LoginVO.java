package com.rhy.interviewprep.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginVO {
    private Long userId;
    private String username;
    /** 登录令牌（JWT），由 JwtTokenProvider 签发 */
    private String token;
}