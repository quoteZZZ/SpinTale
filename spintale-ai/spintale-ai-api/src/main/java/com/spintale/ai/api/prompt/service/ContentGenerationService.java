package com.spintale.ai.generation.service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spintale.ai.generation.model.GenerationRequest;
import com.spintale.ai.generation.model.GenerationRequest.ContentType;
import com.spintale.ai.generation.model.GenerationResponse;
import com.spintale.ai.generation.template.ContentTemplate;

public class ContentGenerationService {

    private static final Logger log = LoggerFactory.getLogger(ContentGenerationService.class);

    private final TextGenerationService textGenerationService;
    private final Map<String, ContentTemplate> templateRegistry = new ConcurrentHashMap<>();

    public ContentGenerationService(TextGenerationService textGenerationService) {
        this.textGenerationService = textGenerationService;
    }

    public void registerTemplate(ContentTemplate template) {
        if (template != null) {
            templateRegistry.put(template.getContentType(), template);
        }
    }

    public void registerTemplates(Collection<ContentTemplate> templates) {
        if (templates != null) {
            templates.forEach(this::registerTemplate);
        }
    }

    public ContentTemplate getTemplate(String contentType) {
        return templateRegistry.get(contentType);
    }

    public Set<String> getSupportedContentTypes() {
        return templateRegistry.keySet();
    }

    public GenerationResponse generate(GenerationRequest request) {
        try {
            if (request == null) {
                return GenerationResponse.error("request is required");
            }

            ContentType contentType = request.getContentType();
            if (contentType == null) {
                return GenerationResponse.error("contentType is required");
            }

            ContentTemplate template = templateRegistry.get(contentType.getCode());
            GenerationPrompt prompt = toGenerationPrompt(request, template);
            String content = textGenerationService.generate(
                    resolveAction(contentType),
                    prompt.prompt(),
                    prompt.systemPrompt());

            GenerationResponse response = new GenerationResponse();
            response.setContent(content);
            response.setContentType(contentType);
            response.setCompleted(true);
            return response;
        } catch (Exception e) {
            log.error("Content generation failed", e);
            return GenerationResponse.error(e.getMessage());
        }
    }

    public GenerationResponse generateArticle(String title, String keywords, String description) {
        return generate(GenerationRequest.builder()
                .contentType(ContentType.ARTICLE)
                .title(title)
                .keywords(keywords)
                .description(description)
                .build());
    }

    public GenerationResponse generateNovel(String title, String genre, String description, String characters) {
        GenerationRequest request = GenerationRequest.builder()
                .contentType(ContentType.NOVEL)
                .title(title)
                .description(description)
                .build();
        request.putExtraParam("genre", genre);
        request.putExtraParam("characters", characters);
        return generate(request);
    }

    public GenerationResponse generateAdCopy(String productName, String productFeatures, String targetAudience) {
        GenerationRequest request = GenerationRequest.builder()
                .contentType(ContentType.AD_COPY)
                .title(productName)
                .targetAudience(targetAudience)
                .build();
        request.putExtraParam("productName", productName);
        request.putExtraParam("productFeatures", productFeatures);
        return generate(request);
    }

    public void generateStream(GenerationRequest request, StreamHandler handler) {
        GenerationResponse response = generate(request);
        if (response.isCompleted()) {
            handler.onComplete(response.getContent());
        } else {
            handler.onError(response.getErrorMessage());
        }
    }

    private GenerationPrompt toGenerationPrompt(GenerationRequest request, ContentTemplate template) {
        if (template != null) {
            return new GenerationPrompt(template.buildPrompt(buildParams(request)), template.getSystemPrompt());
        }
        return new GenerationPrompt(request.getDescription(), null);
    }

    private Map<String, Object> buildParams(GenerationRequest request) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("title", request.getTitle());
        params.put("keywords", request.getKeywords());
        params.put("description", request.getDescription());
        params.put("targetAudience", request.getTargetAudience());
        params.put("tone", request.getTone());
        params.put("length", request.getLength());
        params.put("language", request.getLanguage());
        if (request.getExtraParams() != null) {
            params.putAll(request.getExtraParams());
        }
        return params;
    }

    private String resolveAction(ContentType contentType) {
        if (ContentType.NOVEL.equals(contentType)) {
            return "novel";
        }
        if (ContentType.ARTICLE.equals(contentType)) {
            return "article";
        }
        if (ContentType.AD_COPY.equals(contentType)) {
            return "ad_copy";
        }
        return "generate";
    }

    public interface StreamHandler {
        void onToken(String token);

        void onComplete(String content);

        void onError(String error);
    }

    private record GenerationPrompt(String prompt, String systemPrompt) {
    }
}
