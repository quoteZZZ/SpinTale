package com.spintale.ai.console.application;

import java.util.List;
import com.spintale.ai.console.vo.ToolDefinitionVO;

public interface ToolAdminService
{
    List<ToolDefinitionVO> listTools();

    ToolDefinitionVO getToolById(String toolId);
}
