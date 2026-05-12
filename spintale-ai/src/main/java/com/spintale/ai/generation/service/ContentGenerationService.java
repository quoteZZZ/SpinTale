package com.spintale.ai.generation.service;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.spintale.ai.generation.model.ContentType;
import com.spintale.ai.generation.model.GenerationRequest;
import com.spintale.ai.generation.model.GenerationResponse;
import com.spintale.ai.generation.model.TextGenerationRequest;
import com.spintale.ai.generation.model.TextGenerationResponse;
import com.spintale.ai.generation.template.ContentTemplate;

@Service
public class ContentGenerationService
{
    private static final Logger log = LoggerFactory.getLogger(ContentGenerationService.class);

    private final TextGenerationService textGenerationService;
    private final Map<String, ContentTemplate> templateRegistry = new ConcurrentHashMap<>();

    public ContentGenerationService(TextGenerationService textGenerationService)
    {
        this.textGenerationService = textGenerationService;
    }

    public void registerTemplate(ContentTemplate template)
    {
        if (template != null)
        {
            templateRegistry.put(template.getContentType(), template);
        }
    }

    public void registerTemplates(Collection<ContentTemplate> templates)
    {
        if (templates != null)
        {
            templates.forEach(this::registerTemplate);
        }
    }

    public ContentTemplate getTemplate(String contentType)
    {
        return templateRegistry.get(contentType);
    }

    public Set<String> getSupportedContentTypes()
    {
        return templateRegistry.keySet();
    }

    public GenerationResponse generate(GenerationRequest request)
    {
        try
        {
            ContentType contentType = request.getContentType();
            if (contentType == null)
            {
                return GenerationResponse.error("contentType is required");
            }
            ContentTemplate template = templateRegistry.get(contentType.getCode());
            TextGenerationRequest textRequest = toTextRequest(request, template);
            TextGenerationResponse textResponse = textGenerationService.generate(resolveAction(contentType), textRequest);

            GenerationResponse response = new GenerationResponse();
            response.setContent(textResponse.getContent());
            response.setContentType(contentType);
            response.setModel(textResponse.getModel());
            response.setCompleted(true);
            response.setMetadata(textResponse);
            return response;
        }
        catch (Exception e)
        {
            log.error("Content generation failed", e);
            return GenerationResponse.error(e.getMessage());
        }
    }

    public GenerationResponse generateArticle(String title, String keywords, String description)
    {
        return generate(GenerationRequest.builder()
                .contentType(ContentType.ARTICLE)
                .title(title)
                .keywords(keywords)
                .description(description)
                .build());
    }

    public GenerationResponse generateNovel(String title, String genre, String description, String characters)
    {
        GenerationRequest request = GenerationRequest.builder()
                .contentType(ContentType.NOVEL)
                .title(title)
                .description(description)
                .build();
        request.putExtraParam("genre", genre);
        request.putExtraParam("characters", characters);
        return generate(request);
    }

    public GenerationResponse generateAdCopy(String productName, String productFeatures, String targetAudience)
    {
        GenerationRequest request = GenerationRequest.builder()
                .contentType(ContentType.AD_COPY)
                .title(productName)
                .targetAudience(targetAudience)
                .build();
        request.putExtraParam("productName", productName);
        request.putExtraParam("productFeatures", productFeatures);
        return generate(request);
    }

    public void generateStream(GenerationRequest request, StreamHandler handler)
    {
        GenerationResponse response = generate(request);
        if (response.isCompleted())
        {
            handler.onComplete(response.getContent());
        }
        else
        {
            handler.onError(response.getErrorMessage());
        }
    }

    private TextGenerationRequest toTextRequest(GenerationRequest request, ContentTemplate template)
    {
        TextGenerationRequest textRequest = new TextGenerationRequest();
        textRequest.setApiKey(request.getApiKey());
        textRequest.setModelName(request.getModelName());
        textRequest.setTitle(request.getTitle());
        textRequest.setAudience(request.getTargetAudience());
        textRequest.setTone(request.getTone());
        textRequest.setLanguage(request.getLanguage());
        textRequest.setLengthPreset(request.getLength());
        if (template != null)
        {
            textRequest.setSystemPrompt(template.getSystemPrompt());
            textRequest.setPrompt(template.buildPrompt(buildParams(request)));
        }
        else
        {
            textRequest.setPrompt(request.getDescription());
        }
        return textRequest;
    }

    private Map<String, Object> buildParams(GenerationRequest request)
    {
        Map<String, Object> params = new java.util.LinkedHashMap<>();
        params.put("title", request.getTitle());
        params.put("keywords", request.getKeywords());
        params.put("description", request.getDescription());
        params.put("targetAudience", request.getTargetAudience());
        params.put("tone", request.getTone());
        params.put("length", request.getLength());
        params.put("language", request.getLanguage());
        if (request.getExtraParams() != null)
        {
            params.putAll(request.getExtraParams());
        }
        return params;
    }

    private String resolveAction(ContentType contentType)
    {
        if (ContentType.NOVEL.equals(contentType))
        {
            return "novel";
        }
        if (ContentType.ARTICLE.equals(contentType))
        {
            return "article";
        }
        return "generate";
    }

    public interface StreamHandler
    {
        void onToken(String token);
        void onComplete(String content);
        void onError(String error);
    }
}
