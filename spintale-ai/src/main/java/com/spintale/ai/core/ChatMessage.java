package com.spintale.ai.core;

import lombok.Builder;
import lombok.Data;

/**
 * 聊天消息
 */
@Data
@Builder
public class ChatMessage {
    
    /**
     * 角色：system, user, assistant, tool
     */
    private String role;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 工具调用 ID (当 role 为 tool 时)
     */
    private String toolCallId;
    
    /**
     * 工具名称 (当 role 为 tool 时)
     */
    private String toolName;
}
