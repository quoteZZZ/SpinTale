package com.spintale.ai.console.application;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.spintale.ai.runtime.model.ModelAdminFacade;
import com.spintale.ai.provider.catalog.ModelCatalog;
import com.spintale.ai.provider.catalog.ModelCapability;
import com.spintale.ai.provider.catalog.ProviderCatalog;
import com.spintale.ai.console.dto.ModelConfigDTO;
import com.spintale.ai.console.vo.ModelConfigVO;
import com.spintale.ai.console.vo.ProviderVO;
import com.spintale.ai.console.mapper.AiModelConfigMapper;
import com.spintale.ai.console.domain.AiModelConfig;
import com.spintale.ai.console.convert.ModelConfigConvert;

@Service
public class ModelAdminServiceImpl implements ModelAdminService
{
    @Autowired
    private ModelAdminFacade modelAdminFacade;

    @Autowired
    private AiModelConfigMapper modelConfigMapper;

    @Override
    public List<ModelConfigVO> selectModelList(ModelConfigDTO config)
    {
        List<ModelCatalog> models = modelAdminFacade.listAllModels();
        
        return models.stream()
                .filter(m -> config.getModelName() == null || 
                        m.getModelName().contains(config.getModelName()))
                .filter(m -> config.getProvider() == null || 
                        config.getProvider().equals(m.getProvider()))
                .filter(m -> config.getStatus() == null || 
                        (config.getStatus() == 1) == m.isEnabled())
                .map(ModelConfigConvert::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public ModelConfigVO selectModelById(Long modelId)
    {
        ModelCatalog model = modelAdminFacade.getModel(String.valueOf(modelId))
                .orElse(null);
        return model != null ? ModelConfigConvert.toVO(model) : null;
    }

    @Override
    public int insertModel(ModelConfigDTO config)
    {
        ModelCatalog model = ModelCatalog.builder()
                .modelId(String.valueOf(System.currentTimeMillis()))
                .modelName(config.getModelName())
                .provider(config.getProvider())
                .modelType(config.getModelType())
                .enabled(true)
                .build();
        
        ModelCatalog saved = modelAdminFacade.registerModel(model);
        return saved != null ? 1 : 0;
    }

    @Override
    public int updateModel(ModelConfigDTO config)
    {
        ModelCatalog existing = modelAdminFacade.getModel(String.valueOf(config.getModelId()))
                .orElse(null);
        if (existing == null) return 0;

        ModelCatalog updated = ModelCatalog.builder()
                .modelId(existing.getModelId())
                .modelName(config.getModelName() != null ? config.getModelName() : existing.getModelName())
                .provider(config.getProvider() != null ? config.getProvider() : existing.getProvider())
                .modelType(config.getModelType() != null ? config.getModelType() : existing.getModelType())
                .enabled(config.getStatus() != null ? config.getStatus() == 1 : existing.isEnabled())
                .build();

        modelAdminFacade.registerModel(updated);
        return 1;
    }

    @Override
    public int deleteModelByIds(Long[] modelIds)
    {
        int count = 0;
        for (Long modelId : modelIds)
        {
            modelAdminFacade.unregisterModel(String.valueOf(modelId));
            count++;
        }
        return count;
    }

    @Override
    public List<ProviderVO> listProviders()
    {
        List<ProviderCatalog> providers = modelAdminFacade.listAllProviders();
        return providers.stream()
                .map(p -> ProviderVO.builder()
                        .providerId(p.getProviderId())
                        .providerName(p.getProviderName())
                        .providerType(p.getProviderType().name())
                        .status(p.isHealthy() ? 1 : 0)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<ModelConfigVO> getModelCatalog()
    {
        List<ModelCatalog> models = modelAdminFacade.getAvailableModels();
        return models.stream()
                .map(ModelConfigConvert::toVO)
                .collect(Collectors.toList());
    }
}
