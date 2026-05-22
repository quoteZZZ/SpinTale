package com.spintale.ai.runtime.policy;

import java.util.function.Supplier;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FallbackPolicy
{
    private String fallbackModel;
    private Supplier<String> fallbackContentSupplier;
    private String fallbackContent;
    private FallbackTrigger trigger;
    private boolean enabled;

    public enum FallbackTrigger
    {
        ON_ERROR,
        ON_TIMEOUT,
        ON_MODEL_UNAVAILABLE,
        ON_BUDGET_EXCEEDED,
        ALWAYS
    }

    public static FallbackPolicy withModel(String fallbackModel)
    {
        return FallbackPolicy.builder()
                .fallbackModel(fallbackModel)
                .trigger(FallbackTrigger.ON_ERROR)
                .enabled(true)
                .build();
    }

    public static FallbackPolicy withContent(String fallbackContent)
    {
        return FallbackPolicy.builder()
                .fallbackContent(fallbackContent)
                .trigger(FallbackTrigger.ON_ERROR)
                .enabled(true)
                .build();
    }

    public static FallbackPolicy withDynamicContent(Supplier<String> contentSupplier)
    {
        return FallbackPolicy.builder()
                .fallbackContentSupplier(contentSupplier)
                .trigger(FallbackTrigger.ON_ERROR)
                .enabled(true)
                .build();
    }

    public static FallbackPolicy onModelUnavailable(String fallbackModel)
    {
        return FallbackPolicy.builder()
                .fallbackModel(fallbackModel)
                .trigger(FallbackTrigger.ON_MODEL_UNAVAILABLE)
                .enabled(true)
                .build();
    }

    public boolean shouldTriggerFallback(FallbackTrigger eventTrigger)
    {
        if (!enabled)
        {
            return false;
        }
        return trigger == FallbackTrigger.ALWAYS || trigger == eventTrigger;
    }

    public String getFallbackContent()
    {
        if (fallbackContentSupplier != null)
        {
            return fallbackContentSupplier.get();
        }
        return fallbackContent;
    }
}
