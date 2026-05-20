package com.spintale.ai.core.metrics.hallucination;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provider-agnostic hallucination risk detector.
 */
public class HallucinationDetector {

    private static final Logger log = LoggerFactory.getLogger(HallucinationDetector.class);

    private static final List<Pattern> SUSPICIOUS_PATTERNS = List.of(
            Pattern.compile("\\b\\d{4,}\\b"),
            Pattern.compile("(always|never|guaranteed|100%|absolutely)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(research shows|experts say|data proves)", Pattern.CASE_INSENSITIVE));

    private final Map<String, Set<String>> userFacts = new HashMap<>();

    public void addUserFact(String userId, String fact) {
        if (userId == null || fact == null || fact.isBlank()) {
            return;
        }
        userFacts.computeIfAbsent(userId, ignored -> new HashSet<>()).add(fact.toLowerCase());
    }

    public void addUserFacts(String userId, Collection<String> facts) {
        if (facts == null) {
            return;
        }
        facts.forEach(fact -> addUserFact(userId, fact));
    }

    public void clearUserFacts(String userId) {
        userFacts.remove(userId);
    }

    public HallucinationResult detectHallucination(String userId, String context, String response) {
        HallucinationResult result = new HallucinationResult();
        result.setResponse(response);

        String content = response == null ? "" : response;
        List<String> inconsistencies = checkFactConsistency(userId, content);
        result.setFactInconsistencies(inconsistencies);
        if (!inconsistencies.isEmpty()) {
            result.addFlag("FACT_INCONSISTENCY");
        }

        List<SuspiciousPatternMatch> suspiciousPatterns = checkSuspiciousPatterns(content);
        result.setSuspiciousPatterns(suspiciousPatterns);
        if (!suspiciousPatterns.isEmpty()) {
            result.addFlag("SUSPICIOUS_PATTERN");
        }

        List<String> unverifiedClaims = checkUnverifiedClaims(content);
        result.setUnverifiedClaims(unverifiedClaims);
        if (!unverifiedClaims.isEmpty()) {
            result.addFlag("UNVERIFIED_CLAIM");
        }

        double confidence = calculateConfidence(result);
        result.setOverallConfidence(confidence);
        result.setIsHallucination(confidence < 0.5 || !inconsistencies.isEmpty());

        log.debug("Hallucination detection completed: userId={}, confidence={}, isHallucination={}",
                userId, confidence, result.getIsHallucination());
        return result;
    }

    public String generateResponseWithConfidence(String originalResponse, HallucinationResult result) {
        if (result == null || result.getOverallConfidence() >= 0.8) {
            return originalResponse;
        }
        return originalResponse + "\n\n[Low confidence: verify key facts before use.]";
    }

    private List<String> checkFactConsistency(String userId, String response) {
        List<String> inconsistencies = new ArrayList<>();
        Set<String> facts = userFacts.get(userId);
        if (facts == null || facts.isEmpty()) {
            return inconsistencies;
        }

        String lowerResponse = response.toLowerCase();
        for (String fact : facts) {
            if (lowerResponse.contains("not " + fact) || lowerResponse.contains("no " + fact)) {
                inconsistencies.add("Potentially contradicts known fact: " + fact);
            }
        }
        return inconsistencies;
    }

    private List<SuspiciousPatternMatch> checkSuspiciousPatterns(String response) {
        List<SuspiciousPatternMatch> matches = new ArrayList<>();
        for (Pattern pattern : SUSPICIOUS_PATTERNS) {
            Matcher matcher = pattern.matcher(response);
            while (matcher.find()) {
                SuspiciousPatternMatch match = new SuspiciousPatternMatch();
                match.setPattern(pattern.pattern());
                match.setMatchedText(matcher.group());
                match.setStartPosition(matcher.start());
                match.setEndPosition(matcher.end());
                matches.add(match);
            }
        }
        return matches;
    }

    private List<String> checkUnverifiedClaims(String response) {
        List<String> unverified = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\b\\d+(?:\\.\\d+)?%\\b").matcher(response);
        while (matcher.find()) {
            int start = Math.max(0, matcher.start() - 50);
            int end = Math.min(response.length(), matcher.end() + 50);
            String nearby = response.substring(start, end).toLowerCase();
            if (!nearby.contains("source") && !nearby.contains("according to")) {
                unverified.add("Statistic without nearby source: " + matcher.group());
            }
        }
        return unverified;
    }

    private double calculateConfidence(HallucinationResult result) {
        double confidence = 0.85;
        confidence -= Math.min(0.35, result.getFactInconsistencies().size() * 0.20);
        confidence -= Math.min(0.25, result.getSuspiciousPatterns().size() * 0.05);
        confidence -= Math.min(0.20, result.getUnverifiedClaims().size() * 0.05);
        return Math.max(0.0, Math.min(1.0, confidence));
    }

    public static class HallucinationResult {
        private String response;
        private boolean isHallucination;
        private double overallConfidence;
        private Set<String> flags = new HashSet<>();
        private List<String> factInconsistencies = new ArrayList<>();
        private List<SuspiciousPatternMatch> suspiciousPatterns = new ArrayList<>();
        private List<String> contradictions = new ArrayList<>();
        private List<String> unverifiedClaims = new ArrayList<>();
        private ConfidenceScore aiConfidence;

        public void addFlag(String flag) {
            flags.add(flag);
        }

        public String getResponse() {
            return response;
        }

        public void setResponse(String response) {
            this.response = response;
        }

        public boolean getIsHallucination() {
            return isHallucination;
        }

        public void setIsHallucination(boolean hallucination) {
            isHallucination = hallucination;
        }

        public double getOverallConfidence() {
            return overallConfidence;
        }

        public void setOverallConfidence(double overallConfidence) {
            this.overallConfidence = overallConfidence;
        }

        public Set<String> getFlags() {
            return flags;
        }

        public List<String> getFactInconsistencies() {
            return factInconsistencies;
        }

        public void setFactInconsistencies(List<String> factInconsistencies) {
            this.factInconsistencies = factInconsistencies;
        }

        public List<SuspiciousPatternMatch> getSuspiciousPatterns() {
            return suspiciousPatterns;
        }

        public void setSuspiciousPatterns(List<SuspiciousPatternMatch> suspiciousPatterns) {
            this.suspiciousPatterns = suspiciousPatterns;
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

        public ConfidenceScore getAiConfidence() {
            return aiConfidence;
        }

        public void setAiConfidence(ConfidenceScore aiConfidence) {
            this.aiConfidence = aiConfidence;
        }
    }

    public static class SuspiciousPatternMatch {
        private String pattern;
        private String matchedText;
        private int startPosition;
        private int endPosition;

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public String getMatchedText() {
            return matchedText;
        }

        public void setMatchedText(String matchedText) {
            this.matchedText = matchedText;
        }

        public int getStartPosition() {
            return startPosition;
        }

        public void setStartPosition(int startPosition) {
            this.startPosition = startPosition;
        }

        public int getEndPosition() {
            return endPosition;
        }

        public void setEndPosition(int endPosition) {
            this.endPosition = endPosition;
        }
    }

    public static class ConfidenceScore {
        private double confidence;
        private List<String> reasons = new ArrayList<>();
        private List<String> suggestions = new ArrayList<>();

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }

        public List<String> getReasons() {
            return reasons;
        }

        public void setReasons(List<String> reasons) {
            this.reasons = reasons;
        }

        public List<String> getSuggestions() {
            return suggestions;
        }

        public void setSuggestions(List<String> suggestions) {
            this.suggestions = suggestions;
        }
    }
}
