package com.spintale.ai.console.dto;

import java.io.Serializable;
import lombok.Data;

@Data
public class ModelConfigDTO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long modelId;
    private String modelName;
    private String provider;
    private String modelType;
    private Integer status;
}
