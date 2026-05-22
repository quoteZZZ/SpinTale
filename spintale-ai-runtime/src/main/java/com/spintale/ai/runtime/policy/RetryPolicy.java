package com.spintale.ai.runtime.policy;

import java.time.Duration;
import java.util.function.Predicate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RetryPolicy
{
    private int maxRetries;
    private Duration initialDelay;
    private Duration maxDelay;
    private double multiplier;
    private Predicate<Exception> retryOn;
    private RetryStrategy strategy;

    public enum RetryStrategy
    {
        FIXED_DELAY,
        EXPONENTIAL_BACKOFF,
        LINEAR_BACKOFF
    }

    public static RetryPolicy fixedDelay(int maxRetries, Duration delay)
    {
        return RetryPolicy.builder()
                .maxRetries(maxRetries)
                .initialDelay(delay)
                .strategy(RetryStrategy.FIXED_DELAY)
                .build();
    }

    public static RetryPolicy exponentialBackoff(int maxRetries)
    {
        return RetryPolicy.builder()
                .maxRetries(maxRetries)
                .initialDelay(Duration.ofMillis(100))
                .maxDelay(Duration.ofSeconds(10))
                .multiplier(2.0)
                .strategy(RetryStrategy.EXPONENTIAL_BACKOFF)
                .build();
    }

    public static RetryPolicy exponentialBackoff(int maxRetries, 
            Duration initialDelay, Duration maxDelay)
    {
        return RetryPolicy.builder()
                .maxRetries(maxRetries)
                .initialDelay(initialDelay)
                .maxDelay(maxDelay)
                .multiplier(2.0)
                .strategy(RetryStrategy.EXPONENTIAL_BACKOFF)
                .build();
    }

    public static RetryPolicy linearBackoff(int maxRetries, 
            Duration initialDelay, Duration maxDelay)
    {
        return RetryPolicy.builder()
                .maxRetries(maxRetries)
                .initialDelay(initialDelay)
                .maxDelay(maxDelay)
                .strategy(RetryStrategy.LINEAR_BACKOFF)
                .build();
    }

    public RetryPolicy retryOn(Predicate<Exception> condition)
    {
        this.retryOn = condition;
        return this;
    }

    public RetryPolicy retryOnExceptions(Class<? extends Exception>... exceptionTypes)
    {
        this.retryOn = e -> {
            for (Class<? extends Exception> type : exceptionTypes)
            {
                if (type.isInstance(e))
                {
                    return true;
                }
            }
            return false;
        };
        return this;
    }

    public boolean shouldRetry(Exception exception, int currentRetryCount)
    {
        if (currentRetryCount >= maxRetries)
        {
            return false;
        }
        if (retryOn == null)
        {
            return true;
        }
        return retryOn.test(exception);
    }

    public Duration getDelayForRetry(int retryCount)
    {
        if (retryCount <= 0)
        {
            return Duration.ZERO;
        }

        long initialMs = initialDelay != null ? initialDelay.toMillis() : 100;
        long maxMs = maxDelay != null ? maxDelay.toMillis() : 10000;

        return switch (strategy)
        {
            case FIXED_DELAY -> Duration.ofMillis(Math.min(initialMs, maxMs));
            case EXPONENTIAL_BACKOFF ->
            {
                double mult = multiplier != null ? multiplier : 2.0;
                long delay = (long) (initialMs * Math.pow(mult, retryCount - 1));
                yield Duration.ofMillis(Math.min(delay, maxMs));
            }
            case LINEAR_BACKOFF ->
            {
                long delay = initialMs * retryCount;
                yield Duration.ofMillis(Math.min(delay, maxMs));
            }
        };
    }
}
