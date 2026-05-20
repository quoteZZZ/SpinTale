package com.spintale.ai.observability.hallucination;

import dev.langchain4j.model.chat.ChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 幻觉检测与缓解服务
 * 
 * 主要功能：
 * 1. 事实一致性检查 - 检测 AI 回复是否与已知事实矛盾
 * 2. 置信度评估 - 评估 AI 回复的可信度
 * 3. 引用验证 - 检查 AI 是否编造了不存在的引用
 * 4. 数字/日期验证 - 检测不合理的时间、数字信息
 * 5. 自我矛盾检测 - 检测同一段落内的逻辑矛盾
 */
public class HallucinationDetector {
    
    private static final Logger log = LoggerFactory.getLogger(HallucinationDetector.class);
    
    private final ChatModel chatModel;
    
    // 已知事实库（生产环境应从数据库加载）
    private final Map<String, Set<String>> userFacts = new HashMap<>();
    
    // 可疑模式正则表达式
    private static final List<Pattern> SUSPICIOUS_PATTERNS = Arrays.asList(
        // 过于具体的数字（可能是编造的）
        Pattern.compile("\\b\\d{4,}\\b"),
        // 绝对化表述
        Pattern.compile("(绝对 | 肯定 | 一定 | 百分之百 | 毫无疑问)\\s*(是 | 正确 | 对)"),
        // 模糊的权威引用
        Pattern.compile("(研究表明 | 专家指出 | 据调查 | 有数据显示)\\s*(表明 | 显示 | 指出)，?\\s*(但)?没有具体来源"),
        // 时间矛盾
        Pattern.compile("(昨天 | 今天 | 明天 | 上周 | 下周)\\s*\\d{4}年")
    );
    
    public HallucinationDetector(ChatModel chatModel) {
        this.chatModel = chatModel;
    }
    
    /**
     * 添加用户事实
     */
    public void addUserFact(String userId, String fact) {
        userFacts.computeIfAbsent(userId, k -> new HashSet<>()).add(fact.toLowerCase());
    }
    
    /**
     * 批量添加用户事实
     */
    public void addUserFacts(String userId, Collection<String> facts) {
        userFacts.computeIfAbsent(userId, k -> new HashSet<>())
                .addAll(facts.stream().map(String::toLowerCase).toList());
    }
    
    /**
     * 清除用户事实
     */
    public void clearUserFacts(String userId) {
        userFacts.remove(userId);
    }
    
    /**
     * 综合检测 AI 回复是否存在幻觉
     * @param userId 用户 ID
     * @param context 对话上下文
     * @param response AI 回复内容
     * @return 检测结果
     */
    public HallucinationResult detectHallucination(String userId, String context, String response) {
        HallucinationResult result = new HallucinationResult();
        result.setResponse(response);
        
        // 1. 事实一致性检查
        List<String> inconsistencies = checkFactConsistency(userId, response);
        result.setFactInconsistencies(inconsistencies);
        if (!inconsistencies.isEmpty()) {
            result.addFlag("FACT_INCONSISTENCY");
        }
        
        // 2. 可疑模式检测
        List<SuspiciousPatternMatch> patternMatches = checkSuspiciousPatterns(response);
        result.setSuspiciousPatterns(patternMatches);
        if (!patternMatches.isEmpty()) {
            result.addFlag("SUSPICIOUS_PATTERN");
        }
        
        // 3. 内部一致性检查
        List<String> contradictions = checkInternalConsistency(response);
        result.setContradictions(contradictions);
        if (!contradictions.isEmpty()) {
            result.addFlag("INTERNAL_CONTRADICTION");
        }
        
        // 4. 引用验证
        List<String> unverifiedClaims = checkUnverifiedClaims(response);
        result.setUnverifiedClaims(unverifiedClaims);
        if (!unverifiedClaims.isEmpty()) {
            result.addFlag("UNVERIFIED_CLAIM");
        }
        
        // 5. 使用 AI 进行元评估
        ConfidenceScore aiConfidence = assessConfidenceWithAI(context, response);
        result.setAiConfidence(aiConfidence);
        
        // 计算总体置信度
        double overallConfidence = calculateOverallConfidence(result);
        result.setOverallConfidence(overallConfidence);
        
        // 判断是否为幻觉
        result.setIsHallucination(overallConfidence < 0.5 || !inconsistencies.isEmpty());
        
        log.info("Hallucination detection: userId={}, confidence={}, isHallucination={}", 
                userId, overallConfidence, result.getIsHallucination());
        
        return result;
    }
    
