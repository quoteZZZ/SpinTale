package com.spintale.ai.console.mapper;

import java.util.List;
import org.apache.ibatis.annotations.*;
import com.spintale.ai.console.domain.AiRun;

@Mapper
public interface AiRunMapper
{
    @Select("SELECT * FROM ai_run WHERE run_id = #{runId}")
    AiRun selectByRunId(@Param("runId") String runId);

    @Select("SELECT * FROM ai_run WHERE user_id = #{userId} ORDER BY start_time DESC LIMIT #{limit}")
    List<AiRun> selectByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    @Select("SELECT * FROM ai_run WHERE status = #{status} ORDER BY start_time DESC LIMIT 100")
    List<AiRun> selectByStatus(@Param("status") String status);

    @Select("SELECT * FROM ai_run WHERE start_time BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY start_time DESC LIMIT 1000")
    List<AiRun> selectByTimeRange(@Param("startTime") String startTime, 
            @Param("endTime") String endTime);

    @Insert("INSERT INTO ai_run(run_id, trace_id, parent_run_id, run_type, model, provider, " +
            "user_id, session_id, status, input_text, output_text, input_tokens, output_tokens, " +
            "total_cost, duration_ms, error_message, error_code, start_time, create_time) " +
            "VALUES(#{runId}, #{traceId}, #{parentRunId}, #{runType}, #{model}, #{provider}, " +
            "#{userId}, #{sessionId}, #{status}, #{inputText}, #{outputText}, #{inputTokens}, " +
            "#{outputTokens}, #{totalCost}, #{durationMs}, #{errorMessage}, #{errorCode}, " +
            "#{startTime}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiRun run);

    @Update("UPDATE ai_run SET status = #{status}, output_text = #{outputText}, " +
            "input_tokens = #{inputTokens}, output_tokens = #{outputTokens}, " +
            "total_cost = #{totalCost}, duration_ms = #{durationMs}, " +
            "error_message = #{errorMessage}, end_time = #{endTime} " +
            "WHERE run_id = #{runId}")
    int updateResult(AiRun run);

    @Select("SELECT COUNT(*) as total_runs, SUM(input_tokens) as total_input_tokens, " +
            "SUM(output_tokens) as total_output_tokens, SUM(total_cost) as total_cost " +
            "FROM ai_run WHERE user_id = #{userId} AND start_time >= #{startTime}")
    java.util.Map<String, Object> selectStats(@Param("userId") Long userId, 
            @Param("startTime") String startTime);
}
