package com.spintale.ai.console.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ModelConfigVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long modelId;
    private String modelName;
    private String provider;
    private String modelType;
    private Integer maxTokens;
    private Double costPerToken;
    private Integer status;
    private LocalDateTime createTime;
}
