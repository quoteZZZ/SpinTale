package com.spintale.ai.console.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.spintale.common.annotation.Log;
import com.spintale.common.core.controller.BaseController;
import com.spintale.common.core.domain.AjaxResult;
import com.spintale.common.core.page.TableDataInfo;
import com.spintale.common.enums.BusinessType;
import com.spintale.ai.console.application.ModelAdminService;
import com.spintale.ai.console.dto.ModelConfigDTO;
import com.spintale.ai.console.vo.ModelConfigVO;

@RestController
@RequestMapping("/ai/model")
public class AiModelController extends BaseController
{
    @Autowired
    private ModelAdminService modelAdminService;

    @PreAuthorize("@ss.hasPermi('ai:model:list')")
    @GetMapping("/list")
    public TableDataInfo list(ModelConfigDTO config)
    {
        startPage();
        List<ModelConfigVO> list = modelAdminService.selectModelList(config);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('ai:model:query')")
    @GetMapping(value = "/{modelId}")
    public AjaxResult getInfo(@PathVariable Long modelId)
    {
        return success(modelAdminService.selectModelById(modelId));
    }

    @Log(title = "AI模型配置", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('ai:model:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody ModelConfigDTO config)
    {
        return toAjax(modelAdminService.insertModel(config));
    }

    @Log(title = "AI模型配置", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('ai:model:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody ModelConfigDTO config)
    {
        return toAjax(modelAdminService.updateModel(config));
    }

    @Log(title = "AI模型配置", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('ai:model:remove')")
    @DeleteMapping("/{modelIds}")
    public AjaxResult remove(@PathVariable Long[] modelIds)
    {
        return toAjax(modelAdminService.deleteModelByIds(modelIds));
    }

    @GetMapping("/provider/list")
    public AjaxResult listProviders()
    {
        return success(modelAdminService.listProviders());
    }

    @GetMapping("/catalog")
    public AjaxResult getModelCatalog()
    {
        return success(modelAdminService.getModelCatalog());
    }
}
