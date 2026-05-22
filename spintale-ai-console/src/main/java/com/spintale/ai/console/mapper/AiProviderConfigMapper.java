package com.spintale.ai.console.mapper;

import java.util.List;
import org.apache.ibatis.annotations.*;
import com.spintale.ai.console.domain.AiProviderConfig;

@Mapper
public interface AiProviderConfigMapper
{
    @Select("SELECT * FROM ai_provider_config WHERE provider_id = #{providerId}")
    AiProviderConfig selectByProviderId(@Param("providerId") String providerId);

    @Select("SELECT * FROM ai_provider_config WHERE enabled = 1")
    List<AiProviderConfig> selectEnabled();

    @Select("SELECT * FROM ai_provider_config WHERE health_status = #{healthStatus}")
    List<AiProviderConfig> selectByHealthStatus(@Param("healthStatus") String healthStatus);

    @Insert("INSERT INTO ai_provider_config(provider_id, provider_name, provider_type, " +
            "base_url, api_key_ref, enabled, health_status, config_json, create_time) " +
            "VALUES(#{providerId}, #{providerName}, #{providerType}, " +
            "#{baseUrl}, #{apiKeyRef}, #{enabled}, #{healthStatus}, #{configJson}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiProviderConfig config);

    @Update("UPDATE ai_provider_config SET provider_name = #{providerName}, " +
            "provider_type = #{providerType}, base_url = #{baseUrl}, " +
            "api_key_ref = #{apiKeyRef}, enabled = #{enabled}, " +
            "health_status = #{healthStatus}, config_json = #{configJson}, " +
            "update_time = NOW() WHERE provider_id = #{providerId}")
    int update(AiProviderConfig config);

    @Delete("DELETE FROM ai_provider_config WHERE provider_id = #{providerId}")
    int deleteByProviderId(@Param("providerId") String providerId);

    @Update("UPDATE ai_provider_config SET health_status = #{healthStatus}, " +
            "last_health_check = NOW(), update_time = NOW() WHERE provider_id = #{providerId}")
    int updateHealthStatus(@Param("providerId") String providerId, 
            @Param("healthStatus") String healthStatus);
}
