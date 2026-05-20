package com.spintale.ai.infrastructure.logging;

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
public class MaskingConverter extends CompositeConverter<ILoggingEvent> {

    private static final Pattern[] SENSITIVE_PATTERNS = {
        Pattern.compile("(sk-[a-zA-Z0-9]{20,})"),
        Pattern.compile("(Bearer\\s+[a-zA-Z0-9\\-_.]+)"),
        Pattern.compile("(\"password\"\\s*:\\s*\"[^\"]+\")"),
        Pattern.compile("([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})"),
        Pattern.compile("(1[3-9]\\d{9})"),
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
            result = matcher.replaceAll(match -> maskMatch(match.group()));
        }

        return result;
    }

    private String maskMatch(String match) {
        if (match == null || match.length() < 4) {
            return "***";
        }

        if (match.startsWith("sk-")) {
            return match.substring(0, 7) + "..." + match.substring(match.length() - 4);
        }
        if (match.startsWith("Bearer")) {
            return "Bearer ***";
        }
        if (match.contains("password")) {
            return "\"password\":\"***\"";
        }
        if (match.contains("@")) {
            int atIndex = match.indexOf("@");
            String domain = match.substring(atIndex);
            return match.substring(0, 2) + "***" + domain;
        }
        if (match.length() == 11 && match.matches("\\d+")) {
            return match.substring(0, 3) + "****" + match.substring(7);
        }
        return match.substring(0, 2) + "***" + match.substring(match.length() - 2);
    }
}
