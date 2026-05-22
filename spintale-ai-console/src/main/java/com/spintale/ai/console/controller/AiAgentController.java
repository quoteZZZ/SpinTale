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
import com.spintale.ai.console.application.AgentAdminService;
import com.spintale.ai.console.dto.AgentConfigDTO;
import com.spintale.ai.console.vo.AgentConfigVO;

@RestController
@RequestMapping("/ai/agent")
public class AiAgentController extends BaseController
{
    @Autowired
    private AgentAdminService agentAdminService;

    @PreAuthorize("@ss.hasPermi('ai:agent:list')")
    @GetMapping("/list")
    public TableDataInfo list(AgentConfigDTO agent)
    {
        startPage();
        List<AgentConfigVO> list = agentAdminService.selectAgentList(agent);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('ai:agent:query')")
    @GetMapping(value = "/{agentId}")
    public AjaxResult getInfo(@PathVariable Long agentId)
    {
        return success(agentAdminService.selectAgentById(agentId));
    }

    @Log(title = "AI Agent", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('ai:agent:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody AgentConfigDTO agent)
    {
        return toAjax(agentAdminService.insertAgent(agent));
    }

    @Log(title = "AI Agent", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('ai:agent:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody AgentConfigDTO agent)
    {
        return toAjax(agentAdminService.updateAgent(agent));
    }

    @Log(title = "AI Agent", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('ai:agent:remove')")
    @DeleteMapping("/{agentIds}")
    public AjaxResult remove(@PathVariable Long[] agentIds)
    {
        return toAjax(agentAdminService.deleteAgentByIds(agentIds));
    }

    @GetMapping("/tool/list")
    public AjaxResult listTools()
    {
        return success(agentAdminService.listAvailableTools());
    }
}
