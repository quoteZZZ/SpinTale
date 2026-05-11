package com.spintale.ai.web.controller;

import com.spintale.common.core.domain.AjaxResult;
import com.spintale.common.utils.StringUtils;
import com.spintale.ai.generation.model.ContentType;
import com.spintale.ai.generation.model.GenerationRequest;
import com.spintale.ai.generation.model.GenerationResponse;
import com.spintale.ai.generation.service.ContentGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Set;

/**
 * AI 内容生成控制器
 * 
 * 为前端提供内容生成接口
 */
@RestController
@RequestMapping("/ai/generate")
public class ContentGenerationController {

    private static final Logger log = LoggerFactory.getLogger(ContentGenerationController.class);

    @Autowired
    private ContentGenerationService generationService;

    /**
     * 获取支持的内容类型
     */
    @GetMapping("/types")
    public AjaxResult getSupportedContentTypes() {
        Set<String> types = generationService.getSupportedContentTypes();
        return AjaxResult.success(types);
    }

    /**
     * 生成内容（通用接口）
     */
    @PostMapping("/content")
    public AjaxResult generateContent(@RequestBody GenerationRequest request) {
        try {
            if (request.getContentType() == null) {
                return AjaxResult.error("请指定内容类型");
            }

            GenerationResponse response = generationService.generate(request);
            
            if (response.isCompleted()) {
                return AjaxResult.success(response.getContent(), response);
            } else {
                return AjaxResult.error(response.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("生成内容失败", e);
            return AjaxResult.error("生成失败：" + e.getMessage());
        }
    }

    /**
     * 生成文章
     */
    @PostMapping("/article")
    public AjaxResult generateArticle(
            @RequestParam String title,
            @RequestParam(required = false) String keywords,
            @RequestParam(required = false) String description,
            @RequestParam(required = false, defaultValue = "medium") String length,
            @RequestParam(required = false, defaultValue = "专业") String tone) {
        
        try {
            if (StringUtils.isEmpty(title)) {
                return AjaxResult.error("标题不能为空");
            }

            GenerationResponse response = generationService.generateArticle(title, keywords, description);
            
            if (response.isCompleted()) {
                return AjaxResult.success(response.getContent(), response);
            } else {
                return AjaxResult.error(response.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("生成文章失败", e);
            return AjaxResult.error("生成失败：" + e.getMessage());
        }
    }

    /**
     * 生成小说/故事
     */
    @PostMapping("/novel")
    public AjaxResult generateNovel(
            @RequestParam String title,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String characters,
            @RequestParam(required = false, defaultValue = "medium") String length) {
        
        try {
            if (StringUtils.isEmpty(title)) {
                return AjaxResult.error("标题不能为空");
            }

            GenerationResponse response = generationService.generateNovel(title, genre, description, characters);
            
            if (response.isCompleted()) {
                return AjaxResult.success(response.getContent(), response);
            } else {
                return AjaxResult.error(response.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("生成小说失败", e);
            return AjaxResult.error("生成失败：" + e.getMessage());
        }
    }

    /**
     * 生成广告词
     */
    @PostMapping("/ad-copy")
    public AjaxResult generateAdCopy(
            @RequestParam String productName,
            @RequestParam(required = false) String productFeatures,
            @RequestParam(required = false) String targetAudience,
            @RequestParam(required = false) String platform) {
        
        try {
            if (StringUtils.isEmpty(productName)) {
                return AjaxResult.error("产品名称不能为空");
            }

            GenerationRequest request = GenerationRequest.builder()
                    .contentType(ContentType.AD_COPY)
                    .title(productName)
                    .targetAudience(targetAudience)
                    .tone("活力")
                    .build();
            request.putExtraParam("productName", productName);
            request.putExtraParam("productFeatures", productFeatures);
            request.putExtraParam("platform", platform);

            GenerationResponse response = generationService.generate(request);
            
            if (response.isCompleted()) {
                return AjaxResult.success(response.getContent(), response);
            } else {
                return AjaxResult.error(response.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("生成广告词失败", e);
            return AjaxResult.error("生成失败：" + e.getMessage());
        }
    }

    /**
     * 生成营销文案
     */
    @PostMapping("/marketing")
    public AjaxResult generateMarketingCopy(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String targetAudience,
            @RequestParam(required = false) String tone) {
        
        try {
            GenerationRequest request = GenerationRequest.builder()
                    .contentType(ContentType.MARKETING_COPY)
                    .title(title)
                    .description(description)
                    .targetAudience(targetAudience)
                    .tone(tone)
                    .build();

            GenerationResponse response = generationService.generate(request);
            
            if (response.isCompleted()) {
                return AjaxResult.success(response.getContent(), response);
            } else {
                return AjaxResult.error(response.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("生成营销文案失败", e);
            return AjaxResult.error("生成失败：" + e.getMessage());
        }
    }

    /**
     * 流式生成内容（SSE）
     */
    @GetMapping(value = "/stream", produces = "text/event-stream")
    public SseEmitter generateContentStream(
            @RequestParam String contentType,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String keywords) {
        
        SseEmitter emitter = new SseEmitter(0L); // 永不超时
        
        try {
            ContentType type = ContentType.fromCode(contentType);
            
            GenerationRequest request = GenerationRequest.builder()
                    .contentType(type)
                    .title(title)
                    .description(description)
                    .keywords(keywords)
                    .stream(true)
                    .build();

            generationService.generateStream(request, new ContentGenerationService.StreamHandler() {
                @Override
                public void onToken(String token) {
                    try {
                        emitter.send(token, SseEmitter.event().name("token"));
                    } catch (IOException e) {
                        log.warn("发送 token 失败", e);
                        emitter.completeWithError(e);
                    }
                }

                @Override
                public void onComplete(String content) {
                    try {
                        emitter.send(content, SseEmitter.event().name("complete"));
                        emitter.complete();
                    } catch (IOException e) {
                        log.warn("发送完成消息失败", e);
                    }
                }

                @Override
                public void onError(String error) {
                    try {
                        emitter.send(error, SseEmitter.event().name("error"));
                        emitter.completeWithError(new RuntimeException(error));
                    } catch (IOException e) {
                        log.warn("发送错误消息失败", e);
                    }
                }
            });

        } catch (Exception e) {
            log.error("流式生成失败", e);
            try {
                emitter.send(e.getMessage(), SseEmitter.event().name("error"));
                emitter.completeWithError(e);
            } catch (IOException ex) {
                log.warn("发送错误失败", ex);
            }
        }

        return emitter;
    }

    /**
     * 自定义内容生成
     */
    @PostMapping("/custom")
    public AjaxResult generateCustom(
            @RequestParam String prompt,
            @RequestParam(required = false) String tone,
            @RequestParam(required = false) String length) {
        
        try {
            if (StringUtils.isEmpty(prompt)) {
                return AjaxResult.error("提示词不能为空");
            }

            GenerationRequest request = GenerationRequest.builder()
                    .contentType(ContentType.CUSTOM)
                    .description(prompt)
                    .tone(tone)
                    .length(length)
                    .build();

            GenerationResponse response = generationService.generate(request);
            
            if (response.isCompleted()) {
                return AjaxResult.success(response.getContent(), response);
            } else {
                return AjaxResult.error(response.getErrorMessage());
            }
        } catch (Exception e) {
            log.error("自定义生成失败", e);
            return AjaxResult.error("生成失败：" + e.getMessage());
        }
    }
}
