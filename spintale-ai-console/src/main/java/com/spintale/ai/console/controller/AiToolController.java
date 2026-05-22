package com.spintale.ai.console.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.spintale.common.core.controller.BaseController;
import com.spintale.common.core.domain.AjaxResult;
import com.spintale.ai.console.application.ToolAdminService;
import com.spintale.ai.console.vo.ToolDefinitionVO;

@RestController
@RequestMapping("/ai/tool")
public class AiToolController extends BaseController
{
    @Autowired
    private ToolAdminService toolAdminService;

    @PreAuthorize("@ss.hasPermi('ai:tool:list')")
    @GetMapping("/list")
    public AjaxResult list()
    {
        List<ToolDefinitionVO> list = toolAdminService.listTools();
        return success(list);
    }

    @PreAuthorize("@ss.hasPermi('ai:tool:query')")
    @GetMapping(value = "/{toolId}")
    public AjaxResult getInfo(String toolId)
    {
        return success(toolAdminService.getToolById(toolId));
    }
}
