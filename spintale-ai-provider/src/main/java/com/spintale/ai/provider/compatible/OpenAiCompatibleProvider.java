package com.spintale.ai.provider.compatible;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OpenAiCompatibleProvider
{
    private String providerId;
    private String providerName;
    private String baseUrl;
    private String apiKey;
    private List<String> supportedModels;
    private boolean requiresApiKey;

    public boolean supportsModel(String modelName)
    {
        if (supportedModels == null || supportedModels.isEmpty())
        {
            return true;
        }
        return supportedModels.stream()
                .anyMatch(m -> m.equalsIgnoreCase(modelName) || 
                        modelName.startsWith(m.split(":")[0]));
    }

    public static OpenAiCompatibleProvider of(String providerId, String name, String baseUrl)
    {
        return OpenAiCompatibleProvider.builder()
                .providerId(providerId)
                .providerName(name)
                .baseUrl(baseUrl)
                .requiresApiKey(false)
                .build();
    }

    public static OpenAiCompatibleProvider of(String providerId, String name, 
            String baseUrl, String apiKey)
    {
        return OpenAiCompatibleProvider.builder()
                .providerId(providerId)
                .providerName(name)
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .requiresApiKey(true)
                .build();
    }

    public static OpenAiCompatibleProvider vllm(String baseUrl)
    {
        return of("vllm", "vLLM", baseUrl);
    }

    public static OpenAiCompatibleProvider lmStudio(String baseUrl)
    {
        return of("lm-studio", "LM Studio", baseUrl);
    }

    public static OpenAiCompatibleProvider ollamaOpenai(String baseUrl)
    {
        return of("ollama-openai", "Ollama (OpenAI Compatible)", baseUrl);
    }
}
