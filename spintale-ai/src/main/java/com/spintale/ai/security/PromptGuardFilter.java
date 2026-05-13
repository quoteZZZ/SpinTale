package com.spintale.ai.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Prompt 安全过滤器
 * 检测和阻止潜在的注入攻击、越狱指令和恶意输入
 */
@Component
public class PromptGuardFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(PromptGuardFilter.class);

    // 常见注入攻击模式
    private static final Pattern[] ATTACK_PATTERNS = {
        // SQL 注入
        Pattern.compile("(?i)(union\\s+select|insert\\s+into|delete\\s+from|drop\\s+table)", Pattern.CASE_INSENSITIVE),
        // XSS 攻击
        Pattern.compile("(?i)(<script|javascript:|on\\w+=)", Pattern.CASE_INSENSITIVE),
        // 命令注入
        Pattern.compile("(?i)(;|\\||\\$\\(|`|&&)", Pattern.CASE_INSENSITIVE),
        // 越狱指令
        Pattern.compile("(?i)(ignore\\s+previous|bypass\\s+rules|disable\\s+safety|you\\s+are\\s+now)", Pattern.CASE_INSENSITIVE),
        // 提示词注入
        Pattern.compile("(?i)(system\\s+prompt|developer\\s+mode|dan\\s+mode)", Pattern.CASE_INSENSITIVE)
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String uri = request.getRequestURI();
        
        // 只检查 AI 聊天相关接口
        if (!uri.startsWith("/ai/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 检查请求参数
        String message = request.getParameter("message");
        if (message != null && containsAttackPattern(message)) {
            log.warn("Blocked potential prompt injection attack from IP: {}", getClientIp(request));
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{\"code\":400,\"msg\":\"Invalid input detected\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 检查文本是否包含攻击模式
     */
    private boolean containsAttackPattern(String text) {
        for (Pattern pattern : ATTACK_PATTERNS) {
            if (pattern.matcher(text).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取客户端 IP 地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
