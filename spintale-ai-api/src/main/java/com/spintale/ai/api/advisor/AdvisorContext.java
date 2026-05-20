package com.spintale.ai.api.advisor;

import java.util.HashMap;
import java.util.Map;

/**
 * Advisor context for passing data between advisors.
 */
public class AdvisorContext {

    public static final String ORIGINAL_QUERY = "originalQuery";
    public static final String CACHE_HIT = "cacheHit";
    public static final String CACHE_RESPONSE = "cacheResponse";
    public static final String HALLUCINATION_RESULT = "hallucinationResult";
    public static final String RETRIEVED_DOCUMENTS = "retrievedDocuments";
    public static final String SAFETY_CHECK_PASSED = "safetyCheckPassed";
    public static final String RETRIEVED_MEMORIES = "retrievedMemories";

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

    public Object get(String key) {
        return attributes.get(key);
    }

    public AdvisorContext setAttribute(String key, Object value) {
        attributes.put(key, value);
        return this;
    }

    public void put(String key, Object value) {
        attributes.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = attributes.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public static AdvisorContext create() {
        return new AdvisorContext();
    }
}
