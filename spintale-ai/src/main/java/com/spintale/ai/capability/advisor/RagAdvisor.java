package com.spintale.ai.capability.advisor;

import com.spintale.ai.capability.advisor.Advisor;
import com.spintale.ai.capability.advisor.AdvisorContext;
import com.spintale.ai.capability.advisor.AdvisorRequest;
import com.spintale.ai.capability.advisor.AdvisorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * RAG 上下文增强 Advisor
 *
 * 请求阶段：
 * 1. 使用查询改写优化用户问题
 * 2. 从向量数据库检索相关文档
 * 3. 将检索结果注入系统提示词
 *
 * 响应阶段：
 * - 添加引用来源信息
 *
 * 改进点：
 * - 增加查询改写步骤（Query Rewriting）
 * - 支持 Hybrid Retriever（关键词 + 向量）
 * - 增加引用溯源
 */
public class RagAdvisor implements Advisor {

    private static final Logger log = LoggerFactory.getLogger(RagAdvisor.class);

    private final com.spintale.ai.retrieval.embedding.RetrievalService retrievalService;

    /** 检索文档数量 */
    private int maxRetrievedDocs = 5;

    /** 最低相似度分数 */
    private double minScore = 0.5;

    /** 是否启用查询改写 */
    private boolean queryRewritingEnabled = false;

    /** RAG 上下文注入模板 */
    private static final String RAG_CONTEXT_TEMPLATE = """
            ---
            **参考资料**（请基于以下资料回答，如果资料不足以回答，请明确说明）:
            
            %s
            ---
            """;

    public RagAdvisor(com.spintale.ai.retrieval.embedding.RetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    @Override
    public String getName() {
        return "RagAdvisor";
    }

    @Override
    public int getOrder() {
        return 400; // 在记忆注入之后
    }

    @Override
    public AdvisorRequest adviseRequest(AdvisorRequest request, AdvisorContext context) {
        if (retrievalService == null) {
            return request;
        }

        try {
            // 1. 查询改写（可选）
            String searchQuery = request.getUserMessage();
            if (queryRewritingEnabled) {
                searchQuery = rewriteQuery(request.getUserMessage());
                log.info("Query rewritten: '{}' -> '{}'", request.getUserMessage(), searchQuery);
            }

            // 2. 检索相关文档
            List<?> results = retrievalService.search(searchQuery, maxRetrievedDocs, minScore);
            context.put(AdvisorContext.RETRIEVED_DOCUMENTS, results);

            // 3. 格式化检索结果并注入提示词
            String ragContext = formatRetrievedResults(results);
            if (!ragContext.isEmpty()) {
                String enhancedPrompt = (request.getSystemPrompt() != null ? request.getSystemPrompt() : "")
                        + "\n\n" + String.format(RAG_CONTEXT_TEMPLATE, ragContext);
                request.setSystemPrompt(enhancedPrompt);
            }

            log.info("RAG retrieval: query='{}', results={}", searchQuery, results.size());

        } catch (Exception e) {
            log.error("RAG retrieval failed: {}", e.getMessage(), e);
            // RAG 失败不阻塞主流程，降级为无 RAG
        }

        return request;
    }

    @Override
    public AdvisorResponse adviseResponse(AdvisorResponse response, AdvisorContext context) {
        // 添加引用来源信息到响应元数据
        Object documents = context.get(AdvisorContext.RETRIEVED_DOCUMENTS);
        if (documents != null) {
            response.setMetadata("rag_sources_count", ((List<?>) documents).size());
        }
        return response;
    }

    /**
     * 查询改写
     * 将用户口语化查询转换为更适合检索的形式
     */
    private String rewriteQuery(String originalQuery) {
        // 简单的查询改写规则（生产环境应使用 LLM）
        String rewritten = originalQuery;

        // 去除无意义的口语前缀
        rewritten = rewritten.replaceAll("^(我想知道|请问|帮我查一下|告诉我)\\s*", "");

        // 去除语气词
        rewritten = rewritten.replaceAll("(吧|呢|啊|吗|呀|哦)+$", "");

        return rewritten.trim();
    }

    /**
     * 格式化检索结果
     */
    private String formatRetrievedResults(List<?> results) {
        if (results == null || results.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < results.size(); i++) {
            Object result = results.get(i);
            sb.append("[").append(i + 1).append("] ");

            // 处理 EmbeddingMatch 类型
            if (result instanceof dev.langchain4j.store.embedding.EmbeddingMatch<?> match) {
                sb.append(match.embedded() != null ? match.embedded().toString() : "");
                sb.append(" (相关度: ").append(String.format("%.2f", match.score())).append(")");
            } else {
                sb.append(result.toString());
            }
            sb.append("\n\n");
        }

        return sb.toString();
    }

    // ==================== 配置方法 ====================

    public RagAdvisor setMaxRetrievedDocs(int max) {
        this.maxRetrievedDocs = max;
        return this;
    }

    public RagAdvisor setMinScore(double minScore) {
        this.minScore = minScore;
        return this;
    }

    public RagAdvisor setQueryRewritingEnabled(boolean enabled) {
        this.queryRewritingEnabled = enabled;
        return this;
    }
}
