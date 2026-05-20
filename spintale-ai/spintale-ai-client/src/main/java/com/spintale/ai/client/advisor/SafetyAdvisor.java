package com.spintale.ai.client.advisor;

import com.spintale.ai.capability.advisor.Advisor;
import com.spintale.ai.capability.advisor.AdvisorContext;
import com.spintale.ai.capability.advisor.AdvisorRequest;
import com.spintale.ai.capability.advisor.AdvisorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.List;

/**
 * 安全围栏 Advisor
 *
 * 请求阶段：检测并拦截不当输入
 * 响应阶段：过滤敏感信息和违规内容
 *
 * 改进点：
 * - 统一了原分散�?OutputGuardrailService 和安全检查逻辑
 * - 可配置的安全等级
 * - 正则 + 关键词双层检�?
 */
public class SafetyAdvisor implements Advisor {

    private static final Logger log = LoggerFactory.getLogger(SafetyAdvisor.class);

    /** 安全等级 */
    public enum SafetyLevel {
        STRICT,   // 严格模式：拦�?+ 脱敏
        MODERATE, // 中等模式：脱�?+ 警告
        RELAXED   // 宽松模式：仅记录
    }

    private SafetyLevel safetyLevel = SafetyLevel.MODERATE;

    // 违规关键�?
    private static final List<String> BLOCKED_KEYWORDS = Arrays.asList(
            "暴力", "恐�?, "违法", "犯罪", "自杀", "自残",
            "歧视", "仇恨", "色情", "赌博", "毒品"
    );

    // 敏感信息正则
    private static final List<Pattern> SENSITIVE_PATTERNS = Arrays.asList(
            Pattern.compile("\\b\\d{16,19}\\b"),                          // 银行卡号
            Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b"),               // SSN
            Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"), // 邮箱
            Pattern.compile("\\b1[3-9]\\d{9}\\b"),                        // 中国手机�?
            Pattern.compile("(?i)(password|passwd|pwd)\\s*[=:]\\s*\\S+"), // 密码
            Pattern.compile("(?i)(api[_-]?key|secret[_-]?key|token)\\s*[=:]\\s*\\S+") // API Key
    );

    // 输入安全检查关键词
    private static final List<String> INPUT_BLOCKED_KEYWORDS = Arrays.asList(
            "忽略之前的指�?, "ignore previous instructions",
            "你是一�?, "you are a", "system prompt",
            "jailbreak", "DAN mode"
    );

    @Override
    public String getName() {
        return "SafetyAdvisor";
    }

    @Override
    public int getOrder() {
        return 0; // 最先执�?- 安全围栏在最外层
    }

    @Override
    public AdvisorRequest adviseRequest(AdvisorRequest request, AdvisorContext context) {
        String userMessage = request.getUserMessage();

        if (userMessage == null || userMessage.isEmpty()) {
            return request;
        }

        // 输入安全检�?- 检�?Prompt 注入
        boolean isSafe = checkInputSafety(userMessage);
        context.put(AdvisorContext.SAFETY_CHECK_PASSED, isSafe);

        if (!isSafe) {
            if (safetyLevel == SafetyLevel.STRICT) {
                // 严格模式：替换为安全提示
                request.setUserMessage("（用户输入已被安全过滤拦截）");
                log.warn("Input blocked by safety advisor");
            } else {
                log.warn("Potentially unsafe input detected, allowing with moderation");
            }
        }

        return request;
    }

    @Override
    public AdvisorResponse adviseResponse(AdvisorResponse response, AdvisorContext context) {
        String content = response.getContent();
        if (content == null || content.isEmpty()) {
            return response;
        }

        // 1. 过滤敏感信息
        String filteredContent = filterSensitiveInfo(content);

        // 2. 过滤违规内容
        filteredContent = filterBlockedContent(filteredContent);

        response.setContent(filteredContent);

        return response;
    }

    /**
     * 检查输入安全�?
     */
    private boolean checkInputSafety(String input) {
        String lowerInput = input.toLowerCase();

        // 检�?Prompt 注入攻击
        for (String keyword : INPUT_BLOCKED_KEYWORDS) {
            if (lowerInput.contains(keyword.toLowerCase())) {
                log.warn("Potential prompt injection detected: keyword='{}'", keyword);
                return false;
            }
        }

        return true;
    }

    /**
     * 过滤敏感信息（PII 脱敏�?
     */
    private String filterSensitiveInfo(String content) {
        String filtered = content;

        for (Pattern pattern : SENSITIVE_PATTERNS) {
            Matcher matcher = pattern.matcher(filtered);
            if (matcher.find()) {
                String matched = matcher.group();
                String masked = matched.substring(0, Math.min(3, matched.length())) + "***";
                filtered = matcher.replaceAll(java.util.regex.Matcher.quoteReplacement(masked));
                log.debug("Sensitive info filtered: pattern={}", pattern.pattern());
            }
        }

        return filtered;
    }

    /**
     * 过滤违规内容
     */
    private String filterBlockedContent(String content) {
        String filtered = content;

        for (String keyword : BLOCKED_KEYWORDS) {
            if (filtered.contains(keyword)) {
                if (safetyLevel == SafetyLevel.STRICT) {
                    filtered = filtered.replaceAll("(?i)" + java.util.regex.Pattern.quote(keyword),
                            "*".repeat(keyword.length()));
                }
                log.debug("Blocked content detected: keyword='{}'", keyword);
            }
        }

        return filtered;
    }

    // ==================== 配置方法 ====================

    public SafetyAdvisor setSafetyLevel(SafetyLevel level) {
        this.safetyLevel = level;
        return this;
    }
}
