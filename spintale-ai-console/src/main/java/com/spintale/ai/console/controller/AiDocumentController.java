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
import com.spintale.ai.console.application.DocumentAdminService;
import com.spintale.ai.console.dto.DocumentDTO;
import com.spintale.ai.console.vo.DocumentVO;

@RestController
@RequestMapping("/ai/document")
public class AiDocumentController extends BaseController
{
    @Autowired
    private DocumentAdminService documentAdminService;

    @PreAuthorize("@ss.hasPermi('ai:document:list')")
    @GetMapping("/list")
    public TableDataInfo list(DocumentDTO doc)
    {
        startPage();
        List<DocumentVO> list = documentAdminService.selectDocumentList(doc);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('ai:document:query')")
    @GetMapping(value = "/{docId}")
    public AjaxResult getInfo(@PathVariable Long docId)
    {
        return success(documentAdminService.selectDocumentById(docId));
    }

    @Log(title = "AI文档", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('ai:document:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody DocumentDTO doc)
    {
        return toAjax(documentAdminService.insertDocument(doc));
    }

    @Log(title = "AI文档", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('ai:document:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody DocumentDTO doc)
    {
        return toAjax(documentAdminService.updateDocument(doc));
    }

    @Log(title = "AI文档", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('ai:document:remove')")
    @DeleteMapping("/{docIds}")
    public AjaxResult remove(@PathVariable Long[] docIds)
    {
        return toAjax(documentAdminService.deleteDocumentByIds(docIds));
    }

    @Log(title = "AI文档索引", businessType = BusinessType.OTHER)
    @PreAuthorize("@ss.hasPermi('ai:document:index')")
    @PostMapping("/index/{docId}")
    public AjaxResult startIndex(@PathVariable Long docId)
    {
        return toAjax(documentAdminService.startIndexJob(docId));
    }

    @GetMapping("/chunk/{docId}")
    public AjaxResult listChunks(@PathVariable Long docId)
    {
        return success(documentAdminService.listDocumentChunks(docId));
    }
}
