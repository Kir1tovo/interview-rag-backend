package com.rhy.interviewprep.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Token 黑名单服务（Redis 实现）。
 * <p>
 * 注销登录时将 token 写入 Redis，TTL = token 剩余有效期，
 * 过期后 Redis 自动清理，不占空间。
 * <p>
 * Key 设计：token:blacklist:{SHA-256 前 16 位}
 * 不存完整 token，避免敏感信息泄露和内存膨胀。
 */
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate redisTemplate;
    private static final String KEY_PREFIX = "token:blacklist:";

    /**
     * 将 token 加入黑名单。
     *
     * @param token       原始 JWT token
     * @param remainingMs token 剩余有效期（毫秒）
     */
    public void addToBlacklist(String token, long remainingMs) {
        if (remainingMs <= 0) {
            return; // 已过期的不用存
        }
        String key = KEY_PREFIX + hashToken(token);
        redisTemplate.opsForValue().set(key, "1", remainingMs, TimeUnit.MILLISECONDS);
    }

    /**
     * 判断 token 是否在黑名单中。
     */
    public boolean isBlacklisted(String token) {
        String key = KEY_PREFIX + hashToken(token);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 对 token 做 SHA-256 截断哈希，避免存明文。
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            StringBuilder hex = new StringBuilder();
            for (int i = 0; i < Math.min(hash.length, 8); i++) {
                hex.append(String.format("%02x", hash[i]));
            }
            return hex.toString(); // 前 8 字节 = 16 个十六进制字符
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 是 JDK 标准算法，不可能不存在
            throw new RuntimeException("SHA-256 algorithm not found", e);
        }
    }
}