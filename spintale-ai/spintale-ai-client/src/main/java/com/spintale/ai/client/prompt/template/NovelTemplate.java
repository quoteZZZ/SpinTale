package com.spintale.ai.generation.template;

import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class NovelTemplate implements ContentTemplate
{
    @Override
    public String getContentType()
    {
        return "novel";
    }

    @Override
    public String getSystemPrompt()
    {
        return "You are a fiction writer. Build vivid scenes, coherent characters, conflict, and narrative momentum.";
    }

    @Override
    public String buildPrompt(Map<String, Object> params)
    {
        return "Write a novel or story draft.\n"
                + "Title: " + value(params, "title") + "\n"
                + "Genre: " + value(params, "genre") + "\n"
                + "Characters: " + value(params, "characters") + "\n"
                + "Tone: " + value(params, "tone") + "\n"
                + "Length: " + value(params, "length") + "\n"
                + "Story brief:\n" + value(params, "description");
    }

    private String value(Map<String, Object> params, String key)
    {
        Object value = params.get(key);
        return value == null ? "" : String.valueOf(value);
    }
}
