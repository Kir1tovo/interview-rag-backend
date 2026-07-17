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

@RestController
@RequestMapping("/api")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterDTO registerDTO) {
        return userService.register(registerDTO);
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        return userService.login(loginDTO);
    }

    @PostMapping("/logout")
    public Result<String> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "").trim();
        return userService.logout(token);
    }

    /**
     * 查询当前登录用户信息（username、password 脱敏、createdAt）
     */
    @GetMapping("/info")
    public Result<UserVO> getUserInfo() {
        Long userId = SecurityUtils.getCurrentUserId();
        return userService.getUserInfo(userId);
    }

    /**
     * 修改密码（需提供旧密码 + 新密码）
     */
    @PostMapping("/password")
    public Result<String> changePassword(@RequestBody ChangePasswordDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        return userService.changePassword(userId, dto);
    }
}