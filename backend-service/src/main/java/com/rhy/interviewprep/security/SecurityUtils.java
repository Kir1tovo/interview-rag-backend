package com.rhy.interviewprep.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 提供"在任意位置拿到当前登录用户"的工具方法。
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    /** 取当前登录用户，未登录返回 null */
    public static LoginUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof LoginUser)) {
            return null;
        }
        return (LoginUser) auth.getPrincipal();
    }

    public static Long getCurrentUserId() {
        LoginUser user = getCurrentUser();
        return user != null ? user.getUserId() : null;
    }

    public static String getCurrentUsername() {
        LoginUser user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }
}