package com.spintale.ai.provider.ollama;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OllamaEmbeddingResponse
{
    private String model;
    private float[] embedding;

    public int getDimension()
    {
        return embedding != null ? embedding.length : 0;
    }
}
