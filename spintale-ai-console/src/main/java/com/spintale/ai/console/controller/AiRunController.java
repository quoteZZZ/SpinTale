package com.spintale.ai.console.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.spintale.common.core.controller.BaseController;
import com.spintale.common.core.domain.AjaxResult;
import com.spintale.common.core.page.TableDataInfo;
import com.spintale.ai.console.application.RunQueryService;
import com.spintale.ai.console.dto.RunQueryDTO;
import com.spintale.ai.console.vo.RunRecordVO;

@RestController
@RequestMapping("/ai/run")
public class AiRunController extends BaseController
{
    @Autowired
    private RunQueryService runQueryService;

    @PreAuthorize("@ss.hasPermi('ai:run:list')")
    @GetMapping("/list")
    public TableDataInfo list(RunQueryDTO query)
    {
        startPage();
        List<RunRecordVO> list = runQueryService.selectRunList(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('ai:run:query')")
    @GetMapping(value = "/{runId}")
    public AjaxResult getInfo(@PathVariable Long runId)
    {
        return success(runQueryService.selectRunById(runId));
    }

    @PreAuthorize("@ss.hasPermi('ai:run:trace')")
    @GetMapping(value = "/trace/{runId}")
    public AjaxResult getTrace(@PathVariable Long runId)
    {
        return success(runQueryService.selectRunTrace(runId));
    }

    @PreAuthorize("@ss.hasPermi('ai:run:cost')")
    @GetMapping("/cost")
    public AjaxResult getCostStats(RunQueryDTO query)
    {
        return success(runQueryService.getCostStats(query));
    }
}
