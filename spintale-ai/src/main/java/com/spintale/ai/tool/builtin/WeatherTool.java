package com.spintale.ai.tool.builtin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.spintale.ai.tool.registry.AiTool;
import com.spintale.ai.tool.registry.AiTool.ToolSchema;

import java.net.URI;
import java.util.Map;

/**
 * 天气查询工具
 * 
 * 集成OpenWeatherMap API提供真实天气数据
 * API文档: https://openweathermap.org/api
 * 
 * 配置方式:
 * spintale:
 *   ai:
 *     tools:
 *       weather:
 *         api-key: YOUR_API_KEY
 *         base-url: https://api.openweathermap.org/data/2.5
 */
@Slf4j
@Component
public class WeatherTool implements AiTool {

    @Value("${spintale.ai.tools.weather.api-key:demo}")
    private String apiKey;
    
    @Value("${spintale.ai.tools.weather.base-url:https://api.openweathermap.org/data/2.5}")
    private String baseUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getName() {
        return "get_weather";
    }

    @Override
    public String getDescription() {
        return "获取指定城市的当前天气信息，包括温度、湿度、天气状况、风速等。支持摄氏度和华氏度。";
    }

    @Override
    public ToolSchema getSchema() {
        return ToolSchema.builder()
                .addProperty("city", "string", "城市名称（如：Beijing, Shanghai, New York）", true)
                .addProperty("unit", "string", "温度单位：metric(摄氏度), imperial(华氏度), standard(开尔文)，默认metric", false)
                .addProperty("lang", "string", "语言代码：zh_cn(中文), en(英文)等，默认zh_cn", false)
                .build();
    }

    @Override
    public String execute(Map<String, Object> arguments) {
        String city = (String) arguments.get("city");
        String unit = (String) arguments.getOrDefault("unit", "metric");
        String lang = (String) arguments.getOrDefault("lang", "zh_cn");
        
        if (city == null || city.trim().isEmpty()) {
            return "{\"error\":\"城市名称不能为空\"}";
        }
        
        log.info("Fetching weather for city: {}, unit: {}, lang: {}", city, unit, lang);
        
        try {
            // 构建API URL
            URI uri = UriComponentsBuilder.fromUriString(baseUrl + "/weather")
                    .queryParam("q", city)
                    .queryParam("appid", apiKey)
                    .queryParam("units", unit)
                    .queryParam("lang", lang)
                    .build()
                    .toUri();
            
            // 调用API
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 解析并格式化响应
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                
                // 提取关键信息
                String result = objectMapper.createObjectNode()
                        .put("city", jsonNode.get("name").asText())
                        .put("country", jsonNode.get("sys").get("country").asText())
                        .put("temperature", jsonNode.get("main").get("temp").asDouble())
                        .put("feels_like", jsonNode.get("main").get("feels_like").asDouble())
                        .put("humidity", jsonNode.get("main").get("humidity").asInt())
                        .put("pressure", jsonNode.get("main").get("pressure").asInt())
                        .put("condition", jsonNode.get("weather").get(0).get("description").asText())
                        .put("wind_speed", jsonNode.get("wind").get("speed").asDouble())
                        .put("visibility", jsonNode.has("visibility") ? jsonNode.get("visibility").asInt() : 0)
                        .put("timestamp", System.currentTimeMillis())
                        .toString();
                
                log.debug("Weather data fetched successfully for {}", city);
                return result;
            } else {
                log.warn("Failed to fetch weather data: {}", response.getStatusCode());
                return "{\"error\":\"无法获取天气数据，请检查城市名称\"}";
            }
            
        } catch (Exception e) {
            log.error("Error fetching weather data for city: {}", city, e);
            
            // 降级：返回模拟数据（仅用于演示）
            if ("demo".equals(apiKey)) {
                log.info("Using demo mode (no valid API key)");
                return getDemoWeather(city, unit);
            }
            
            return String.format("{\"error\":\"查询失败: %s\"}", e.getMessage());
        }
    }
    
    /**
     * 演示模式：返回模拟数据（仅在无API key时使用）
     */
    private String getDemoWeather(String city, String unit) {
        double temp = 20 + Math.random() * 15; // 20-35度
        int humidity = 40 + (int)(Math.random() * 40); // 40-80%
        String[] conditions = {"晴天", "多云", "阴天", "小雨"};
        String condition = conditions[(int)(Math.random() * conditions.length)];
        
        return String.format(
            "{\"city\":\"%s\",\"temperature\":%.1f,\"feels_like\":%.1f,\"humidity\":%d,\"condition\":\"%s\",\"wind_speed\":%.1f,\"note\":\"演示数据，请配置API key获取真实数据\"}",
            city, temp, temp - 2, humidity, condition, 2 + Math.random() * 5
        );
    }
}
