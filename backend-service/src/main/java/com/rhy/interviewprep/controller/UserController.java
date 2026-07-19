package com.rhy.interviewprep.controller;

import com.rhy.interviewprep.common.Result;
import com.rhy.interviewprep.dto.ChangePasswordDTO;
import com.rhy.interviewprep.dto.LoginDTO;
import com.rhy.interviewprep.dto.LoginVO;
import com.rhy.interviewprep.dto.RegisterDTO;
import com.rhy.interviewprep.dto.UserVO;
import com.rhy.interviewprep.security.SecurityUtils;
import com.rhy.interviewprep.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器
 * 提供用户注册、登录、注销、信息查询、修改密码等接口
 */
@RestController
@RequestMapping("/api")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息（username、password）
     * @return 操作结果
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterDTO registerDTO) {
        return userService.register(registerDTO);
    }

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息（username、password）
     * @return 登录结果（含 JWT Token 和用户名）
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        return userService.login(loginDTO);
    }

    /**
     * 用户注销登录
     * 将 Token 加入 Redis 黑名单，使其失效
     *
     * @param authHeader Authorization 请求头（Bearer Token）
     * @return 操作结果
     */
    @PostMapping("/logout")
    public Result<String> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "").trim();
        return userService.logout(token);
    }

    /**
     * 查询当前登录用户信息
     *
     * @return 用户信息（username、password 脱敏、createdAt）
     */
    @GetMapping("/info")
    public Result<UserVO> getUserInfo() {
        Long userId = SecurityUtils.getCurrentUserId();
        return userService.getUserInfo(userId);
    }

    /**
     * 修改密码
     *
     * @param dto 修改密码请求（oldPassword、newPassword）
     * @return 操作结果
     */
    @PostMapping("/password")
    public Result<String> changePassword(@RequestBody ChangePasswordDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        return userService.changePassword(userId, dto);
    }
}