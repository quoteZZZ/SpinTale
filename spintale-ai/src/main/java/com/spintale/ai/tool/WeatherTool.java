package com.spintale.ai.tool;

import org.springframework.stereotype.Component;

/**
 * 天气查询工具示例
 */
@Component
public class WeatherTool implements AiTool {
    
    @Override
    public String getName() {
        return "get_weather";
    }
    
    @Override
    public String getDescription() {
        return "查询指定城市的当前天气情况";
    }
    
    @Override
    public String execute(String args) {
        // TODO: 实现真实的天气查询逻辑
        return "{\"city\": \"北京\", \"temperature\": 25, \"condition\": \"晴\", \"humidity\": 60}";
    }
}
