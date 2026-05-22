package com.spintale.ai.console.application;

import java.util.List;
import com.spintale.ai.console.dto.AgentConfigDTO;
import com.spintale.ai.console.vo.AgentConfigVO;
import com.spintale.ai.console.vo.ToolDefinitionVO;

public interface AgentAdminService
{
    List<AgentConfigVO> selectAgentList(AgentConfigDTO agent);

    AgentConfigVO selectAgentById(Long agentId);

    int insertAgent(AgentConfigDTO agent);

    int updateAgent(AgentConfigDTO agent);

    int deleteAgentByIds(Long[] agentIds);

    List<ToolDefinitionVO> listAvailableTools();
}
