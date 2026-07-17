package com.rhy.interviewprep.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 签发与解析。
 *  - 登录成功：调用 generateToken(userId, username) 拿到 token
 *  - 拦截器（2.4）：调用 parseClaims(token) 拿到 userId / username
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        // HS256 要求密钥 >= 32 字节
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 JWT：subject = username，自定义字段 userId
     */
    public String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 解析 token，返回 Claims（含 userId、subject）
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 获取 token 剩余有效期（毫秒）。
     * 用于注销时设置 Redis 黑名单的 TTL。
     */
    public long getRemainingExpiration(String token) {
        Claims claims = parseClaims(token);
        Date expiry = claims.getExpiration();
        long remaining = expiry.getTime() - System.currentTimeMillis();
        return Math.max(remaining, 0);
    }

    public long getExpiration() {
        return expiration;
    }
}