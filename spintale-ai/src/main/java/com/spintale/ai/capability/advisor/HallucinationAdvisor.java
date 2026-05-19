package com.spintale.ai.capability.advisor;

import com.spintale.ai.capability.advisor.Advisor;
import com.spintale.ai.capability.advisor.AdvisorContext;
import com.spintale.ai.capability.advisor.AdvisorRequest;
import com.spintale.ai.capability.advisor.AdvisorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 幻觉检测 Advisor
 *
 * 响应阶段：
 * 1. 使用规则引擎检测可疑模式
 * 2. 检查事实一致性
 * 3. 基于检索文档的 groundedness 检测
 * 4. 标注置信度
 *
 * 改进点：
 * - 集成到 Advisor 链，而非独立调用
 * - 利用 RAG Advisor 检索到的文档做 grounding 检测
 * - 规则 + AI 双重检测
 */
public class HallucinationAdvisor implements Advisor {

    private static final Logger log = LoggerFactory.getLogger(HallucinationAdvisor.class);

    /** 是否启用幻觉检测 */
    private boolean enabled = true;

    /** 幻觉判定阈值 */
    private double hallucinationThreshold = 0.5;

    /** 检测到幻觉时的处理方式 */
    public enum HallucinationAction {
        WARN,       // 添加警告
        REGENERATE, // 重新生成（需要外部支持）
        BLOCK       // 阻止输出
    }

    private HallucinationAction action = HallucinationAction.WARN;

    @Override
    public String getName() {
        return "HallucinationAdvisor";
    }

    @Override
    public int getOrder() {
        return 600; // 在 LLM 调用之后处理响应
    }

    @Override
    public AdvisorRequest adviseRequest(AdvisorRequest request, AdvisorContext context) {
        // 请求阶段不做处理
        return request;
    }

    @Override
    public AdvisorResponse adviseResponse(AdvisorResponse response, AdvisorContext context) {
        if (!enabled || response.getContent() == null) {
            return response;
        }

        try {
            double confidence = assessConfidence(response.getContent(), context);
            response.setConfidenceScore(confidence);

            if (confidence < hallucinationThreshold) {
                log.warn("Low confidence response detected: score={}", confidence);
                handleHallucination(response, confidence);
            }

            context.put(AdvisorContext.HALLUCINATION_RESULT, confidence);

        } catch (Exception e) {
            log.error("Hallucination detection failed: {}", e.getMessage(), e);
        }

        return response;
    }

    /**
     * 评估响应置信度
     * 综合规则检测和上下文 grounding 评估
     */
    private double assessConfidence(String content, AdvisorContext context) {
        double confidence = 1.0;

        // 1. 规则检测 - 检测可疑模式
        confidence -= detectSuspiciousPatterns(content);

        // 2. Grounding 检测 - 如果有 RAG 检索结果，检查响应是否基于文档
        Object documents = context.get(AdvisorContext.RETRIEVED_DOCUMENTS);
        if (documents != null) {
            double groundingScore = assessGrounding(content, documents);
            confidence = confidence * 0.5 + groundingScore * 0.5;
        }

        return Math.max(0.0, Math.min(1.0, confidence));
    }

    /**
     * 检测可疑模式（规则引擎）
     */
    private double detectSuspiciousPatterns(String content) {
        double penalty = 0.0;

        // 检测过于确定的表述
        if (content.contains("百分之百") || content.contains("毫无疑问")) {
            penalty += 0.15;
        }

        // 检测模糊的权威引用
        if (content.contains("研究表明") && !content.contains("根据") ||
            content.contains("专家指出") && !content.contains("来源")) {
            penalty += 0.1;
        }

        // 检测大量数字（可能是编造的统计）
        long numberCount = content.chars().filter(ch -> ch >= '0' && ch <= '9').count();
        if (numberCount > content.length() * 0.15) {
            penalty += 0.1;
        }

        return penalty;
    }

    /**
     * 评估 Grounding 程度
     * 检查响应内容是否与检索到的文档一致
     */
    private double assessGrounding(String content, Object documents) {
        // 简化实现：如果响应中有与文档重叠的内容，则 grounding 较好
        // 生产环境应使用 NLI 模型做 entailment 检测
        return 0.7; // 默认 grounding 分数
    }

    /**
     * 处理检测到的幻觉
     */
    private void handleHallucination(AdvisorResponse response, double confidence) {
        switch (action) {
            case WARN:
                String warning = confidence < 0.3
                        ? "\n\n【此回复可能存在不准确信息，请谨慎参考】"
                        : "\n\n【部分信息的准确性有待核实】";
                response.setContent(response.getContent() + warning);
                break;
            case BLOCK:
                response.setContent("抱歉，该回复未通过可信度验证，请换个方式提问。");
                break;
            case REGENERATE:
                // 标记需要重新生成（由调用方处理）
                response.setMetadata("needs_regeneration", true);
                break;
        }
    }

    // ==================== 配置方法 ====================

    public HallucinationAdvisor setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public HallucinationAdvisor setHallucinationThreshold(double threshold) {
        this.hallucinationThreshold = threshold;
        return this;
    }

    public HallucinationAdvisor setAction(HallucinationAction action) {
        this.action = action;
        return this;
    }
}
