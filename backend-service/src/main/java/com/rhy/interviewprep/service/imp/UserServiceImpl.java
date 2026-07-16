package com.rhy.interviewprep.service.imp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rhy.interviewprep.dto.RegisterDTO;
import com.rhy.interviewprep.entity.SysUser;
import com.rhy.interviewprep.common.Result;
import com.rhy.interviewprep.common.ErrorCode;
import com.rhy.interviewprep.mapper.SysUserMapper;
import com.rhy.interviewprep.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

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
}