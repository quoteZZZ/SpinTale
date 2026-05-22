package com.spintale.ai.console.domain;

import java.time.LocalDateTime;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class AiModelConfig
{
    private Long id;
    private String modelId;
    private String modelName;
    private String providerId;
    private String modelType;
    private Integer maxContextTokens;
    private Integer maxOutputTokens;
    private Double inputPricePer1k;
    private Double outputPricePer1k;
    private Integer supportsStreaming;
    private Integer supportsFunctionCalling;
    private Integer supportsVision;
    private Integer enabled;
    private String capabilities;
    private String metadata;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
