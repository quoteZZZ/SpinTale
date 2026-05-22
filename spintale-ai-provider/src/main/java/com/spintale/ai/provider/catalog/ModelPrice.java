package com.spintale.ai.provider.catalog;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ModelPrice
{
    private Double inputPricePer1k;
    private Double outputPricePer1k;
    private Double embeddingPricePer1k;
    private String currency;

    public double calculateCost(int inputTokens, int outputTokens)
    {
        double cost = 0.0;
        if (inputPricePer1k != null && inputTokens > 0)
        {
            cost += (inputTokens / 1000.0) * inputPricePer1k;
        }
        if (outputPricePer1k != null && outputTokens > 0)
        {
            cost += (outputTokens / 1000.0) * outputPricePer1k;
        }
        return cost;
    }

    public double calculateEmbeddingCost(int tokens)
    {
        if (embeddingPricePer1k == null || tokens <= 0)
        {
            return 0.0;
        }
        return (tokens / 1000.0) * embeddingPricePer1k;
    }

    public static ModelPrice of(double inputPer1k, double outputPer1k)
    {
        return ModelPrice.builder()
                .inputPricePer1k(inputPer1k)
                .outputPricePer1k(outputPer1k)
                .currency("USD")
                .build();
    }

    public static ModelPrice embedding(double per1k)
    {
        return ModelPrice.builder()
                .embeddingPricePer1k(per1k)
                .currency("USD")
                .build();
    }

    public static ModelPrice free()
    {
        return ModelPrice.builder()
                .inputPricePer1k(0.0)
                .outputPricePer1k(0.0)
                .embeddingPricePer1k(0.0)
                .currency("USD")
                .build();
    }
}
