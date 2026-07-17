package com.rhy.interviewprep.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhy.interviewprep.common.ErrorCode;
import com.rhy.interviewprep.common.Result;
import com.rhy.interviewprep.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 密码加密器（BCrypt）
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 暴露 AuthenticationManager，供 UserServiceImpl 注入用于登录校验
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // SecurityFilterChain：放开注册登录、其余接口走 JWT 鉴权
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/register", "/api/login", "/api/logout").permitAll()
                .anyRequest().authenticated()
            )
            // 无 token 访问受保护资源时的兜底
            .exceptionHandling(eh -> eh
                .authenticationEntryPoint((req, resp, ex) -> writeJsonError(resp,
                        HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.UNAUTHORIZED))
                .accessDeniedHandler((req, resp, ex) -> writeJsonError(resp,
                        HttpServletResponse.SC_FORBIDDEN, ErrorCode.FORBIDDEN))
            )
            // 把 JWT 过滤器插到 UsernamePasswordAuthenticationFilter 之前
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void writeJsonError(HttpServletResponse response, int status, ErrorCode code) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(code)));
    }
}