package com.spintale.ai.console.domain;

import java.time.LocalDateTime;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class AiKnowledgeBase
{
    private Long id;
    private String kbId;
    private String kbName;
    private String description;
    private String embeddingModel;
    private Integer vectorDimension;
    private Integer chunkSize;
    private Integer chunkOverlap;
    private Integer documentCount;
    private Integer chunkCount;
    private String status;
    private Long createBy;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
