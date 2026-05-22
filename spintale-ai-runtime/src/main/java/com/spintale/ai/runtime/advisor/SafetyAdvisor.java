package com.spintale.ai.runtime.advisor;

import java.util.List;
import java.util.Locale;

/**
 * Basic request safety advisor.
 */
public class SafetyAdvisor implements Advisor {

    private boolean enabled = true;
    private SafetyLevel safetyLevel = SafetyLevel.MODERATE;

    private static final List<String> STRICT_BLOCKLIST = List.of(
            "ignore previous instructions",
            "system prompt",
            "jailbreak");

    @Override
    public String getName() {
        return "SafetyAdvisor";
    }

    @Override
    public int getOrder() {
        return AdvisorOrder.SAFETY;
    }

    @Override
    public AdvisorRequest adviseRequest(AdvisorRequest request, AdvisorContext context) {
        if (!enabled || request == null || request.getUserMessage() == null) {
            context.put(AdvisorContext.SAFETY_CHECK_PASSED, true);
            return request;
        }

        String message = request.getUserMessage().toLowerCase(Locale.ROOT);
        boolean blocked = SafetyLevel.STRICT.equals(safetyLevel)
                && STRICT_BLOCKLIST.stream().anyMatch(message::contains);
        context.put(AdvisorContext.SAFETY_CHECK_PASSED, !blocked);
        if (blocked) {
            throw new IllegalArgumentException("Request blocked by safety policy");
        }
        return request;
    }

    public SafetyAdvisor setSafetyLevel(SafetyLevel safetyLevel) {
        this.safetyLevel = safetyLevel == null ? SafetyLevel.MODERATE : safetyLevel;
        return this;
    }

    public SafetyAdvisor setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public enum SafetyLevel {
        OFF,
        LOW,
        MODERATE,
        STRICT
    }
}
