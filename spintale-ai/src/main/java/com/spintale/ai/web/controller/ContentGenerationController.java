package com.spintale.ai.web.controller;

import java.io.IOException;
import java.util.Set;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.spintale.ai.generation.model.ContentType;
import com.spintale.ai.generation.model.GenerationRequest;
import com.spintale.ai.generation.model.GenerationResponse;
import com.spintale.ai.generation.service.ContentGenerationService;
import com.spintale.common.core.domain.AjaxResult;
import com.spintale.common.utils.StringUtils;

@RestController
@RequestMapping("/ai/generate")
public class ContentGenerationController
{
    private final ContentGenerationService generationService;

    public ContentGenerationController(ContentGenerationService generationService)
    {
        this.generationService = generationService;
    }

    @GetMapping("/types")
    public AjaxResult getSupportedContentTypes()
    {
        Set<String> types = generationService.getSupportedContentTypes();
        return AjaxResult.success(types);
    }

    @PostMapping("/content")
    public AjaxResult generateContent(@RequestBody GenerationRequest request)
    {
        return toAjax(generationService.generate(request));
    }

    @PostMapping("/article")
    public AjaxResult generateArticle(@RequestBody GenerationRequest request)
    {
        request.setContentType(ContentType.ARTICLE);
        return toAjax(generationService.generate(request));
    }

    @PostMapping("/novel")
    public AjaxResult generateNovel(@RequestBody GenerationRequest request)
    {
        request.setContentType(ContentType.NOVEL);
        return toAjax(generationService.generate(request));
    }

    @PostMapping("/ad-copy")
    public AjaxResult generateAdCopy(@RequestBody GenerationRequest request)
    {
        request.setContentType(ContentType.AD_COPY);
        return toAjax(generationService.generate(request));
    }

    @PostMapping("/custom")
    public AjaxResult generateCustom(@RequestBody GenerationRequest request)
    {
        request.setContentType(ContentType.CUSTOM);
        return toAjax(generationService.generate(request));
    }

    @GetMapping(value = "/stream", produces = "text/event-stream")
    public SseEmitter generateContentStream(@RequestParam String contentType,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String apiKey)
    {
        SseEmitter emitter = new SseEmitter(0L);
        GenerationRequest request = GenerationRequest.builder()
                .contentType(ContentType.fromCode(contentType))
                .title(title)
                .description(description)
                .apiKey(apiKey)
                .stream(true)
                .build();
        generationService.generateStream(request, new ContentGenerationService.StreamHandler()
        {
            @Override
            public void onToken(String token)
            {
                send(emitter, "token", token);
            }

            @Override
            public void onComplete(String content)
            {
                send(emitter, "complete", content);
                emitter.complete();
            }

            @Override
            public void onError(String error)
            {
                send(emitter, "error", error);
                emitter.completeWithError(new RuntimeException(error));
            }
        });
        return emitter;
    }

    private AjaxResult toAjax(GenerationResponse response)
    {
        if (response.isCompleted())
        {
            return AjaxResult.success(response.getContent(), response);
        }
        return AjaxResult.error(StringUtils.defaultIfBlank(response.getErrorMessage(), "AI generation failed"));
    }

    private void send(SseEmitter emitter, String name, String data)
    {
        try
        {
            emitter.send(data, SseEmitter.event().name(name));
        }
        catch (IOException e)
        {
            emitter.completeWithError(e);
        }
    }
}
