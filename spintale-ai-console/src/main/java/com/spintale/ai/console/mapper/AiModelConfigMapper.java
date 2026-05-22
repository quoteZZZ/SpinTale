package com.spintale.ai.console.mapper;

import java.util.List;
import org.apache.ibatis.annotations.*;
import com.spintale.ai.console.domain.AiModelConfig;

@Mapper
public interface AiModelConfigMapper
{
    @Select("SELECT * FROM ai_model_config WHERE model_id = #{modelId}")
    AiModelConfig selectByModelId(@Param("modelId") String modelId);

    @Select("SELECT * FROM ai_model_config WHERE provider_id = #{providerId}")
    List<AiModelConfig> selectByProviderId(@Param("providerId") String providerId);

    @Select("SELECT * FROM ai_model_config WHERE enabled = 1")
    List<AiModelConfig> selectEnabled();

    @Select("SELECT * FROM ai_model_config WHERE model_type = #{modelType}")
    List<AiModelConfig> selectByType(@Param("modelType") String modelType);

    @Insert("INSERT INTO ai_model_config(model_id, model_name, provider_id, model_type, " +
            "max_context_tokens, max_output_tokens, input_price_per_1k, output_price_per_1k, " +
            "supports_streaming, supports_function_calling, supports_vision, enabled, " +
            "capabilities, metadata, create_time) " +
            "VALUES(#{modelId}, #{modelName}, #{providerId}, #{modelType}, " +
            "#{maxContextTokens}, #{maxOutputTokens}, #{inputPricePer1k}, #{outputPricePer1k}, " +
            "#{supportsStreaming}, #{supportsFunctionCalling}, #{supportsVision}, #{enabled}, " +
            "#{capabilities}, #{metadata}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiModelConfig config);

    @Update("UPDATE ai_model_config SET model_name = #{modelName}, " +
            "provider_id = #{providerId}, model_type = #{modelType}, " +
            "max_context_tokens = #{maxContextTokens}, max_output_tokens = #{maxOutputTokens}, " +
            "input_price_per_1k = #{inputPricePer1k}, output_price_per_1k = #{outputPricePer1k}, " +
            "supports_streaming = #{supportsStreaming}, supports_function_calling = #{supportsFunctionCalling}, " +
            "supports_vision = #{supportsVision}, enabled = #{enabled}, " +
            "capabilities = #{capabilities}, metadata = #{metadata}, update_time = NOW() " +
            "WHERE model_id = #{modelId}")
    int update(AiModelConfig config);

    @Delete("DELETE FROM ai_model_config WHERE model_id = #{modelId}")
    int deleteByModelId(@Param("modelId") String modelId);

    @Update("UPDATE ai_model_config SET enabled = 1, update_time = NOW() WHERE model_id = #{modelId}")
    int enable(@Param("modelId") String modelId);

    @Update("UPDATE ai_model_config SET enabled = 0, update_time = NOW() WHERE model_id = #{modelId}")
    int disable(@Param("modelId") String modelId);
}
