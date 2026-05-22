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
import com.spintale.ai.console.application.KnowledgeAdminService;
import com.spintale.ai.console.dto.KnowledgeBaseDTO;
import com.spintale.ai.console.vo.KnowledgeBaseVO;

@RestController
@RequestMapping("/ai/knowledge")
public class AiKnowledgeController extends BaseController
{
    @Autowired
    private KnowledgeAdminService knowledgeAdminService;

    @PreAuthorize("@ss.hasPermi('ai:knowledge:list')")
    @GetMapping("/list")
    public TableDataInfo list(KnowledgeBaseDTO kb)
    {
        startPage();
        List<KnowledgeBaseVO> list = knowledgeAdminService.selectKnowledgeBaseList(kb);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('ai:knowledge:query')")
    @GetMapping(value = "/{kbId}")
    public AjaxResult getInfo(@PathVariable Long kbId)
    {
        return success(knowledgeAdminService.selectKnowledgeBaseById(kbId));
    }

    @Log(title = "AI知识库", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('ai:knowledge:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody KnowledgeBaseDTO kb)
    {
        return toAjax(knowledgeAdminService.insertKnowledgeBase(kb));
    }

    @Log(title = "AI知识库", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('ai:knowledge:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody KnowledgeBaseDTO kb)
    {
        return toAjax(knowledgeAdminService.updateKnowledgeBase(kb));
    }

    @Log(title = "AI知识库", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('ai:knowledge:remove')")
    @DeleteMapping("/{kbIds}")
    public AjaxResult remove(@PathVariable Long[] kbIds)
    {
        return toAjax(knowledgeAdminService.deleteKnowledgeBaseByIds(kbIds));
    }
}
