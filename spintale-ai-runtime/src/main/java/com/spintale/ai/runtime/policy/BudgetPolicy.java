package com.spintale.ai.runtime.policy;

import java.time.Duration;
import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BudgetPolicy
{
    private Double maxCostPerRequest;
    private Double maxCostPerHour;
    private Double maxCostPerDay;
    private Long maxTokensPerRequest;
    private Long maxTokensPerHour;
    private Long maxTokensPerDay;
    private BudgetAction onExceeded;
    private String fallbackModel;

    public enum BudgetAction
    {
        FAIL,
        SWITCH_TO_CHEAPER_MODEL,
        RETURN_CACHED_IF_AVAILABLE
    }

    public static BudgetPolicy costLimitPerRequest(double maxCost)
    {
        return BudgetPolicy.builder()
                .maxCostPerRequest(maxCost)
                .onExceeded(BudgetAction.FAIL)
                .build();
    }

    public static BudgetPolicy tokenLimitPerRequest(long maxTokens)
    {
        return BudgetPolicy.builder()
                .maxTokensPerRequest(maxTokens)
                .onExceeded(BudgetAction.FAIL)
                .build();
    }

    public static BudgetPolicy hourlyLimit(double maxCostPerHour)
    {
        return BudgetPolicy.builder()
                .maxCostPerHour(maxCostPerHour)
                .onExceeded(BudgetAction.SWITCH_TO_CHEAPER_MODEL)
                .build();
    }

    public static BudgetPolicy dailyLimit(double maxCostPerDay)
    {
        return BudgetPolicy.builder()
                .maxCostPerDay(maxCostPerDay)
                .onExceeded(BudgetAction.SWITCH_TO_CHEAPER_MODEL)
                .build();
    }

    public static BudgetPolicy withFallback(double maxCost, String cheaperModel)
    {
        return BudgetPolicy.builder()
                .maxCostPerRequest(maxCost)
                .onExceeded(BudgetAction.SWITCH_TO_CHEAPER_MODEL)
                .fallbackModel(cheaperModel)
                .build();
    }

    public boolean isWithinBudget(Double cost, Long tokens)
    {
        if (maxCostPerRequest != null && cost != null && cost > maxCostPerRequest)
        {
            return false;
        }
        if (maxTokensPerRequest != null && tokens != null && tokens > maxTokensPerRequest)
        {
            return false;
        }
        return true;
    }

    public BudgetCheckResult checkRequestBudget(double estimatedCost, long estimatedTokens)
    {
        if (maxCostPerRequest != null && estimatedCost > maxCostPerRequest)
        {
            return new BudgetCheckResult(false, 
                    "Estimated cost " + estimatedCost + " exceeds limit " + maxCostPerRequest,
                    onExceeded, fallbackModel);
        }
        if (maxTokensPerRequest != null && estimatedTokens > maxTokensPerRequest)
        {
            return new BudgetCheckResult(false,
                    "Estimated tokens " + estimatedTokens + " exceeds limit " + maxTokensPerRequest,
                    onExceeded, fallbackModel);
        }
        return new BudgetCheckResult(true, null, null, null);
    }

    public record BudgetCheckResult(
            boolean withinBudget,
            String reason,
            BudgetAction action,
            String fallbackModel
    ) {}
}
