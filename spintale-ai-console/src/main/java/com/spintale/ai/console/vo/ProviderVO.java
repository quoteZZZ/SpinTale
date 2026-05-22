package com.spintale.ai.console.vo;

import java.io.Serializable;
import lombok.Data;

@Data
public class ProviderVO implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String providerId;
    private String providerName;
    private String providerType;
    private Integer status;
}
