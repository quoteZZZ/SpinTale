package com.spintale.ai.memory;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对话消息
 */
@Data
@Builder
public class ConversationMessage {
    
    /**
     * 角色：system, user, assistant
     */
    private String role;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 时间戳
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * Token 数
     */
    private Integer tokenCount;
}
