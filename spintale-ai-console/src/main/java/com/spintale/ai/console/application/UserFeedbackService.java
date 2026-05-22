package com.spintale.ai.console.application;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserFeedbackService
{
    Feedback submitFeedback(String runId, Long userId, FeedbackType type, 
            int rating, String comment);

    Feedback submitFeedback(String runId, Long userId, FeedbackType type,
            int rating, String comment, List<String> tags);

    Optional<Feedback> getFeedback(String feedbackId);

    List<Feedback> getFeedbacksByRun(String runId);

    List<Feedback> getFeedbacksByUser(Long userId);

    List<Feedback> getNegativeFeedbacks(Instant since);

    FeedbackSummary getSummary(Instant start, Instant end);

    void markAsReviewed(String feedbackId);

    void addTags(String feedbackId, List<String> tags);

    enum FeedbackType
    {
        GENERAL,
        ACCURACY,
        HELPFULNESS,
        RELEVANCE,
        SAFETY,
        QUALITY
    }

    record Feedback(
            String feedbackId,
            String runId,
            Long userId,
            FeedbackType type,
            int rating,
            String comment,
            List<String> tags,
            boolean isPositive,
            boolean reviewed,
            Instant createTime
    ) {}

    record FeedbackSummary(
            long totalCount,
            long positiveCount,
            long negativeCount,
            double averageRating,
            double positiveRate,
            java.util.Map<FeedbackType, Double> ratingByType
    ) {}
}
