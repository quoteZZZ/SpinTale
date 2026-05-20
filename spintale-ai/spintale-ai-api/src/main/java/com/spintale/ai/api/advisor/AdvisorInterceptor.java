package com.spintale.ai.api.advisor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.spintale.ai.core.model.ChatRequest;
import com.spintale.ai.core.model.ChatResponse;
import com.spintale.ai.core.pipeline.AiChatInterceptor;
import com.spintale.ai.core.pipeline.PipelineAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bridges the existing Advisor model into the new provider-agnostic chat pipeline.
 */
public class AdvisorInterceptor implements AiChatInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AdvisorInterceptor.class);

    private final List<Advisor> advisors;

    public AdvisorInterceptor(List<Advisor> advisors) {
        this.advisors = new ArrayList<>(advisors == null ? List.of() : advisors);
        this.advisors.sort(Comparator.comparingInt(Advisor::getOrder));
    }

    @Override
    public int getOrder() {
        return 100;
    }

    @Override
    public ChatRequest beforeChat(ChatRequest request) {
        AdvisorContext context = new AdvisorContext();
        context.put(AdvisorContext.ORIGINAL_QUERY, request.getMessage());

        AdvisorRequest advisorRequest = toAdvisorRequest(request);
        for (Advisor advisor : advisors) {
            try {
                advisorRequest = advisor.adviseRequest(advisorRequest, context);
                if (Boolean.TRUE.equals(context.get(AdvisorContext.CACHE_HIT, Boolean.class))) {
                    Object cachedResponse = context.get(AdvisorContext.CACHE_RESPONSE);
                    if (cachedResponse != null) {
                        advisorRequest.setParam(PipelineAttributes.SHORT_CIRCUIT_RESPONSE, cachedResponse);
                    }
                    break;
                }
            } catch (Exception ex) {
                log.warn("Advisor [{}] skipped during request phase: {}", advisor.getName(), ex.getMessage(), ex);
            }
        }
        advisorRequest.setParam(PipelineAttributes.ADVISOR_CONTEXT, context);
        return toChatRequest(request, advisorRequest);
    }

    @Override
    public ChatResponse afterChat(ChatRequest request, ChatResponse response) {
        AdvisorContext context = getAdvisorContext(request);
        if (context == null) {
            return response;
        }

        AdvisorResponse advisorResponse = toAdvisorResponse(response);
        for (int i = advisors.size() - 1; i >= 0; i--) {
            try {
                advisorResponse = advisors.get(i).adviseResponse(advisorResponse, context);
            } catch (Exception ex) {
                log.warn("Advisor [{}] skipped during response phase: {}", advisors.get(i).getName(), ex.getMessage(), ex);
            }
        }
        return toChatResponse(response, advisorResponse);
    }

    @Override
    public void onError(ChatRequest request, Throwable error) {
        // Context is stored on the request attributes, so there is no thread-local state to clear.
    }

    private AdvisorRequest toAdvisorRequest(ChatRequest request) {
        String userId = request.getUserId();
        if ((userId == null || userId.isBlank()) && request.getExtraParams() != null) {
            Object extraUserId = request.getExtraParams().get("userId");
            userId = extraUserId == null ? null : String.valueOf(extraUserId);
        }
        AdvisorRequest advisorRequest = AdvisorRequest.from(
                request.getMessage(),
                request.getSystemPrompt(),
                request.getHistory(),
                request.getSessionId(),
                userId,
                request.getTemperature(),
                request.getMaxTokens(),
                request.getStream()
        );
        if (request.getExtraParams() != null) {
            advisorRequest.setParams(new HashMap<>(request.getExtraParams()));
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
                .stream(advisorRequest.getStream())
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
            advisorResponse.setMetadata(new HashMap<>(response.getExtraData()));
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

    private AdvisorContext getAdvisorContext(ChatRequest request) {
        if (request == null || request.getExtraParams() == null) {
            return null;
        }
        Object context = request.getExtraParams().get(PipelineAttributes.ADVISOR_CONTEXT);
        return context instanceof AdvisorContext advisorContext ? advisorContext : null;
    }
}
