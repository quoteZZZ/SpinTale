package com.spintale.ai.console.domain;

import java.time.LocalDateTime;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class AiProviderConfig
{
    private Long id;
    private String providerId;
    private String providerName;
    private String providerType;
    private String baseUrl;
    private String apiKeyRef;
    private Integer enabled;
    private String healthStatus;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastHealthCheck;
    
    private String configJson;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