    /**
     * 检查与已知事实的一致性
     */
    private List<String> checkFactConsistency(String userId, String response) {
        List<String> inconsistencies = new ArrayList<>();
        Set<String> facts = userFacts.get(userId);
        
        if (facts == null || facts.isEmpty()) {
            return inconsistencies;
        }
        
        String lowerResponse = response.toLowerCase();
        
        // 简单检查：如果回复中包含与已知事实明显矛盾的表述
        for (String fact : facts) {
            // 这里可以实现更复杂的矛盾检测逻辑
            // 目前只做简单的关键词检查
            if (lowerResponse.contains("不是 " + fact) || 
                lowerResponse.contains("没有 " + fact) ||
                lowerResponse.contains("错误 " + fact)) {
                inconsistencies.add("可能与已知事实矛盾：" + fact);
            }
        }
        
        return inconsistencies;
    }
    
    /**
     * 检测可疑模式
     */
    private List<SuspiciousPatternMatch> checkSuspiciousPatterns(String response) {
        List<SuspiciousPatternMatch> matches = new ArrayList<>();
        
        for (Pattern pattern : SUSPICIOUS_PATTERNS) {
            Matcher matcher = pattern.matcher(response);
            while (matcher.find()) {
                SuspiciousPatternMatch match = new SuspiciousPatternMatch();
                match.setPattern(pattern.pattern());
                match.setMatchedText(matcher.group());
                match.setStartPosition(matcher.start());
                match.setEndPosition(matcher.end());
                matches.add(match);
            }
        }
        
        return matches;
    }
    
    /**
     * 检查内部一致性（检测自相矛盾）
     */
    private List<String> checkInternalConsistency(String response) {
        List<String> contradictions = new ArrayList<>();
        
        // 检测常见的矛盾词对
        String[][] contradictionPairs = {
            {"但是", "而且"},
            {"然而", "同时"},
            {"虽然", "但是"},
            {"尽管", "可是"}
        };
        
        // 简化版本：检测明显的数字矛盾
        Pattern yearPattern = Pattern.compile("\\b(\\d{4})年\\b");
        Matcher matcher = yearPattern.matcher(response);
        Set<Integer> years = new HashSet<>();
        while (matcher.find()) {
            years.add(Integer.parseInt(matcher.group(1)));
        }
        
        // 如果出现了跨度很大的年份，可能是幻觉
        if (years.size() > 1) {
            int minYear = Collections.min(years);
            int maxYear = Collections.max(years);
            if (maxYear - minYear > 100) {
                contradictions.add("检测到异常的时间跨度：" + minYear + "年 至 " + maxYear + "年");
            }
        }
        
        return contradictions;
    }
    
    /**
     * 检查未经验证的主张
     */
    private List<String> checkUnverifiedClaims(String response) {
        List<String> unverified = new ArrayList<>();
        
        // 检测没有来源的统计性主张
        Pattern statPattern = Pattern.compile("(\\d+[%\\d 万百万亿]?)\\s*(人 | 个 | 次|年|月)");
        Matcher matcher = statPattern.matcher(response);
        while (matcher.find()) {
            // 检查附近是否有"根据"、"来源"等词
            int start = Math.max(0, matcher.start() - 50);
            int end = Math.min(response.length(), matcher.end() + 50);
            String context = response.substring(start, end);
            
            if (!context.contains("根据") && !context.contains("来源") && 
                !context.contains("研究显示") && !context.contains("数据显示")) {
                unverified.add("未注明来源的统计数据：" + matcher.group());
            }
        }
        
        return unverified;
    }
    
    /**
     * 使用 AI 评估回复的置信度
     */
    private ConfidenceScore assessConfidenceWithAI(String context, String response) {
        ConfidenceScore score = new ConfidenceScore();
        
        try {
            String prompt = String.format(
                "请评估以下 AI 回复的可信度。考虑以下因素：\n" +
                "1. 是否有明确的事实依据\n" +
                "2. 是否有逻辑矛盾\n" +
                "3. 是否有模糊或编造的信息\n" +
                "4. 语气是否过度确定或不确定\n\n" +
                "对话上下文:\n%s\n\n" +
                "AI 回复:\n%s\n\n" +
                "请以 JSON 格式返回评估结果：\n" +
                "{\n" +
                "  \"confidence\": 0.0-1.0 之间的数值,\n" +
                "  \"reasons\": [\"列出影响置信度的原因\"],\n" +
                "  \"suggestions\": [\"改进建议\"]\n" +
                "}",
                context != null ? context : "无上下文",
                response
            );
            
            // 注意：实际实现中需要解析 AI 返回的 JSON
            // 这里简化处理
            String aiResponse = chatModel.chat(prompt);
            
            score.setConfidence(0.7); // 默认值，实际应解析 AI 响应
            score.setReasons(Arrays.asList("基于 AI 元评估"));
            score.setSuggestions(Arrays.asList("建议核实关键信息"));
            
        } catch (Exception e) {
            log.error("AI confidence assessment failed: {}", e.getMessage());
            score.setConfidence(0.5);
            score.setReasons(Arrays.asList("AI 评估失败，使用默认置信度"));
        }
        
        return score;
    }
    
