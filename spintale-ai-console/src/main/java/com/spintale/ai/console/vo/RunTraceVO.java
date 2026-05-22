package com.spintale.ai.console.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class RunTraceVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long spanId;
    private String spanName;
    private String spanType;
    private Long durationMs;
    private LocalDateTime startTime;
}
