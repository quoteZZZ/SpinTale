package com.spintale.ai.console.convert;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import com.spintale.ai.provider.catalog.ModelCatalog;
import com.spintale.ai.console.vo.ModelConfigVO;

public class ModelConfigConvert
{
    public static ModelConfigVO toVO(ModelCatalog model)
    {
        return ModelConfigVO.builder()
                .modelId(extractId(model.getModelId()))
                .modelName(model.getModelName())
                .provider(model.getProvider())
                .modelType(model.getModelType())
                .maxTokens(model.getCapability() != null ? 
                        model.getCapability().getMaxContextTokens() : null)
                .costPerToken(model.getPricing() != null ? 
                        model.getPricing().getInputPricePer1k() : null)
                .status(model.isEnabled() ? 1 : 0)
                .createTime(toLocalDateTime(model.getCreatedAt()))
                .build();
    }

    public static ModelCatalog toEntity(ModelConfigVO vo)
    {
        return ModelCatalog.builder()
                .modelId(String.valueOf(vo.getModelId()))
                .modelName(vo.getModelName())
                .provider(vo.getProvider())
                .modelType(vo.getModelType())
                .enabled(vo.getStatus() != null && vo.getStatus() == 1)
                .build();
    }

    private static Long extractId(String id)
    {
        if (id == null) return null;
        try
        {
            return Long.parseLong(id.replace("-", "").substring(0, 18));
        }
        catch (Exception e)
        {
            return (long) id.hashCode();
        }
    }

    private static LocalDateTime toLocalDateTime(Instant instant)
    {
        return instant != null ? 
                LocalDateTime.ofInstant(instant, ZoneId.systemDefault()) : null;
    }
}