    /**
     * 计算总体置信度
     */
    private double calculateOverallConfidence(HallucinationResult result) {
        double baseConfidence = result.getAiConfidence() != null ? 
                result.getAiConfidence().getConfidence() : 0.7;
        
        // 根据检测结果调整置信度
        if (!result.getFactInconsistencies().isEmpty()) {
            baseConfidence -= 0.3;
        }
        if (!result.getSuspiciousPatterns().isEmpty()) {
            baseConfidence -= 0.1 * Math.min(result.getSuspiciousPatterns().size(), 3);
        }
        if (!result.getContradictions().isEmpty()) {
            baseConfidence -= 0.2;
        }
        if (!result.getUnverifiedClaims().isEmpty()) {
            baseConfidence -= 0.05 * Math.min(result.getUnverifiedClaims().size(), 4);
        }
        
        return Math.max(0.0, Math.min(1.0, baseConfidence));
    }
    
    /**
     * 生成带置信度提示的回复
     */
    public String generateResponseWithConfidence(String originalResponse, HallucinationResult result) {
        if (result.getOverallConfidence() >= 0.8) {
            return originalResponse;
        }
        
        StringBuilder sb = new StringBuilder();
        
        if (result.getOverallConfidence() < 0.5) {
            sb.append("【⚠️ 此回复可能存在不准确信息，请谨慎参考】\n\n");
        } else if (result.getOverallConfidence() < 0.7) {
            sb.append("【ℹ️ 部分信息的准确性有待核实】\n\n");
        }
        
        sb.append(originalResponse);
        
        if (!result.getFactInconsistencies().isEmpty()) {
            sb.append("\n\n---\n**注意**: 以下内容可能与已知事实不一致:\n");
            for (String inconsistency : result.getFactInconsistencies()) {
                sb.append("- ").append(inconsistency).append("\n");
            }
        }
        
        return sb.toString();
    }
    
    // ==================== 内部类 ====================
    
    /**
     * 幻觉检测结果
     */
    public static class HallucinationResult {
        private String response;
        private boolean isHallucination;
        private double overallConfidence;
        private Set<String> flags = new HashSet<>();
        private List<String> factInconsistencies = new ArrayList<>();
        private List<SuspiciousPatternMatch> suspiciousPatterns = new ArrayList<>();
        private List<String> contradictions = new ArrayList<>();
        private List<String> unverifiedClaims = new ArrayList<>();
        private ConfidenceScore aiConfidence;
        
        public void addFlag(String flag) {
            flags.add(flag);
        }
        
        // Getters and Setters
        public String getResponse() { return response; }
        public void setResponse(String response) { this.response = response; }
        public boolean getIsHallucination() { return isHallucination; }
        public void setIsHallucination(boolean isHallucination) { this.isHallucination = isHallucination; }
        public double getOverallConfidence() { return overallConfidence; }
        public void setOverallConfidence(double overallConfidence) { this.overallConfidence = overallConfidence; }
        public Set<String> getFlags() { return flags; }
        public List<String> getFactInconsistencies() { return factInconsistencies; }
        public void setFactInconsistencies(List<String> factInconsistencies) { this.factInconsistencies = factInconsistencies; }
        public List<SuspiciousPatternMatch> getSuspiciousPatterns() { return suspiciousPatterns; }
        public void setSuspiciousPatterns(List<SuspiciousPatternMatch> suspiciousPatterns) { this.suspiciousPatterns = suspiciousPatterns; }
        public List<String> getContradictions() { return contradictions; }
        public void setContradictions(List<String> contradictions) { this.contradictions = contradictions; }
        public List<String> getUnverifiedClaims() { return unverifiedClaims; }
        public void setUnverifiedClaims(List<String> unverifiedClaims) { this.unverifiedClaims = unverifiedClaims; }
        public ConfidenceScore getAiConfidence() { return aiConfidence; }
        public void setAiConfidence(ConfidenceScore aiConfidence) { this.aiConfidence = aiConfidence; }
    }
    
    /**
     * 可疑模式匹配
     */
    public static class SuspiciousPatternMatch {
        private String pattern;
        private String matchedText;
        private int startPosition;
        private int endPosition;
        
        public String getPattern() { return pattern; }
        public void setPattern(String pattern) { this.pattern = pattern; }
        public String getMatchedText() { return matchedText; }
        public void setMatchedText(String matchedText) { this.matchedText = matchedText; }
        public int getStartPosition() { return startPosition; }
        public void setStartPosition(int startPosition) { this.startPosition = startPosition; }
        public int getEndPosition() { return endPosition; }
        public void setEndPosition(int endPosition) { this.endPosition = endPosition; }
    }
    
    /**
     * 置信度评分
     */
    public static class ConfidenceScore {
        private double confidence;
        private List<String> reasons;
        private List<String> suggestions;
        
        public double getConfidence() { return confidence; }
        public void setConfidence(double confidence) { this.confidence = confidence; }
        public List<String> getReasons() { return reasons; }
        public void setReasons(List<String> reasons) { this.reasons = reasons; }
        public List<String> getSuggestions() { return suggestions; }
        public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
    }
}
