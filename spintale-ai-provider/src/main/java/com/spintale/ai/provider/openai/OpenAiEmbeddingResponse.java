package com.spintale.ai.provider.openai;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OpenAiEmbeddingResponse
{
    private String object;
    private List<Embedding> data;
    private String model;
    private Usage usage;

    @Data
    @Builder
    public static class Embedding
    {
        private String object;
        private Integer index;
        private float[] embedding;
    }

    @Data
    @Builder
    public static class Usage
    {
        private Integer promptTokens;
        private Integer totalTokens;
    }

    public float[] getFirstEmbedding()
    {
        if (data == null || data.isEmpty()) return null;
        return data.get(0).getEmbedding();
    }

    public List<float[]> getAllEmbeddings()
    {
        if (data == null) return List.of();
        return data.stream().map(Embedding::getEmbedding).toList();
    }

    public int getDimension()
    {
        float[] emb = getFirstEmbedding();
        return emb != null ? emb.length : 0;
    }
}
