package com.spintale.ai.web.controller;

import com.spintale.ai.core.ChatRequest;
import com.spintale.ai.core.ChatResponse;
import com.spintale.ai.core.EnhancedAiChatService;
import com.spintale.ai.core.TokenUsage;
import com.spintale.ai.memory.ConversationManager;
import com.spintale.ai.web.dto.ChatRequestDTO;
import com.spintale.ai.web.dto.ChatResponseDTO;
import com.spintale.ai.web.dto.ToolCallDTO;
import com.spintale.ai.web.dto.TokenUsageDTO;
import com.spintale.common.core.domain.AjaxResult;
import com.spintale.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * AI 聊天控制器
 * 提供统一的聊天接口，支持 ReAct 模式、工具调用、长期记忆等高级功能
 */
@RestController
@RequestMapping("/ai/chat")
public class AiChatController {

    private static final Logger log = LoggerFactory.getLogger(AiChatController.class);

    private final EnhancedAiChatService chatService;
    private final ConversationManager conversationManager;

    // 存储活跃的 SSE 连接
    private final Map<String, SseEmitter> activeEmitters = new ConcurrentHashMap<>();

    public AiChatController(EnhancedAiChatService chatService, ConversationManager conversationManager) {
        this.chatService = chatService;
        this.conversationManager = conversationManager;
    }

    /**
     * 发送消息并获取回复（非流式）
     */
    @PostMapping("/message")
    public AjaxResult sendMessage(@RequestBody ChatRequestDTO requestDTO) {
        try {
            // 生成会话 ID（如果未提供）
            String sessionId = StringUtils.isNotBlank(requestDTO.getSessionId())
                    ? requestDTO.getSessionId()
                    : UUID.randomUUID().toString();

            // 构建聊天请求
            ChatRequest request = ChatRequest.builder()
                    .sessionId(sessionId)
                    .message(requestDTO.getMessage())
                    .systemPrompt(requestDTO.getSystemPrompt())
                    .temperature(requestDTO.getTemperature())
                    .maxTokens(requestDTO.getMaxTokens())
                    .stream(false)
                    .extraParams(requestDTO.getExtraParams())
                    .build();

            // 执行聊天
            ChatResponse response = chatService.chat(request);

            // 转换为 DTO
            ChatResponseDTO responseDTO = convertToDTO(response);
            responseDTO.setSessionId(sessionId);

            return AjaxResult.success(responseDTO);
        } catch (Exception e) {
            log.error("Chat failed", e);
            return AjaxResult.error("聊天失败：" + e.getMessage());
        }
    }

