package com.spintale.ai.generation.template;

import java.util.Map;

public interface ContentTemplate
{
    String getContentType();

    String buildPrompt(Map<String, Object> params);

    default String getSystemPrompt()
    {
        return "You are a professional content creation assistant.";
    }

    default String postProcess(String content, Map<String, Object> params)
    {
        return content;
    }
}
