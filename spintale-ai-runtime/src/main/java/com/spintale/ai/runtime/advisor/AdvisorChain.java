package com.spintale.ai.runtime.advisor;

import com.spintale.ai.core.model.ChatRequest;
import com.spintale.ai.core.model.ChatResponse;
import com.spintale.ai.core.service.AiChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

@Component
public class AdvisorChain {
    
    private static final Logger log = LoggerFactory.getLogger(AdvisorChain.class);
    
    private final List<Advisor> advisors;
    
    public AdvisorChain(List<Advisor> advisors) {
        this.advisors = new ArrayList<>(advisors == null ? List.of() : advisors);
        this.advisors.sort(Comparator.comparingInt(Advisor::getOrder));
        log.info("AdvisorChain initialized with {} advisors: {}", 
                this.advisors.size(), 
                this.advisors.stream().map(Advisor::getName).toList());
    }
    
    public ChatResponse execute(ChatRequest request, AiChatService chatService) {
        return execute(request, chatService::chat);
    }
    
    public ChatResponse execute(ChatRequest request, Function<ChatRequest, ChatResponse> executor) {
        AdvisorContext context = new AdvisorContext();
        context.put(AdvisorContext.ORIGINAL_QUERY, request.getMessage());
        
        AdvisorRequest advisorRequest = toAdvisorRequest(request);
        
        for (Advisor advisor : advisors) {
            try {
                advisorRequest = advisor.adviseRequest(advisorRequest, context);
                
                if (Boolean.TRUE.equals(context.get(AdvisorContext.CACHE_HIT, Boolean.class))) {
                    Object cachedResponse = context.get(AdvisorContext.CACHE_RESPONSE);
                    if (cachedResponse != null) {
                        log.debug("AdvisorChain short-circuited by {} (cache hit)", advisor.getName());
                        return createCacheResponse(cachedResponse);
                    }
                }
            } catch (Exception ex) {
                log.warn("Advisor [{}] failed in request phase: {}", advisor.getName(), ex.getMessage());
            }
        }
        
        ChatRequest processedRequest = toChatRequest(request, advisorRequest);
        
        ChatResponse response;
        try {
            response = executor.apply(processedRequest);
        } catch (Exception ex) {
            log.error("Chat execution failed", ex);
            for (int i = advisors.size() - 1; i >= 0; i--) {
                try {
                    advisors.get(i).onError(advisorRequest, context, ex);
                } catch (Exception handlerEx) {
                    log.warn("Error handler [{}] failed: {}", advisors.get(i).getName(), handlerEx.getMessage());
                }
            }
            throw ex;
        }
        
        AdvisorResponse advisorResponse = toAdvisorResponse(response);
        
        for (int i = advisors.size() - 1; i >= 0; i--) {
            try {
                advisorResponse = advisors.get(i).adviseResponse(advisorResponse, context);
            } catch (Exception ex) {
                log.warn("Advisor [{}] failed in response phase: {}", advisors.get(i).getName(), ex.getMessage());
            }
        }
        
        return toChatResponse(response, advisorResponse);
    }
    
    private AdvisorRequest toAdvisorRequest(ChatRequest request) {
        AdvisorRequest advisorRequest = AdvisorRequest.from(
                request.getMessage(),
                request.getSystemPrompt(),
                request.getHistory(),
                request.getSessionId(),
                request.getUserId(),
                request.getTemperature(),
                request.getMaxTokens(),
                request.isStreaming()
        );
        if (request.getExtraParams() != null) {
            advisorRequest.setParams(new java.util.HashMap<>(request.getExtraParams()));
        }
        return advisorRequest;
    }
    
    private ChatRequest toChatRequest(ChatRequest original, AdvisorRequest advisorRequest) {
        return ChatRequest.builder()
                .message(advisorRequest.getUserMessage())
                .systemPrompt(advisorRequest.getSystemPrompt())
                .messages(original.getMessages())
                .history(advisorRequest.getHistory())
                .sessionId(advisorRequest.getSessionId())
                .userId(advisorRequest.getUserId())
                .temperature(advisorRequest.getTemperature())
                .maxTokens(advisorRequest.getMaxTokens())
                .streaming(Boolean.TRUE.equals(advisorRequest.getStream()))
                .extraParams(advisorRequest.getParams())
                .build();
    }
    
    private AdvisorResponse toAdvisorResponse(ChatResponse response) {
        AdvisorResponse advisorResponse = new AdvisorResponse();
        if (response == null) {
            return advisorResponse;
        }
        advisorResponse.setContent(response.getContent());
        advisorResponse.setModel(response.getModel());
        advisorResponse.setSessionId(response.getSessionId());
        advisorResponse.setTokenUsage(response.getTokenUsage());
        advisorResponse.setFinished(Boolean.TRUE.equals(response.getFinished()));
        if (response.getExtraData() != null) {
            advisorResponse.setMetadata(new java.util.HashMap<>(response.getExtraData()));
        }
        return advisorResponse;
    }
    
    private ChatResponse toChatResponse(ChatResponse original, AdvisorResponse advisorResponse) {
        ChatResponse response = original == null ? new ChatResponse() : original;
        response.setContent(advisorResponse.getContent());
        response.setModel(advisorResponse.getModel());
        response.setSessionId(advisorResponse.getSessionId());
        response.setTokenUsage(advisorResponse.getTokenUsage());
        response.setFinished(advisorResponse.isFinished());
        response.setExtraData(advisorResponse.getMetadata());
        return response;
    }
    
    private ChatResponse createCacheResponse(Object cachedContent) {
        ChatResponse response = new ChatResponse();
        response.setContent(String.valueOf(cachedContent));
        response.setFinished(true);
        response.setComplete(true);
        return response;
    }
    
    public List<Advisor> getAdvisors() {
        return new ArrayList<>(advisors);
    }
    
    public AdvisorChain addAdvisor(Advisor advisor) {
        List<Advisor> newAdvisors = new ArrayList<>(this.advisors);
        newAdvisors.add(advisor);
        newAdvisors.sort(Comparator.comparingInt(Advisor::getOrder));
        return new AdvisorChain(newAdvisors);
    }
}
