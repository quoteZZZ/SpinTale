package com.spintale.ai.generation.service;

import com.spintale.ai.core.AiChatService;
import com.spintale.ai.core.ChatRequest;
import com.spintale.ai.core.ChatMessage;
import com.spintale.ai.core.ChatResponse;
import com.spintale.ai.generation.model.ContentType;
import com.spintale.ai.generation.model.GenerationRequest;
import com.spintale.ai.generation.model.GenerationResponse;
import com.spintale.ai.generation.template.ContentTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 内容生成服务
 * 
 * 提供统一的内容生成接口，支持多种内容类型
 */
@Service
public class ContentGenerationService {

    private static final Logger log = LoggerFactory.getLogger(ContentGenerationService.class);

    @Autowired
    private AiChatService aiChatService;

    /**
     * 模板注册表
     */
    private final Map<String, ContentTemplate> templateRegistry = new ConcurrentHashMap<>();

    /**
     * 注册内容模板
     */
    public void registerTemplate(ContentTemplate template) {
        if (template != null) {
            templateRegistry.put(template.getContentType(), template);
            log.info("注册内容模板：{}", template.getContentType());
        }
    }

    /**
     * 批量注册模板
     */
    public void registerTemplates(Collection<ContentTemplate> templates) {
        if (templates != null) {
            for (ContentTemplate template : templates) {
                registerTemplate(template);
            }
        }
    }

    /**
     * 获取已注册的模板
     */
    public ContentTemplate getTemplate(String contentType) {
        return templateRegistry.get(contentType);
    }

    /**
     * 获取所有已注册的内容类型
     */
    public Set<String> getSupportedContentTypes() {
        return templateRegistry.keySet();
    }

    /**
     * 生成内容（同步）
     * 
     * @param request 生成请求
     * @return 生成响应
     */
    public GenerationResponse generate(GenerationRequest request) {
        try {
            // 获取对应的模板
            ContentType contentType = request.getContentType();
            if (contentType == null) {
                return GenerationResponse.error("未指定内容类型");
            }

            ContentTemplate template = templateRegistry.get(contentType.getCode());
            if (template == null) {
                log.warn("未找到内容类型 {} 的模板，使用默认模板", contentType.getCode());
                template = createDefaultTemplate();
            }

            // 构建参数
            Map<String, Object> params = buildParams(request);

            // 构建 Prompt
            String userPrompt = template.buildPrompt(params);
            String systemPrompt = template.getSystemPrompt();

            log.info("生成内容，类型：{}, 标题：{}", contentType.getCode(), request.getTitle());

            // 调用 AI 服务
            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(Arrays.asList(
                            ChatMessage.system(systemPrompt),
                            ChatMessage.user(userPrompt)
                    ))
                    .stream(request.isStream())
                    .build();

            ChatResponse chatResponse = aiChatService.chat(chatRequest);

            // 构建响应
            GenerationResponse response = new GenerationResponse();
            response.setContent(chatResponse.getContent());
            response.setContentType(contentType);
            response.setModel(chatResponse.getModel());
            response.setTokenUsage(chatResponse.getTokenUsage());
            response.setCompleted(true);

            // 后处理
            String processedContent = template.postProcess(response.getContent(), params);
            response.setContent(processedContent);

            log.info("内容生成完成，类型：{}, Token 使用：{}", 
                    contentType.getCode(), 
                    response.getTokenUsage() != null ? response.getTokenUsage().getTotalTokens() : "N/A");

            return response;

        } catch (Exception e) {
            log.error("内容生成失败", e);
            return GenerationResponse.error("生成失败：" + e.getMessage());
        }
    }

    /**
     * 生成文章
     */
    public GenerationResponse generateArticle(String title, String keywords, String description) {
        GenerationRequest request = GenerationRequest.builder()
                .contentType(ContentType.ARTICLE)
                .title(title)
                .keywords(keywords)
                .description(description)
                .build();
        return generate(request);
    }

