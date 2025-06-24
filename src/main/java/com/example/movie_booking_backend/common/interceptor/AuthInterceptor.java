package com.example.movie_booking_backend.common.interceptor;

import com.example.movie_booking_backend.common.annotation.RequireRole;
import com.example.movie_booking_backend.common.constants.PermissionConstants;
import com.example.movie_booking_backend.common.exception.BusinessException;
import com.example.movie_booking_backend.common.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是方法处理器，直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // 获取token
        String token = request.getHeader("Authorization");
        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 验证token
        if (!StringUtils.hasText(token)) {
            throw new BusinessException("未提供访问令牌");
        }

        try {
            // 验证token有效性
            if (jwtUtils.isTokenExpired(token)) {
                throw new BusinessException("访问令牌已过期");
            }

            // 获取用户角色
            String userRole = jwtUtils.getRoleFromToken(token);
            if (userRole == null) {
                throw new BusinessException("无效的访问令牌");
            }

            // 检查角色权限
            RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
            if (requireRole != null) {
                String[] requiredRoles = requireRole.value();
                if (!Arrays.asList(requiredRoles).contains(userRole)) {
                    throw new BusinessException("权限不足");
                }
            }

            // 将用户信息存入request
            request.setAttribute("userId", jwtUtils.getUserIdFromToken(token));
            request.setAttribute("username", jwtUtils.getUsernameFromToken(token));
            request.setAttribute("userRole", userRole);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("访问令牌验证失败");
        }

        return true;
    }
} 