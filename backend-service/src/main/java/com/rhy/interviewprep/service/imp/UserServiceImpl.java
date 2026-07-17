package com.rhy.interviewprep.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rhy.interviewprep.dto.ChangePasswordDTO;
import com.rhy.interviewprep.dto.LoginDTO;
import com.rhy.interviewprep.dto.LoginVO;
import com.rhy.interviewprep.dto.RegisterDTO;
import com.rhy.interviewprep.dto.UserVO;
import com.rhy.interviewprep.entity.SysUser;
import com.rhy.interviewprep.common.Result;
import com.rhy.interviewprep.common.ErrorCode;
import com.rhy.interviewprep.mapper.SysUserMapper;
import com.rhy.interviewprep.security.JwtTokenProvider;
import com.rhy.interviewprep.security.TokenBlacklistService;
import com.rhy.interviewprep.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    /**
     * 由 SecurityConfig 注入。authenticate() 内部会：
     *   1. 调 UserDetailsService.loadUserByUsername 查 DB
     *   2. 用 PasswordEncoder.matches(BCrypt) 校验明文 vs 密文
     *   3. 成功返回 Authentication，失败抛 AuthenticationException
     */
    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private JwtTokenProvider jwtTokenProvider;

    @Resource
    private TokenBlacklistService tokenBlacklistService;

    @Override
    public Result<String> register(RegisterDTO dto) {
        // 1. 密码长度校验：不能小于6位
        String password = dto.getPassword();
        if (password == null || password.length() < 6) {
            return Result.error(ErrorCode.BAD_REQUEST, "密码长度不能少于6位");
        }

        // 2. 校验用户名是否重复
        Long count = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, dto.getUsername())
        );
        if (count > 0) {
            return Result.error(ErrorCode.BAD_REQUEST, "用户名已存在");
        }

        // 3. BCrypt 加密明文密码
        String encryptPwd = passwordEncoder.encode(password);

        // 4. 封装用户数据插入数据库
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(encryptPwd);
        sysUserMapper.insert(user);

        return Result.success("注册成功");
    }

    @Override
    public Result<LoginVO> login(LoginDTO dto) {
        // 基本非空校验
        if (dto.getUsername() == null || dto.getUsername().isEmpty()
                || dto.getPassword() == null || dto.getPassword().isEmpty()) {
            return Result.error(ErrorCode.BAD_REQUEST, "用户名和密码不能为空");
        }

        try {
            // Spring Security 校验入口：内部走 UserDetailsService + BCrypt
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
            );

            // 校验通过，查出 userId 一起返回
            SysUser user = sysUserMapper.selectOne(
                    new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername())
            );

            // 登录成功：立刻签发 JWT
            String token = jwtTokenProvider.generateToken(
                    user != null ? user.getId() : null,
                    authentication.getName()
            );

            LoginVO vo = LoginVO.builder()
                    .userId(user != null ? user.getId() : null)
                    .username(authentication.getName())
                    .token(token)
                    .build();
            return Result.success("登录成功", vo);
        } catch (BadCredentialsException e) {
            // 密码错误（用户存在但密码不匹配，Security 抛 BadCredentialsException）
            return Result.error(ErrorCode.USER_PASSWORD_ERROR, "用户名或密码错误");
        } catch (AuthenticationException e) {
            // 其它认证失败：用户不存在 / 账号被锁等
            return Result.error(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }
    }

    @Override
    public Result<String> logout(String token) {
        try {
            long remainingMs = jwtTokenProvider.getRemainingExpiration(token);
            tokenBlacklistService.addToBlacklist(token, remainingMs);
            return Result.success("注销成功");
        } catch (Exception e) {
            // token 已无效/过期，无需加入黑名单，直接返回成功
            return Result.success("注销成功");
        }
    }

    @Override
    public Result<UserVO> getUserInfo(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return Result.error(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }
        UserVO vo = UserVO.builder()
                .username(user.getUsername())
                .password("******")
                .createdAt(user.getCreatedAt())
                .build();
        return Result.success(vo);
    }

    @Override
    public Result<String> changePassword(Long userId, ChangePasswordDTO dto) {
        // 1. 新密码长度校验
        String newPassword = dto.getNewPassword();
        if (newPassword == null || newPassword.length() < 6) {
            return Result.error(ErrorCode.BAD_REQUEST, "新密码长度不能少于6位");
        }

        // 2. 查出当前用户
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return Result.error(ErrorCode.USER_NOT_FOUND, "用户不存在");
        }

        // 3. 校验旧密码
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            return Result.error(ErrorCode.USER_PASSWORD_ERROR, "旧密码错误");
        }

        // 4. BCrypt 加密新密码并更新
        user.setPassword(passwordEncoder.encode(newPassword));
        sysUserMapper.updateById(user);

        return Result.success("密码修改成功");
    }
}