    /**
     * 生成小说/故事
     */
    public GenerationResponse generateNovel(String title, String genre, String description, String characters) {
        GenerationRequest request = GenerationRequest.builder()
                .contentType(ContentType.NOVEL)
                .title(title)
                .description(description)
                .tone("生动")
                .build();
        request.putExtraParam("genre", genre);
        request.putExtraParam("characters", characters);
        return generate(request);
    }

    /**
     * 生成广告词
     */
    public GenerationResponse generateAdCopy(String productName, String productFeatures, String targetAudience) {
        GenerationRequest request = GenerationRequest.builder()
                .contentType(ContentType.AD_COPY)
                .title(productName)
                .targetAudience(targetAudience)
                .tone("活力")
                .build();
        request.putExtraParam("productName", productName);
        request.putExtraParam("productFeatures", productFeatures);
        return generate(request);
    }

    /**
     * 流式生成内容
     * 
     * @param request 生成请求
     * @param handler 流式处理器
     */
    public void generateStream(GenerationRequest request, StreamHandler handler) {
        try {
            ContentType contentType = request.getContentType();
            if (contentType == null) {
                handler.onError("未指定内容类型");
                return;
            }

            ContentTemplate template = templateRegistry.get(contentType.getCode());
            if (template == null) {
                template = createDefaultTemplate();
            }

            Map<String, Object> params = buildParams(request);
            String userPrompt = template.buildPrompt(params);
            String systemPrompt = template.getSystemPrompt();

            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(Arrays.asList(
                            ChatMessage.system(systemPrompt),
                            ChatMessage.user(userPrompt)
                    ))
                    .stream(true)
                    .build();

            aiChatService.chatStream(chatRequest, new com.spintale.ai.core.StreamHandler() {
                @Override
                public void onToken(String token) {
                    handler.onToken(token);
                }

                @Override
                public void onComplete(String content) {
                    handler.onComplete(content);
                }

                @Override
                public void onError(String error) {
                    handler.onError(error);
                }
            });

        } catch (Exception e) {
            log.error("流式生成失败", e);
            handler.onError("生成失败：" + e.getMessage());
        }
    }

    /**
     * 构建参数 Map
     */
    private Map<String, Object> buildParams(GenerationRequest request) {
        Map<String, Object> params = new HashMap<>();
        params.put("title", request.getTitle());
        params.put("keywords", request.getKeywords());
        params.put("description", request.getDescription());
        params.put("targetAudience", request.getTargetAudience());
        params.put("tone", request.getTone());
        params.put("length", request.getLength());
        params.put("language", request.getLanguage());
        
        // 合并额外参数
        if (request.getExtraParams() != null) {
            params.putAll(request.getExtraParams());
        }
        
        return params;
    }

    /**
     * 创建默认模板（兜底）
     */
    private ContentTemplate createDefaultTemplate() {
        return new ContentTemplate() {
            @Override
            public String getContentType() {
                return "custom";
            }

            @Override
            public String buildPrompt(Map<String, Object> params) {
                StringBuilder prompt = new StringBuilder();
                String title = (String) params.getOrDefault("title", "");
                String description = (String) params.getOrDefault("description", "");
                String language = (String) params.getOrDefault("language", "zh");

                if (!title.isEmpty()) {
                    prompt.append("主题：").append(title).append("\n\n");
                }
                if (!description.isEmpty()) {
                    prompt.append("要求：\n").append(description).append("\n\n");
                }
                
                prompt.append("请根据以上要求创作内容。");
                
                if ("zh".equals(language)) {
                    prompt.append("请使用中文回复。");
                }

                return prompt.toString();
            }
        };
    }

    /**
     * 流式处理器接口
     */
    public interface StreamHandler {
        void onToken(String token);
        void onComplete(String content);
        void onError(String error);
    }
}
