package com.spintale.ai.safety.guardrail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 输出围栏服务
 * 实时检测并过滤 AI 生成内容中的违规信息、隐私数据等
 */
@Slf4j
@Service
public class OutputGuardrailService {

    // 敏感信息正则表达式
    private static final List<Pattern> SENSITIVE_PATTERNS = Arrays.asList(
        Pattern.compile("\\b\\d{16}\\b"), // 信用卡号
        Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b"), // SSN
        Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"), // 邮箱
        Pattern.compile("\\b\\d{11}\\b"), // 手机号（简单匹配）
        Pattern.compile("(?i)(password|passwd|pwd)\\s*[=:]\\s*\\S+"), // 密码
        Pattern.compile("(?i)(api[_-]?key|secret[_-]?key|token)\\s*[=:]\\s*\\S+") // API Key
    );

    // 违规关键词列表
    private static final List<String> BLOCKED_KEYWORDS = Arrays.asList(
        "暴力", "恐怖", "违法", "犯罪", "自杀", "自残",
        "歧视", "仇恨", "色情", "赌博", "毒品"
    );

    /**
     * 检测和过滤输出内容
     * @param content AI 生成的原始内容
     * @return 过滤后的内容
     */
    public FilteredResult filter(String content) {
        if (content == null || content.isEmpty()) {
            return new FilteredResult("", true, "内容为空");
        }

        log.debug("开始检测输出内容：长度={}", content.length());

        StringBuilder filteredContent = new StringBuilder(content);
        boolean hasViolation = false;
        List<String> violations = new java.util.ArrayList<>();

        // 1. 检测敏感信息
        for (Pattern pattern : SENSITIVE_PATTERNS) {
            Matcher matcher = pattern.matcher(filteredContent);
            if (matcher.find()) {
                hasViolation = true;
                violations.add("检测到敏感信息：" + matcher.group());
                // 脱敏处理
                filteredContent = new StringBuilder(
                    matcher.replaceAll(matcher.group().substring(0, Math.min(3, matcher.group().length())) + "***")
                );
            }
        }

        // 2. 检测违规关键词
        for (String keyword : BLOCKED_KEYWORDS) {
            if (filteredContent.toString().toLowerCase().contains(keyword.toLowerCase())) {
                hasViolation = true;
                violations.add("检测到违规关键词：" + keyword);
                // 替换为星号
                filteredContent = new StringBuilder(
                    filteredContent.toString().replaceAll("(?i)" + keyword, "*".repeat(keyword.length()))
                );
            }
        }

        if (hasViolation) {
            log.warn("输出内容检测到违规：{}", String.join(", ", violations));
        } else {
            log.debug("输出内容检测通过");
        }

        return new FilteredResult(filteredContent.toString(), !hasViolation, violations);
    }

    /**
     * 批量检测多条内容
     */
    public List<FilteredResult> filterBatch(List<String> contents) {
        return contents.stream()
                .map(this::filter)
                .collect(Collectors.toList());
    }

    /**
     * 过滤结果记录
     */
    public record FilteredResult(
            String content,
            boolean isSafe,
            Object violations // List<String> 或 String
    ) {}
}
