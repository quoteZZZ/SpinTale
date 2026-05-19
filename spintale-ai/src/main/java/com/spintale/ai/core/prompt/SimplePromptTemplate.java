package com.spintale.ai.core.prompt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.core.io.ClassPathResource;

public class SimplePromptTemplate implements PromptTemplate
{
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_.-]+)\\s*}}");

    @Override
    public String render(String template, Map<String, Object> variables)
    {
        if (template == null || variables == null)
        {
            return template;
        }
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find())
        {
            Object value = variables.get(matcher.group(1));
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(value == null ? "" : String.valueOf(value)));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    @Override
    public String loadTemplate(String resourcePath)
    {
        try
        {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Failed to load prompt template: " + resourcePath, e);
        }
    }

    @Override
    public String renderFile(String resourcePath, Map<String, Object> variables)
    {
        return render(loadTemplate(resourcePath), variables);
    }
}
