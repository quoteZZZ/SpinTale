package com.spintale.ai.console.application;

import java.util.List;
import com.spintale.ai.console.dto.RunQueryDTO;
import com.spintale.ai.console.vo.CostStatsVO;
import com.spintale.ai.console.vo.RunRecordVO;
import com.spintale.ai.console.vo.RunTraceVO;

public interface RunQueryService
{
    List<RunRecordVO> selectRunList(RunQueryDTO query);

    RunRecordVO selectRunById(Long runId);

    List<RunTraceVO> selectRunTrace(Long runId);

    CostStatsVO getCostStats(RunQueryDTO query);
}
