package com.spintale.ai.provider.ollama;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OllamaEmbeddingRequest
{
    private String model;
    private String prompt;

    public static OllamaEmbeddingRequest of(String model, String text)
    {
        return OllamaEmbeddingRequest.builder()
                .model(model)
                .prompt(text)
                .build();
    }
}
