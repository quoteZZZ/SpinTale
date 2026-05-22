package com.spintale.ai.console.dto;

import java.io.Serializable;
import lombok.Data;

@Data
public class KnowledgeBaseDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long kbId;
    private String kbName;
    private String description;
    private String embeddingModel;
    private Integer status;
}
