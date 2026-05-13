package com.spintale.ai.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.CompositeConverter;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 敏感数据脱敏转换器
 * 用于日志中自动掩盖 API Key、Token、密码等敏感信息
 */
@Component
public class SensitiveDataMaskingConverter extends CompositeConverter<ILoggingEvent> {

    // 匹配常见的敏感模式
    private static final Pattern[] SENSITIVE_PATTERNS = {
        // API Key (OpenAI, Anthropic 等)
        Pattern.compile("(sk-[a-zA-Z0-9]{20,})"),
        // Bearer Token
        Pattern.compile("(Bearer\\s+[a-zA-Z0-9\\-_.]+)"),
        // 密码
        Pattern.compile("(\"password\"\\s*:\\s*\"[^\"]+\")"),
        // 邮箱
        Pattern.compile("([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})"),
        // 手机号 (中国)
        Pattern.compile("(1[3-9]\\d{9})"),
        // 身份证号
        Pattern.compile("(\\d{6}\\d{4}\\d{2}\\d{2}\\d{3}[\\dxX])")
    };

    @Override
    protected String transform(ILoggingEvent event, String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String result = input;
        for (Pattern pattern : SENSITIVE_PATTERNS) {
            Matcher matcher = pattern.matcher(result);
            result = matcher.replaceAll(maskMatch(matcher.group()));
        }

        return result;
    }

    /**
     * 根据匹配内容类型进行脱敏
     */
    private String maskMatch(String match) {
        if (match == null || match.length() < 4) {
            return "***";
        }

        // API Key: 保留前 4 位和后 4 位
        if (match.startsWith("sk-")) {
            return match.substring(0, 7) + "..." + match.substring(match.length() - 4);
        }

        // Bearer Token
        if (match.startsWith("Bearer")) {
            return "Bearer ***";
        }

        // 密码
        if (match.contains("password")) {
            return "\"password\":\"***\"";
        }

        // 邮箱：保留前 2 位和域名
        if (match.contains("@")) {
            int atIndex = match.indexOf("@");
            String domain = match.substring(atIndex);
            return match.substring(0, 2) + "***" + domain;
        }

        // 手机号：保留前 3 位和后 4 位
        if (match.length() == 11 && match.matches("\\d+")) {
            return match.substring(0, 3) + "****" + match.substring(7);
        }

        // 默认：只保留前 2 位和后 2 位
        return match.substring(0, 2) + "***" + match.substring(match.length() - 2);
    }
}
