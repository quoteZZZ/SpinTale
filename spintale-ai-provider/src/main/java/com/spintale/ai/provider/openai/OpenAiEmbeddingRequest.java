package com.spintale.ai.provider.openai;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OpenAiEmbeddingRequest
{
    private String model;
    private List<String> input;
    private String encodingFormat;

    public static OpenAiEmbeddingRequest of(String model, String text)
    {
        return OpenAiEmbeddingRequest.builder()
                .model(model)
                .input(List.of(text))
                .build();
    }

    public static OpenAiEmbeddingRequest of(String model, List<String> texts)
    {
        return OpenAiEmbeddingRequest.builder()
                .model(model)
                .input(texts)
                .build();
    }
}
