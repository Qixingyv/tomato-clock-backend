package com.paolua.tomatoclockbackend.security.interceptor;

import com.paolua.tomatoclockbackend.common.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 认证拦截器
 * 验证JWT Token的有效性
 */
@Component
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        // 只拦截Controller方法
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        // 检查是否有 @SkipAuth 注解
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        if (handlerMethod.hasMethodAnnotation(com.paolua.tomatoclockbackend.security.annotation.SkipAuth.class)) {
            return true;
        }

        // 获取Token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorizedResponse(response, "缺少认证Token");
            return false;
        }

        String token = authHeader.substring(7);
        try {
            if (jwtUtil.isTokenExpired(token)) {
                sendUnauthorizedResponse(response, "Token已过期");
                return false;
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            request.setAttribute("userId", userId);

            log.debug("认证成功: userId={}, uri={}", userId, request.getRequestURI());
            return true;

        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
            sendUnauthorizedResponse(response, "Token无效");
            return false;
        }
    }

    /**
     * 发送未授权响应
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        String jsonBody = String.format("{\"code\":401,\"message\":\"%s\"}", message);
        response.getWriter().write(jsonBody);
    }
}
