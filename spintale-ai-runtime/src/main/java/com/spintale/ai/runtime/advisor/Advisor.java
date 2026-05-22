package com.spintale.ai.runtime.advisor;

/**
 * Advisor interface for intercepting AI chat requests and responses.
 * Similar to Spring AI Advisors pattern.
 */
public interface Advisor {

    /**
     * Get the advisor name.
     *
     * @return advisor name
     */
    String getName();

    /**
     * Get the advisor order (lower = higher priority).
     *
     * @return order value
     */
    default int getOrder() {
        return 0;
    }

    /**
     * Advise the request before processing.
     *
     * @param request the advisor request
     * @param context the advisor context
     * @return modified request
     */
    default AdvisorRequest adviseRequest(AdvisorRequest request, AdvisorContext context) {
        return request;
    }

    /**
     * Advise the response after processing.
     *
     * @param response the advisor response
     * @param context the advisor context
     * @return modified response
     */
    default AdvisorResponse adviseResponse(AdvisorResponse response, AdvisorContext context) {
        return response;
    }
    
    default void onError(AdvisorRequest request, AdvisorContext context, Throwable error) {
    }
}
