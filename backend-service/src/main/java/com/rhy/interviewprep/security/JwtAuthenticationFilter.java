package com.rhy.interviewprep.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhy.interviewprep.common.ErrorCode;
import com.rhy.interviewprep.common.Result;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT 鉴权过滤器：
 *  1. 从 Authorization 头解析 Bearer token
 *  2. 验签 + 解析 Claims
 *  3. 写入 SecurityContext（principal = LoginUser）
 *  4. 失败时统一以 Result 格式返回 401
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            // 没带 token，放行到下一个过滤器，
            // SecurityFilterChain 里的 .anyRequest().authenticated() 会兜底拦截
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();

        try {
            Claims claims = jwtTokenProvider.parseClaims(token);
            Long userId = claims.get("userId", Long.class);
            String username = claims.getSubject();

            // 检查 token 是否已被注销（黑名单）
            if (tokenBlacklistService.isBlacklisted(token)) {
                log.warn("JWT 已被注销（黑名单）: username={}", username);
                writeUnauthorized(response, ErrorCode.USER_TOKEN_EXPIRED);
                return;
            }

            LoginUser principal = new LoginUser(userId, username);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    principal, null, Collections.emptyList()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            chain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            log.warn("JWT 已过期: {}", e.getMessage());
            writeUnauthorized(response, ErrorCode.USER_TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT 校验失败: {}", e.getMessage());
            writeUnauthorized(response, ErrorCode.USER_TOKEN_INVALID);
        }
    }

    private void writeUnauthorized(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        Result<Void> body = Result.error(errorCode);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}