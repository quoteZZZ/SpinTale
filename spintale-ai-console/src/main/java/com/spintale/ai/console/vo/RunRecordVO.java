package com.spintale.ai.console.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class RunRecordVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long runId;
    private String runType;
    private String model;
    private Long inputTokens;
    private Long outputTokens;
    private Double cost;
    private Long durationMs;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
