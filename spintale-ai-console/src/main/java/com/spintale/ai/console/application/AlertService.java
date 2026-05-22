package com.spintale.ai.console.application;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface AlertService
{
    Alert createAlert(AlertType type, AlertSeverity severity, 
            String title, String message);

    Alert createAlert(AlertType type, AlertSeverity severity,
            String title, String message, Map<String, Object> data);

    List<Alert> getActiveAlerts();

    List<Alert> getAlerts(AlertSeverity minSeverity);

    List<Alert> getAlertsByType(AlertType type);

    void acknowledge(String alertId, Long userId);

    void resolve(String alertId, String resolution);

    void cleanupOldAlerts(int daysToKeep);

    AlertStats getStats(Instant since);

    enum AlertType
    {
        PROVIDER_DOWN,
        PROVIDER_DEGRADED,
        MODEL_ERROR,
        COST_THRESHOLD_EXCEEDED,
        RATE_LIMIT_EXCEEDED,
        BUDGET_EXCEEDED,
        SYSTEM_ERROR,
        PERFORMANCE_DEGRADATION,
        SECURITY_ALERT,
        CUSTOM
    }

    enum AlertSeverity
    {
        INFO,
        WARNING,
        ERROR,
        CRITICAL
    }

    record Alert(
            String alertId,
            AlertType type,
            AlertSeverity severity,
            String title,
            String message,
            Map<String, Object> data,
            AlertStatus status,
            Long acknowledgedBy,
            Instant acknowledgedAt,
            String resolution,
            Instant createTime
    ) {}

    enum AlertStatus
    {
        ACTIVE,
        ACKNOWLEDGED,
        RESOLVED,
        DISMISSED
    }

    record AlertStats(
            long totalAlerts,
            long activeCount,
            long acknowledgedCount,
            long resolvedCount,
            java.util.Map<AlertSeverity, Long> countBySeverity,
            java.util.Map<AlertType, Long> countByType
    ) {}
}
