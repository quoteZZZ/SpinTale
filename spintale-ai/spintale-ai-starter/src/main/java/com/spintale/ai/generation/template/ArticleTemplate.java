package com.spintale.ai.generation.template;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ArticleTemplate implements ContentTemplate
{
    @Override
    public String getContentType()
    {
        return "article";
    }

    @Override
    public String getSystemPrompt()
    {
        return "You are a professional article writer. Create structured, concrete, readable articles.";
    }

    @Override
    public String buildPrompt(Map<String, Object> params)
    {
        return "Write an article.\n"
                + "Title: " + value(params, "title") + "\n"
                + "Keywords: " + value(params, "keywords") + "\n"
                + "Audience: " + value(params, "targetAudience") + "\n"
                + "Tone: " + value(params, "tone") + "\n"
                + "Length: " + value(params, "length") + "\n"
                + "Requirements:\n" + value(params, "description");
    }

    private String value(Map<String, Object> params, String key)
    {
        Object value = params.get(key);
        return value == null ? "" : String.valueOf(value);
    }
}
