package com.rhy.interviewprep.service;

import com.rhy.interviewprep.dto.ChangePasswordDTO;
import com.rhy.interviewprep.dto.LoginDTO;
import com.rhy.interviewprep.dto.LoginVO;
import com.rhy.interviewprep.dto.RegisterDTO;
import com.rhy.interviewprep.dto.UserVO;
import com.rhy.interviewprep.common.Result;

public interface UserService {
    /**
     * 用户注册
     */
    Result<String> register(RegisterDTO dto);

    /**
     * 用户登录（用户名 + 密码，BCrypt 校验）
     */
    Result<LoginVO> login(LoginDTO dto);

    /**
     * 用户注销登录（将 token 加入 Redis 黑名单）
     */
    Result<String> logout(String token);

    /**
     * 查询当前用户信息（username、password 脱敏、createdAt）
     */
    Result<UserVO> getUserInfo(Long userId);

    /**
     * 修改密码（校验旧密码 → BCrypt 加密新密码 → 更新）
     */
    Result<String> changePassword(Long userId, ChangePasswordDTO dto);
}