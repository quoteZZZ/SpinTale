package com.spintale.ai.core.util;

import com.alibaba.fastjson2.JSON;
import com.spintale.ai.core.exception.AiServiceException;

/**
 * JSON utility for AI module.
 */
public final class JsonUtils {

    private JsonUtils() {
        // Prevent instantiation
    }

    /**
     * Serialize object to JSON string.
     *
     * @param obj object to serialize
     * @return JSON string
     * @throws AiServiceException if serialization fails
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return "null";
        }
        try {
            return JSON.toJSONString(obj);
        } catch (Exception e) {
            throw new AiServiceException("JSON_SERIALIZE_ERROR", "Failed to serialize object", e);
        }
    }

    /**
     * Deserialize JSON string to object.
     *
     * @param json JSON string
     * @param clazz target class
     * @param <T> type parameter
     * @return deserialized object
     * @throws AiServiceException if deserialization fails
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return JSON.parseObject(json, clazz);
        } catch (Exception e) {
            throw new AiServiceException("JSON_DESERIALIZE_ERROR", 
                "Failed to deserialize JSON: " + json, e);
        }
    }

    /**
     * Check if string is valid JSON.
     *
     * @param json string to check
     * @return true if valid JSON
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.isEmpty()) {
            return false;
        }
        try {
            JSON.parse(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
