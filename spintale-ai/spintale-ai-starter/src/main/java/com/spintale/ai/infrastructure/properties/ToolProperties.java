package com.spintale.ai.infrastructure.properties;

import lombok.Data;

/**
 * Built-in AI tool settings.
 */
@Data
public class ToolProperties {
    private WeatherConfig weather = new WeatherConfig();

    @Data
    public static class WeatherConfig {
        private Boolean enabled = true;
        private String apiKey = "demo";
        private String baseUrl = "https://api.openweathermap.org/data/2.5";
    }
}
