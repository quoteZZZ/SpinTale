package com.spintale.ai.console.domain;

import java.time.LocalDateTime;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class AiRun
{
    private Long id;
    private String runId;
    private String traceId;
    private String parentRunId;
    private String runType;
    private String model;
    private String provider;
    private Long userId;
    private String sessionId;
    private String status;
    private String inputText;
    private String outputText;
    private Long inputTokens;
    private Long outputTokens;
    private Double totalCost;
    private Long durationMs;
    private String errorMessage;
    private String errorCode;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
