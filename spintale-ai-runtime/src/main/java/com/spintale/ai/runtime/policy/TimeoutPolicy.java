package com.spintale.ai.runtime.policy;

import java.time.Duration;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TimeoutPolicy
{
    private Duration timeout;
    private TimeoutAction action;
    private String fallbackModel;
    private String fallbackMessage;

    public enum TimeoutAction
    {
        FAIL,
        RETURN_FALLBACK,
        SWITCH_MODEL
    }

    public static TimeoutPolicy ofSeconds(long seconds)
    {
        return TimeoutPolicy.builder()
                .timeout(Duration.ofSeconds(seconds))
                .action(TimeoutAction.FAIL)
                .build();
    }

    public static TimeoutPolicy ofMillis(long millis)
    {
        return TimeoutPolicy.builder()
                .timeout(Duration.ofMillis(millis))
                .action(TimeoutAction.FAIL)
                .build();
    }

    public static TimeoutPolicy withFallback(long seconds, String fallbackMessage)
    {
        return TimeoutPolicy.builder()
                .timeout(Duration.ofSeconds(seconds))
                .action(TimeoutAction.RETURN_FALLBACK)
                .fallbackMessage(fallbackMessage)
                .build();
    }

    public static TimeoutPolicy withModelSwitch(long seconds, String fallbackModel)
    {
        return TimeoutPolicy.builder()
                .timeout(Duration.ofSeconds(seconds))
                .action(TimeoutAction.SWITCH_MODEL)
                .fallbackModel(fallbackModel)
                .build();
    }

    public long getTimeoutMs()
    {
        return timeout != null ? timeout.toMillis() : 30000L;
    }
}
