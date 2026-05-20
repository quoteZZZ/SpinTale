package com.spintale.ai.api.advisor;

/**
 * Applies confidence metadata produced by hallucination checks.
 */
public class HallucinationAdvisor implements Advisor {

    private boolean enabled = true;
    private double hallucinationThreshold = 0.5;
    private HallucinationAction action = HallucinationAction.WARN;

    @Override
    public String getName() {
        return "HallucinationAdvisor";
    }

    @Override
    public int getOrder() {
        return AdvisorOrder.HALLUCINATION;
    }

    @Override
    public AdvisorResponse adviseResponse(AdvisorResponse response, AdvisorContext context) {
        if (!enabled || response == null) {
            return response;
        }

        Double confidence = response.getConfidenceScore();
        if (confidence == null) {
            confidence = 1.0;
            response.setConfidenceScore(confidence);
        }

        boolean hallucinationRisk = confidence < hallucinationThreshold;
        response.setMetadata("hallucination_risk", hallucinationRisk);
        if (hallucinationRisk && HallucinationAction.BLOCK.equals(action)) {
            response.setContent("The response was blocked because confidence was below policy threshold.");
            response.setFinished(true);
        }
        return response;
    }

    public HallucinationAdvisor setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public HallucinationAdvisor setHallucinationThreshold(double threshold) {
        this.hallucinationThreshold = Math.max(0.0, Math.min(1.0, threshold));
        return this;
    }

    public HallucinationAdvisor setAction(HallucinationAction action) {
        this.action = action == null ? HallucinationAction.WARN : action;
        return this;
    }

    public enum HallucinationAction {
        WARN,
        BLOCK
    }
}
