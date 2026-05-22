package com.spintale.ai.runtime.policy;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ExecutionPolicy
{
    private TimeoutPolicy timeoutPolicy;
    private RetryPolicy retryPolicy;
    private FallbackPolicy fallbackPolicy;
    private BudgetPolicy budgetPolicy;
    private List<String> enabledFeatures;

    public static ExecutionPolicy create()
    {
        ExecutionPolicy policy = new ExecutionPolicy();
        policy.setEnabledFeatures(new ArrayList<>());
        return policy;
    }

    public ExecutionPolicy withTimeout(TimeoutPolicy timeout)
    {
        this.timeoutPolicy = timeout;
        return this;
    }

    public ExecutionPolicy withTimeout(long timeoutSeconds)
    {
        this.timeoutPolicy = TimeoutPolicy.ofSeconds(timeoutSeconds);
        return this;
    }

    public ExecutionPolicy withRetry(RetryPolicy retry)
    {
        this.retryPolicy = retry;
        return this;
    }

    public ExecutionPolicy withRetry(int maxRetries)
    {
        this.retryPolicy = RetryPolicy.exponentialBackoff(maxRetries);
        return this;
    }

    public ExecutionPolicy withFallback(FallbackPolicy fallback)
    {
        this.fallbackPolicy = fallback;
        return this;
    }

    public ExecutionPolicy withFallbackModel(String model)
    {
        this.fallbackPolicy = FallbackPolicy.withModel(model);
        return this;
    }

    public ExecutionPolicy withBudget(BudgetPolicy budget)
    {
        this.budgetPolicy = budget;
        return this;
    }

    public ExecutionPolicy withBudgetLimit(double maxCostPerRequest)
    {
        this.budgetPolicy = BudgetPolicy.costLimitPerRequest(maxCostPerRequest);
        return this;
    }

    public ExecutionPolicy enableFeature(String feature)
    {
        if (this.enabledFeatures == null)
        {
            this.enabledFeatures = new ArrayList<>();
        }
        this.enabledFeatures.add(feature);
        return this;
    }

    public boolean isFeatureEnabled(String feature)
    {
        return enabledFeatures != null && enabledFeatures.contains(feature);
    }

    public static ExecutionPolicy defaultPolicy()
    {
        return ExecutionPolicy.create()
                .withTimeout(30)
                .withRetry(3);
    }

    public static ExecutionPolicy strictPolicy()
    {
        return ExecutionPolicy.create()
                .withTimeout(10)
                .withRetry(1)
                .withBudgetLimit(0.1);
    }

    public static ExecutionPolicy lenientPolicy()
    {
        return ExecutionPolicy.create()
                .withTimeout(60)
                .withRetry(5);
    }
}
