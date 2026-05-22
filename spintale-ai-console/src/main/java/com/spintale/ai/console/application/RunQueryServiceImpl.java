package com.spintale.ai.console.application;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.spintale.ai.runtime.execution.AiRunLedger;
import com.spintale.ai.runtime.execution.AiRunContext;
import com.spintale.ai.runtime.execution.AiRunResult;
import com.spintale.ai.runtime.observability.CostRecorder;
import com.spintale.ai.console.dto.RunQueryDTO;
import com.spintale.ai.console.vo.RunRecordVO;
import com.spintale.ai.console.vo.RunTraceVO;
import com.spintale.ai.console.vo.CostStatsVO;

@Service
public class RunQueryServiceImpl implements RunQueryService
{
    @Autowired
    private AiRunLedger runLedger;

    @Autowired
    private CostRecorder costRecorder;

    @Override
    public List<RunRecordVO> selectRunList(RunQueryDTO query)
    {
        List<AiRunResult> results = runLedger.queryRuns(
                query.getRunId() != null ? query.getRunId() : null,
                query.getStartTimeBegin(),
                query.getStartTimeEnd(),
                100
        );

        return results.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public RunRecordVO selectRunById(Long runId)
    {
        Optional<AiRunResult> result = runLedger.getResult(String.valueOf(runId));
        return result.map(this::toVO).orElse(null);
    }

    @Override
    public List<RunTraceVO> selectRunTrace(Long runId)
    {
        return runLedger.getSpans(String.valueOf(runId)).stream()
                .map(span -> RunTraceVO.builder()
                        .spanId(span.getSpanId())
                        .spanName(span.getName())
                        .spanType(span.getSpanType().name())
                        .durationMs(span.getDurationMs())
                        .startTime(span.getStartTime())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public CostStatsVO getCostStats(RunQueryDTO query)
    {
        AiRunLedger.CostSummary summary = runLedger.getCostSummary(
                query.getRunId(),
                query.getStartTimeBegin(),
                query.getStartTimeEnd()
        );

        return CostStatsVO.builder()
                .totalRuns(summary.totalRuns())
                .totalInputTokens(summary.totalInputTokens())
                .totalOutputTokens(summary.totalOutputTokens())
                .totalCost(summary.totalCost())
                .build();
    }

    private RunRecordVO toVO(AiRunResult result)
    {
        return RunRecordVO.builder()
                .runId(Long.parseLong(result.getRunId().replace("-", "").substring(0, 18)))
                .runType(result.getMetadata() != null ? String.valueOf(result.getMetadata()) : "CHAT")
                .model(result.getModel())
                .inputTokens(result.getInputTokens())
                .outputTokens(result.getOutputTokens())
                .cost(result.getCost())
                .durationMs(result.getDurationMs())
                .status(result.isSuccess() ? "SUCCEEDED" : "FAILED")
                .startTime(result.getStartTime())
                .endTime(result.getEndTime())
                .build();
    }
}
