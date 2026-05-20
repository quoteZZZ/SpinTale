package com.spintale.ai.agent.tool.builtin;

import java.net.URI;
import java.util.Map;
import java.util.Set;

import com.spintale.ai.infrastructure.properties.AiProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spintale.ai.agent.tool.registry.AiTool;
import com.spintale.ai.agent.tool.registry.AiTool.ToolSchema;

import lombok.extern.slf4j.Slf4j;

/**
 * Current weather tool backed by OpenWeatherMap.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "spintale.ai.tools.weather", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WeatherTool implements AiTool {

    private static final Set<String> ALLOWED_UNITS = Set.of("metric", "imperial", "standard");

    private final AiProperties properties;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeatherTool(AiProperties properties) {
        this.properties = properties;
    }

    @Override
    public String getName() {
        return "get_weather";
    }

    @Override
    public String getDescription() {
        return "Get current weather for a city, including temperature, humidity, condition, wind, and visibility.";
    }

    @Override
    public ToolSchema getSchema() {
        return ToolSchema.builder()
                .addProperty("city", "string", "City name, for example Beijing, Shanghai, or New York.", true)
                .addProperty("unit", "string", "Temperature unit: metric, imperial, or standard. Default: metric.", false)
                .addProperty("lang", "string", "OpenWeatherMap language code. Default: zh_cn.", false)
                .build();
    }

    @Override
    public String execute(Map<String, Object> arguments) {
        String city = asString(arguments, "city", "");
        String unit = normalizeUnit(asString(arguments, "unit", "metric"));
        String lang = asString(arguments, "lang", "zh_cn");
        var weather = properties.getTools().getWeather();
        String apiKey = weather.getApiKey();

        if (city.isBlank()) {
            return error("city is required");
        }

        if (apiKey == null || apiKey.isBlank() || "demo".equalsIgnoreCase(apiKey)) {
            log.debug("Using demo weather data because no API key is configured");
            return getDemoWeather(city, unit);
        }

        try {
            URI uri = UriComponentsBuilder.fromUriString(weather.getBaseUrl() + "/weather")
                    .queryParam("q", city)
                    .queryParam("appid", apiKey)
                    .queryParam("units", unit)
                    .queryParam("lang", lang)
                    .build()
                    .encode()
                    .toUri();

            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("Failed to fetch weather data: {}", response.getStatusCode());
                return error("failed to fetch weather data");
            }

            return formatWeather(response.getBody());
        } catch (Exception e) {
            log.error("Error fetching weather data for city: {}", city, e);
            return error("weather query failed: " + e.getMessage());
        }
    }

    private String formatWeather(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
        ObjectNode result = objectMapper.createObjectNode();
        result.put("city", root.path("name").asText(""));
        result.put("country", root.path("sys").path("country").asText(""));
        result.put("temperature", root.path("main").path("temp").asDouble());
        result.put("feels_like", root.path("main").path("feels_like").asDouble());
        result.put("humidity", root.path("main").path("humidity").asInt());
        result.put("pressure", root.path("main").path("pressure").asInt());
        result.put("condition", root.path("weather").path(0).path("description").asText(""));
        result.put("wind_speed", root.path("wind").path("speed").asDouble());
        result.put("visibility", root.path("visibility").asInt());
        result.put("timestamp", System.currentTimeMillis());
        return result.toString();
    }

    private String getDemoWeather(String city, String unit) {
        double temp = 20 + Math.random() * 15;
        int humidity = 40 + (int) (Math.random() * 40);
        String[] conditions = {"sunny", "cloudy", "overcast", "light rain"};
        String condition = conditions[(int) (Math.random() * conditions.length)];

        ObjectNode result = objectMapper.createObjectNode();
        result.put("city", city);
        result.put("unit", unit);
        result.put("temperature", Math.round(temp * 10.0) / 10.0);
        result.put("feels_like", Math.round((temp - 2) * 10.0) / 10.0);
        result.put("humidity", humidity);
        result.put("condition", condition);
        result.put("wind_speed", Math.round((2 + Math.random() * 5) * 10.0) / 10.0);
        result.put("note", "demo data; configure spintale.ai.tools.weather.api-key for live data");
        return result.toString();
    }

    private String normalizeUnit(String unit) {
        String normalized = unit == null ? "metric" : unit.trim().toLowerCase();
        return ALLOWED_UNITS.contains(normalized) ? normalized : "metric";
    }

    private String asString(Map<String, Object> arguments, String key, String defaultValue) {
        if (arguments == null) {
            return defaultValue;
        }
        Object value = arguments.get(key);
        return value == null ? defaultValue : String.valueOf(value).trim();
    }

    private String error(String message) {
        ObjectNode error = objectMapper.createObjectNode();
        error.put("error", message);
        return error.toString();
    }
}
