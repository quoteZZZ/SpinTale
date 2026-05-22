package com.spintale.ai.console.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class RunQueryDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long runId;
    private String runType;
    private LocalDateTime startTimeBegin;
    private LocalDateTime startTimeEnd;
}