    /**
     * 发送消息并获取流式回复
     */
    @GetMapping(value = "/stream", produces = "text/event-stream")
    public SseEmitter sendMessageStream(
            @RequestParam String message,
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) String systemPrompt,
            @RequestParam(required = false) Double temperature,
            @RequestParam(required = false) Integer maxTokens) {
        
        // 生成会话 ID（如果未提供）
        String finalSessionId = StringUtils.isNotBlank(sessionId)
                ? sessionId
                : UUID.randomUUID().toString();

        SseEmitter emitter = new SseEmitter(0L);
        activeEmitters.put(finalSessionId, emitter);

        // 构建聊天请求
        ChatRequest.Builder builder = ChatRequest.builder()
                .sessionId(finalSessionId)
                .message(message)
                .stream(true);

        if (StringUtils.isNotBlank(systemPrompt)) {
            builder.systemPrompt(systemPrompt);
        }
        if (temperature != null) {
            builder.temperature(temperature);
        }
        if (maxTokens != null) {
            builder.maxTokens(maxTokens);
        }

        ChatRequest request = builder.build();

        // 执行流式聊天
        chatService.chatStream(request, new com.spintale.ai.core.StreamHandler() {
            @Override
            public void onToken(String token) {
                send(emitter, "token", token);
            }

            @Override
            public void onComplete(String content) {
                send(emitter, "complete", content);
                emitter.complete();
                activeEmitters.remove(finalSessionId);
            }

            @Override
            public void onError(String error) {
                send(emitter, "error", error);
                emitter.completeWithError(new RuntimeException(error));
                activeEmitters.remove(finalSessionId);
            }
        });

        // 处理客户端断开连接
        emitter.onCompletion(() -> activeEmitters.remove(finalSessionId));
        emitter.onTimeout(() -> {
            log.warn("SSE timeout for session: {}", finalSessionId);
            activeEmitters.remove(finalSessionId);
        });
        emitter.onError(throwable -> {
            log.error("SSE error for session: {}", finalSessionId, throwable);
            activeEmitters.remove(finalSessionId);
        });

        return emitter;
    }

    /**
     * 获取会话历史
     */
    @GetMapping("/history/{sessionId}")
    public AjaxResult getConversationHistory(@PathVariable String sessionId) {
        try {
            var session = conversationManager.getSession(sessionId);
            if (session == null) {
                return AjaxResult.error("会话不存在");
            }

            var messages = session.getMessages().stream()
                    .map(msg -> Map.of(
                            "role", msg.getRole(),
                            "content", msg.getContent(),
                            "timestamp", msg.getTimestamp()
                    ))
                    .collect(Collectors.toList());

            return AjaxResult.success(Map.of(
                    "sessionId", sessionId,
                    "userId", session.getUserId(),
                    "createdAt", session.getCreatedAt(),
                    "lastActiveAt", session.getLastActiveAt(),
                    "messageCount", messages.size(),
                    "messages", messages
            ));
        } catch (Exception e) {
            log.error("Failed to get conversation history", e);
            return AjaxResult.error("获取会话历史失败：" + e.getMessage());
        }
    }

    /**
     * 清除会话历史
     */
    @DeleteMapping("/history/{sessionId}")
    public AjaxResult clearConversationHistory(@PathVariable String sessionId) {
        try {
            conversationManager.clearSession(sessionId);
            return AjaxResult.success("会话历史已清除");
        } catch (Exception e) {
            log.error("Failed to clear conversation history", e);
            return AjaxResult.error("清除会话历史失败：" + e.getMessage());
        }
    }

    /**
     * 列出所有活跃会话
     */
    @GetMapping("/sessions")
    public AjaxResult listActiveSessions() {
        try {
            var sessions = conversationManager.listSessions();
            var sessionList = sessions.stream()
                    .map(session -> Map.of(
                            "sessionId", session.getSessionId(),
                            "userId", session.getUserId(),
                            "messageCount", session.getMessages().size(),
                            "createdAt", session.getCreatedAt(),
                            "lastActiveAt", session.getLastActiveAt()
                    ))
                    .collect(Collectors.toList());

            return AjaxResult.success(sessionList);
        } catch (Exception e) {
            log.error("Failed to list sessions", e);
            return AjaxResult.error("列出会话失败：" + e.getMessage());
        }
    }

    /**
     * 获取可用工具列表
     */
    @GetMapping("/tools")
    public AjaxResult listAvailableTools() {
        try {
            // 这里可以从 chatService 或 McpServer 获取工具列表
            // 暂时返回空列表，实际实现需要注入 McpServer
            return AjaxResult.success(List.of());
        } catch (Exception e) {
            log.error("Failed to list tools", e);
            return AjaxResult.error("获取工具列表失败：" + e.getMessage());
        }
    }

    /**
     * 转换为 DTO
     */
    private ChatResponseDTO convertToDTO(ChatResponse response) {
        ChatResponseDTO dto = new ChatResponseDTO();
        dto.setContent(response.getContent());
        dto.setModel(response.getModel());
        dto.setFinished(response.getFinished());
        dto.setExtraData(response.getExtraData());

        if (response.getTokenUsage() != null) {
            TokenUsage usage = response.getTokenUsage();
            dto.setTokenUsage(new TokenUsageDTO(
                    usage.getInputTokens(),
                    usage.getOutputTokens(),
                    usage.getTotalTokens()
            ));
        }

        if (response.getToolCalls() != null && !response.getToolCalls().isEmpty()) {
            List<ToolCallDTO> toolCallDTOs = response.getToolCalls().stream()
                    .map(toolCall -> {
                        ToolCallDTO toolCallDTO = new ToolCallDTO();
                        toolCallDTO.setId(toolCall.getId());
                        toolCallDTO.setName(toolCall.getName());
                        toolCallDTO.setArguments(toolCall.getArguments());
                        // result 需要在执行后设置
                        return toolCallDTO;
                    })
                    .collect(Collectors.toList());
            dto.setToolCalls(toolCallDTOs);
        }

        return dto;
    }

    /**
     * 发送 SSE 消息
     */
    private void send(SseEmitter emitter, String name, String data) {
        try {
            emitter.send(data, SseEmitter.event().name(name));
        } catch (IOException e) {
            log.error("Failed to send SSE message", e);
            emitter.completeWithError(e);
        }
    }
}
