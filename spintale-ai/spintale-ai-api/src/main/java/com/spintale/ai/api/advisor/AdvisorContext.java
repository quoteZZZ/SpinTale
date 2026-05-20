package com.spintale.ai.api.advisor;

import java.util.HashMap;
import java.util.Map;

/**
 * Advisor context for passing data between advisors.
 */
public class AdvisorContext {

    private final Map<String, Object> attributes = new HashMap<>();
    private String providerId;
    private String model;
    private long startTime;

    public AdvisorContext() {
        this.startTime = System.currentTimeMillis();
    }

    public String getProviderId() {
        return providerId;
    }

    public AdvisorContext providerId(String providerId) {
        this.providerId = providerId;
        return this;
    }

    public String getModel() {
        return model;
    }

    public AdvisorContext model(String model) {
        this.model = model;
        return this;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public AdvisorContext setAttribute(String key, Object value) {
        attributes.put(key, value);
        return this;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public static AdvisorContext create() {
        return new AdvisorContext();
    }
}
