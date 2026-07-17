package com.rhy.interviewprep.security;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录态主体。
 * Controller 可以通过 @AuthenticationPrincipal LoginUser 拿到当前登录用户。
 */
@Data
@AllArgsConstructor
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
}