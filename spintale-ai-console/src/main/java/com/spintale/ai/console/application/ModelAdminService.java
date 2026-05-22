package com.spintale.ai.console.application;

import java.util.List;
import com.spintale.ai.console.dto.ModelConfigDTO;
import com.spintale.ai.console.vo.ModelConfigVO;
import com.spintale.ai.console.vo.ProviderVO;

public interface ModelAdminService
{
    List<ModelConfigVO> selectModelList(ModelConfigDTO config);

    ModelConfigVO selectModelById(Long modelId);

    int insertModel(ModelConfigDTO config);

    int updateModel(ModelConfigDTO config);

    int deleteModelByIds(Long[] modelIds);

    List<ProviderVO> listProviders();

    List<ModelConfigVO> getModelCatalog();
}
