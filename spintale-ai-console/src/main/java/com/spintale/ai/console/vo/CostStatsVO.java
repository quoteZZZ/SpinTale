package com.spintale.ai.console.vo;

import java.io.Serializable;
import lombok.Data;

@Data
public class CostStatsVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long totalRuns;
    private Long totalInputTokens;
    private Long totalOutputTokens;
    private Double totalCost;
}
