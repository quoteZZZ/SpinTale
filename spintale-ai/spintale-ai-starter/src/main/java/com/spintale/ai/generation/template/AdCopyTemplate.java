package com.spintale.ai.generation.template;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class AdCopyTemplate implements ContentTemplate
{
    @Override
    public String getContentType()
    {
        return "ad_copy";
    }

    @Override
    public String getSystemPrompt()
    {
        return "You are a conversion-focused copywriter. Write concise and persuasive ad copy.";
    }

    @Override
    public String buildPrompt(Map<String, Object> params)
    {
        return "Write ad copy.\n"
                + "Product: " + value(params, "productName") + "\n"
                + "Features: " + value(params, "productFeatures") + "\n"
                + "Audience: " + value(params, "targetAudience") + "\n"
                + "Platform: " + value(params, "platform") + "\n"
                + "Requirements:\n" + value(params, "description");
    }

    private String value(Map<String, Object> params, String key)
    {
        Object value = params.get(key);
        return value == null ? "" : String.valueOf(value);
    }
}
