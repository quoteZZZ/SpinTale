package com.spintale.ai.observability.hallucination;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * Hallucination detection result wrapper for Temporal workflow.
 * 
 * This class provides a serializable result container that can be
 * passed between Temporal workflow and activities.
 * 
 * @author SpinTale AI Team
 */
public class HallucinationReport {
    
    private boolean isHallucination;
    private double confidenceScore;
    private List<String> flags;
    private List<String> factInconsistencies;
    private List<String> contradictions;
    private List<String> unverifiedClaims;
    private String summary;
    
    public HallucinationReport() {
        this.flags = new ArrayList<>();
        this.factInconsistencies = new ArrayList<>();
        this.contradictions = new ArrayList<>();
        this.unverifiedClaims = new ArrayList<>();
        this.confidenceScore = 1.0;
        this.isHallucination = false;
    }
    
    /**
     * Create from internal HallucinationResult.
     */
    public static HallucinationReport from(HallucinationDetector.HallucinationResult internalResult) {
        HallucinationReport result = new HallucinationReport();
        
        if (internalResult != null) {
            result.setIsHallucination(internalResult.getIsHallucination());
            result.setConfidenceScore(internalResult.getOverallConfidence());
            
            if (internalResult.getFlags() != null) {
                result.setFlags(new ArrayList<>(internalResult.getFlags()));
            }
            
            if (internalResult.getFactInconsistencies() != null) {
                result.setFactInconsistencies(internalResult.getFactInconsistencies());
            }
            
            if (internalResult.getContradictions() != null) {
                result.setContradictions(internalResult.getContradictions());
            }
            
            if (internalResult.getUnverifiedClaims() != null) {
                result.setUnverifiedClaims(internalResult.getUnverifiedClaims());
            }
            
            // Generate summary
            result.setSummary(generateSummary(result));
        }
        
        return result;
    }
    
    /**
     * Check if response is safe to use.
     */
    public boolean isSafe() {
        return !isHallucination && confidenceScore >= 0.7;
    }
    
    /**
     * Get risk level (LOW, MEDIUM, HIGH).
     */
    public String getRiskLevel() {
        if (confidenceScore >= 0.8) {
            return "LOW";
        } else if (confidenceScore >= 0.5) {
            return "MEDIUM";
        } else {
            return "HIGH";
        }
    }
    
    /**
     * Get actionable recommendations.
     */
    public List<String> getRecommendations() {
        List<String> recommendations = new ArrayList<>();
        
        if (isHallucination) {
            recommendations.add("Response contains potential hallucinations. Consider regenerating with stricter constraints.");
        }
        
        if (confidenceScore < 0.5) {
            recommendations.add("Low confidence score. Verify facts before presenting to user.");
        }
        
        if (!factInconsistencies.isEmpty()) {
            recommendations.add("Fact inconsistencies detected: " + String.join(", ", factInconsistencies.subList(0, Math.min(3, factInconsistencies.size()))));
        }
        
        if (!contradictions.isEmpty()) {
            recommendations.add("Logical contradictions found. Review response coherence.");
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("Response appears reliable. Safe to present to user.");
        }
        
        return recommendations;
    }
    
    // Getters and Setters
    
    public boolean getIsHallucination() {
        return isHallucination;
    }
    
    public void setIsHallucination(boolean isHallucination) {
        this.isHallucination = isHallucination;
    }
    
    public double getConfidenceScore() {
        return confidenceScore;
    }
    
    public void setConfidenceScore(double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
    
    public List<String> getFlags() {
        return flags;
    }
    
    public void setFlags(List<String> flags) {
        this.flags = flags;
    }
    
    public List<String> getFactInconsistencies() {
        return factInconsistencies;
    }
    
    public void setFactInconsistencies(List<String> factInconsistencies) {
        this.factInconsistencies = factInconsistencies;
    }
    
    public List<String> getContradictions() {
        return contradictions;
    }
    
    public void setContradictions(List<String> contradictions) {
        this.contradictions = contradictions;
    }
    
    public List<String> getUnverifiedClaims() {
        return unverifiedClaims;
    }
    
    public void setUnverifiedClaims(List<String> unverifiedClaims) {
        this.unverifiedClaims = unverifiedClaims;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    @Override
    public String toString() {
        return "HallucinationReport{" +
            "isHallucination=" + isHallucination +
            ", confidenceScore=" + confidenceScore +
            ", riskLevel='" + getRiskLevel() + '\'' +
            ", flags=" + flags.size() +
            ", inconsistencies=" + factInconsistencies.size() +
            '}';
    }
    
    // Helper methods
    
    private static String generateSummary(HallucinationReport result) {
        StringBuilder summary = new StringBuilder();
        
        summary.append("Risk Level: ").append(result.getRiskLevel());
        summary.append(" | Confidence: ").append(String.format("%.2f", result.confidenceScore));
        
        if (result.isHallucination) {
            summary.append(" | ⚠️ Hallucination detected");
        }
        
        int totalIssues = result.factInconsistencies.size() + 
                         result.contradictions.size() + 
                         result.unverifiedClaims.size();
        
        if (totalIssues > 0) {
            summary.append(" | Issues: ").append(totalIssues);
        }
        
        return summary.toString();
    }
}
