package com.spintale.ai.core.constant;

/**
 * AI module constants.
 */
public final class AiConstants {

    private AiConstants() {
        // Prevent instantiation
    }

    // Message roles
    public static final String ROLE_SYSTEM = "system";
    public static final String ROLE_USER = "user";
    public static final String ROLE_ASSISTANT = "assistant";
    public static final String ROLE_TOOL = "tool";

    // Finish reasons
    public static final String FINISH_STOP = "stop";
    public static final String FINISH_LENGTH = "length";
    public static final String FINISH_TOOL_CALLS = "tool_calls";
    public static final String FINISH_ERROR = "error";

    // Provider IDs
    public static final String PROVIDER_OPENAI = "openai";
    public static final String PROVIDER_OLLAMA = "ollama";
    public static final String PROVIDER_ANTHROPIC = "anthropic";
    public static final String PROVIDER_AZURE = "azure";

    // Default values
    public static final double DEFAULT_TEMPERATURE = 0.7;
    public static final int DEFAULT_MAX_TOKENS = 2048;
    public static final int DEFAULT_MAX_RETRIES = 3;

    // Error codes
    public static final String ERROR_PROVIDER_NOT_FOUND = "PROVIDER_NOT_FOUND";
    public static final String ERROR_MODEL_NOT_SUPPORTED = "MODEL_NOT_SUPPORTED";
    public static final String ERROR_RATE_LIMIT = "RATE_LIMIT_EXCEEDED";
    public static final String ERROR_TIMEOUT = "REQUEST_TIMEOUT";
    public static final String ERROR_INVALID_REQUEST = "INVALID_REQUEST";
}
