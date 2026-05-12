package com.spintale.ai.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * 天气查询工具
 * 使用 OpenWeatherMap API 或类似服务获取真实天气数据
 */
@Component
public class WeatherTool implements AiTool {
    
    private static final Logger log = LoggerFactory.getLogger(WeatherTool.class);
    
    // 可配置的天气 API 端点（支持多个提供商）
    private static final String OPENWEATHERMAP_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String WEATHER_API_URL = "https://api.weatherapi.com/v1/current.json";
    
    private final HttpClient httpClient;
    
    // 从环境变量或配置获取 API Key（生产环境应使用配置中心）
    private final String openWeatherApiKey;
    private final String weatherApiApiKey;
    
    public WeatherTool() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.openWeatherApiKey = System.getenv("OPENWEATHER_API_KEY");
        this.weatherApiApiKey = System.getenv("WEATHERAPI_API_KEY");
    }
    
    @Override
    public String getName() {
        return "get_weather";
    }
    
    @Override
    public String getDescription() {
        return "查询指定城市的当前天气情况，包括温度、湿度、天气状况等实时数据";
    }
    
    @Override
    public String execute(String args) {
        try {
            // 解析参数：期望格式 {"city": "北京"} 或直接城市名
            String city = extractCity(args);
            
            if (city == null || city.trim().isEmpty()) {
                return "{\"error\": \"请提供城市名称\", \"example\": \"{\\\"city\\\": \\\"北京\\\"}\"}";
            }
            
            // 优先使用 WeatherAPI（更稳定），回退到 OpenWeatherMap
            if (weatherApiApiKey != null && !weatherApiApiKey.isEmpty()) {
                return fetchFromWeatherApi(city);
            } else if (openWeatherApiKey != null && !openWeatherApiKey.isEmpty()) {
                return fetchFromOpenWeatherMap(city);
            } else {
                // 无 API Key 时返回示例数据并提示配置
                return createDemoResponse(city) + 
                       "\n\n[提示] 配置 OPENWEATHER_API_KEY 或 WEATHERAPI_API_KEY 环境变量以获取真实天气数据";
            }
            
        } catch (Exception e) {
            log.error("Weather query failed: {}", e.getMessage());
            return "{\"error\": \"天气查询失败：" + e.getMessage() + "\"}";
        }
    }
    
    /**
     * 从参数中提取城市名
     */
    private String extractCity(String args) {
        if (args == null || args.trim().isEmpty()) {
            return null;
        }
        
        // 尝试解析 JSON
        if (args.trim().startsWith("{")) {
            try {
                // 简单 JSON 解析（生产环境应使用 Jackson 或 FastJSON）
                int start = args.indexOf("\"city\"");
                if (start >= 0) {
                    int colonPos = args.indexOf(":", start);
                    int quoteStart = args.indexOf("\"", colonPos) + 1;
                    int quoteEnd = args.indexOf("\"", quoteStart);
                    if (quoteStart > 0 && quoteEnd > quoteStart) {
                        return args.substring(quoteStart, quoteEnd);
                    }
                }
            } catch (Exception e) {
                // 解析失败，尝试直接作为城市名
            }
        }
        
        // 直接作为城市名
        return args.trim();
    }
    
    /**
     * 从 WeatherAPI 获取天气
     */
    private String fetchFromWeatherApi(String city) throws Exception {
        String url = String.format("%s?key=%s&q=%s&aqi=no&lang=zh", 
                WEATHER_API_URL, weatherApiApiKey, city);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            // 解析并格式化响应
            String body = response.body();
            return formatWeatherApiResponse(body);
        } else {
            throw new Exception("WeatherAPI 返回错误：" + response.statusCode());
        }
    }
    
    /**
     * 从 OpenWeatherMap 获取天气
     */
    private String fetchFromOpenWeatherMap(String city) throws Exception {
        String url = String.format("%s?q=%s&appid=%s&units=metric&lang=zh_cn", 
                OPENWEATHERMAP_URL, city, openWeatherApiKey);
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return formatOpenWeatherResponse(response.body());
        } else {
            throw new Exception("OpenWeatherMap 返回错误：" + response.statusCode());
        }
    }
    
    /**
     * 格式化 WeatherAPI 响应
     */
    private String formatWeatherApiResponse(String json) {
        // 简化解析（生产环境应使用 JSON 库）
        try {
            int tempStart = json.indexOf("\"temp_c\"") + 9;
            int tempEnd = json.indexOf(",", tempStart);
            double temp = Double.parseDouble(json.substring(tempStart, tempEnd));
            
            int conditionStart = json.indexOf("\"text\"") + 8;
            int conditionEnd = json.indexOf("\"", conditionStart);
            String condition = json.substring(conditionStart, conditionEnd);
            
            int humidityStart = json.indexOf("\"humidity\"") + 11;
            int humidityEnd = json.indexOf(",", humidityStart);
            int humidity = Integer.parseInt(json.substring(humidityStart, humidityEnd));
            
            return String.format("{\"temperature\": %.1f, \"condition\": \"%s\", \"humidity\": %d, \"source\": \"WeatherAPI\"}", 
                    temp, condition, humidity);
        } catch (Exception e) {
            return json;
        }
    }
    
    /**
     * 格式化 OpenWeatherMap 响应
     */
    private String formatOpenWeatherResponse(String json) {
        try {
            int tempStart = json.indexOf("\"temp\"") + 7;
            int tempEnd = json.indexOf(",", tempStart);
            double temp = Double.parseDouble(json.substring(tempStart, tempEnd));
            
            int humidityStart = json.indexOf("\"humidity\"") + 11;
            int humidityEnd = json.indexOf(",", humidityStart);
            int humidity = Integer.parseInt(json.substring(humidityStart, humidityEnd));
            
            int weatherStart = json.indexOf("\"description\"") + 14;
            int weatherEnd = json.indexOf("\"", weatherStart);
            String weather = json.substring(weatherStart, weatherEnd);
            
            return String.format("{\"temperature\": %.1f, \"condition\": \"%s\", \"humidity\": %d, \"source\": \"OpenWeatherMap\"}", 
                    temp, weather, humidity);
        } catch (Exception e) {
            return json;
        }
    }
    
    /**
     * 创建演示响应（无 API Key 时）
     */
    private String createDemoResponse(String city) {
        // 基于城市名生成合理的演示数据
        int hash = city.hashCode();
        double temp = 15 + ((hash & 0xFF) % 20); // 15-35°C
        int humidity = 40 + ((hash >> 8) & 0xFF) % 40; // 40-80%
        String[] conditions = {"晴", "多云", "阴", "小雨", "大雨"};
        String condition = conditions[Math.abs(hash) % conditions.length];
        
        return String.format("{\"city\": \"%s\", \"temperature\": %.1f, \"condition\": \"%s\", \"humidity\": %d, \"demo\": true}", 
                city, temp, condition, humidity);
    }
}
