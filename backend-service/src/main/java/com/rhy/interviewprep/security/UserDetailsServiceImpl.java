package com.rhy.interviewprep.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rhy.interviewprep.entity.SysUser;
import com.rhy.interviewprep.mapper.SysUserMapper;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Spring Security 标准的用户加载器：
 * - 根据 username 查库
 * - 抛出 UsernameNotFoundException 让 Security 返回 "用户不存在"
 * - 返回的 User 中 password 字段是 BCrypt 密文，交给 PasswordEncoder.matches 校验
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
        );
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        // 第三个参数 authorities：目前无角色控制，给空集合即可
        return new User(user.getUsername(), user.getPassword(), Collections.emptyList());
    }
}